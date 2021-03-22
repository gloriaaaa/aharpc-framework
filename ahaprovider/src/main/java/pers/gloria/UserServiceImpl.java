package pers.gloria;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * 描述: 原来是本地服务方法，现在要发布成RPC方法
 *
 * @Author gloria
 */
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
     * 注册业务
     * @param name
     * @param pwd
     * @param age
     * @param sex
     * @param phone
     * @return
     */
    public boolean reg(String name, String pwd, int age, String sex, String phone) {
        System.out.println("call UserServiceImpl -> reg");
        System.out.println("name:" + name);
        System.out.println("pwd:" + pwd);
        System.out.println("age:" + age);
        System.out.println("sex:" + sex);
        System.out.println("phone:" + phone);
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

    /**
     *
     * @param controller
     * @param request
     * @param done
     */
    @Override
    public void reg(RpcController controller, UserServiceProto.RegRequest request, RpcCallback<UserServiceProto.Response> done) {

    }
}
