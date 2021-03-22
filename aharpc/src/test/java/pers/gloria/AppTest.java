package pers.gloria;

import static org.junit.Assert.assertTrue;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * 测试Protobuf的序列化和反序列化
     */
    @Test
    public void test1() {
        TestProto.LoginRequest.Builder login_builder = TestProto.LoginRequest.newBuilder();
        //不能直接生成LoginRequest的对象，建造者模式封装了对象创建的细节，Builder是静态内部类
        //先生成建造者，再对对象进行建造
        login_builder.setName("zhang san");
        login_builder.setPwd("123456");

        TestProto.LoginRequest request = login_builder.build();//通过LoginRequest的建造者来建造对象
        System.out.println(request.getName());
        System.out.println(request.getPwd());

        /**
         * 把LoginRequest对象序列化成字节流，通过网络发送出去
         * 此处的sendbuf就可以通过网络发送出去了
         */
        byte[] sendbuf = request.toByteArray();
        /**
         * Protobuf从byte数组字节流反序列化生成LoginRequst对象
         */
        try {
            TestProto.LoginRequest r = TestProto.LoginRequest.parseFrom(sendbuf);
            System.out.println(r.getName());
            System.out.println(r.getPwd());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
    /**
     * 测试proerties文件的加载
     */
    @Test
    public void test2() {
        Properties pro = new Properties(); // key-value
        try {
            pro.load(AppTest.class.getClassLoader().getResourceAsStream("config.properties"));
            System.out.println(pro.getProperty("IP"));
            System.out.println(pro.getProperty("PORT"));
            System.out.println(pro.getProperty("ZOOKEEPER"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
