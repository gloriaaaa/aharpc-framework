package pers.gloria;

import pers.gloria.consumer.RpcConsumer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        /**
         * 模拟RPC方法调用者  google  grpc
         */
        UserServiceProto.UserServiceRpc.Stub stub
                =  UserServiceProto.UserServiceRpc.newStub(new RpcConsumer());
        UserServiceProto.LoginRequest.Builder login_builder = UserServiceProto.LoginRequest.newBuilder();
        login_builder.setName("zhang san");
        login_builder.setPwd("888888");
        stub.login(null,login_builder.build(),response -> {
            /**
             * rpc方法调用完成后的返回值
             */
            System.out.println("receive rpc call response!");
            if(response.getErrno() == 0) { // 调用正常
                System.out.println(response.getResult());
            } else { // 调用出错
                System.out.println(response.getErrinfo());
            }
        });
    }
}
