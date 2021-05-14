# aharpc
aharpc是基于Protobuf+Netty+ZooKeeper实现的简易分布式RPC通信框架，具体来说：
- 使用Protobuf完成对数据的序列化和反序列化，实现对RPC方法参数的打包和解析。
- 基于Netty网络库完成网络通信，实现发起RPC调用请求和响应RPC调用结果的功能。
- 使用ZooKeeper作为注册中心，实现服务注册功能。

## Quick start

### 服务提供端
项目中提供了一个模拟服务提供者的测试用例，即子项    目ahaprovider，核心代码如下：
1. 编写要发布的本地方法
```
public boolean login(String name, String pwd) {
    System.out.println("call UserServiceImpl -> login");
    System.out.println("name:" + name);
    System.out.println("pwd:" + pwd);
    return true;
}
```
2. 创建`user_service.proto`文件，根据要发布的方法定义对应的消息和rpc服务
```
syntax = "proto3";
package com.gloria;
option java_outer_classname = "UserServiceProto";
option java_generic_services = true;
message LoginRequest { //login方法会有两个参数值用户名和密码
  string name = 1;
  string pwd = 2;
}
message Response {
  int32 errno = 1; //错误码
  string errinfo = 2; //错误信息
  bool result = 3; //rpc调用返回值
}
// 定义RPC服务接口类和服务方法
service UserServiceRpc{
  rpc login(LoginRequest) returns (Response);
}
```
3. 编译proto文件生成java类 `UserServiceProto.java`
```
protoc user_service.proto --java_out=./
```
4.创建`UserServiceImpl.java`文件，实现UserServiceImpl类，该类需要继承UserServiceProto.UserServiceRpc抽象类，在该类中实现本地方法和对应的rpc代理方法
```
public class UserServiceImpl extends UserServiceProto.UserServiceRpc {
    /**
     * 登录业务
     * @param name
     * @param pwd
     * @return
     */
    public boolean login(String name, String pwd) {
        System.out.println("call UserServiceImpl -> login");
        System.out.println("name:" + name);
        System.out.println("pwd:" + pwd);
        return true;
    }
    /**
     * login的rpc代理方法
     * @param controller 可以接受方法执行状态，忽略
     * @param request
     * @param done
     */
    @Override
    public void login(RpcController controller, UserServiceProto.LoginRequest request, RpcCallback<UserServiceProto.Response> done) {
        // 1. 从request里面读取到远程rpc调用请求的参数了
        String name = request.getName();
        String pwd = request.getPwd();
        // 2. 根据解析的参数，做本地业务
        boolean result = login(name, pwd);

        // 3. 填写方法的响应值
        UserServiceProto.Response.Builder response_builder = UserServiceProto.Response.newBuilder();
        response_builder.setErrno(0);
        response_builder.setErrinfo("");
        response_builder.setResult(result);

        // 4. 把Response对象给到rpc框架，由框架负责处理发送rpc调用响应值
        done.run(response_builder.build());
    }
}
```
5. 启动可以提供rpc远程方法调用的Server
- 引入框架的RpcProvider类
```
import pers.gloria.provider.RpcProvider;
```
- 编写配置文件`config.properties`,包括服务器的ip和端口号以及zookeeper地址
```
## 服务器的ip地址
ip=127.0.0.1
## 服务器的端口号
port=6000
## zookeeper的连接字符串
zookeeper=127.0.0.1:2181
```
- 根据配置文件创建一个RpcProvider（rpc框架提供的）对象
```
RpcProvider.Builder builder = RpcProvider.newBuilder();
RpcProvider provider = builder.build("config.properties");
```
- 注册服务
```
provider.registerRpcService(new UserServiceImpl());
```
- 启动rpc server,阻塞等到远程rpc调用请求
```
provider.start();
```
### 服务调用端
项目中提供了一个模拟服务调用者的测试用例，即子项目ahaconsumer，核心代码如下：

1. 需要定义和服务端的相同的消息，直接将服务端的`UserServiceProto.java`拷贝过来
2. 开始rpc服务调用
- 引入框架的RpcConsumer类和ArpcController类
```
import pers.gloria.consumer.RpcConsumer;
import pers.gloria.controller.ArpcController;
```
- 将zookeeper的地址写入config.properites配置文件中
```
zookeeper=127.0.0.1:2181
```
- 创建消费端存根stub
```
UserServiceProto.UserServiceRpc.Stub stub
        =  UserServiceProto.UserServiceRpc.newStub(new RpcConsumer("config.properties"));
```
- 将调用服务的参数写入消息体
```
UserServiceProto.LoginRequest.Builder login_builder = UserServiceProto.LoginRequest.newBuilder();
login_builder.setName("zhang san");
login_builder.setPwd("888888");
```
- 使用stub发送消息调用远程方法并接收调用完成的返回值
```
ArpcController con = new ArpcController();
stub.login(con,login_builder.build(), response -> {
    /**
     * rpc方法调用完成后的返回值
     */
    if(con.failed()){
        System.out.println("failed");
        System.out.println(con.errorText());
    } else {
        System.out.println("receive rpc call response!");
        if(response.getErrno() == 0) { // 调用正常
            System.out.println(response.getResult());
        } else { // 调用出错
            System.out.println(response.getErrinfo());
        }
    }

});
```