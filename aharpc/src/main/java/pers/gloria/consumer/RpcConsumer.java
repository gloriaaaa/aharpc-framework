package pers.gloria.consumer;

import com.google.protobuf.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import pers.gloria.RpcMetaProto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

public class RpcConsumer implements RpcChannel {
    /**
     * stub代理对象，需要接收一个实现了RpcChannel的对象，当用stub调用任意rpc方法的时候，
     * 全部都调用了当前这个RpcChannel的callMethod方法
     * @param methodDescriptor
     * @param rpcController
     * @param message
     * @param message1
     * @param rpcCallback
     */
    @Override
    public void callMethod(Descriptors.MethodDescriptor methodDescriptor, RpcController rpcController, Message message, Message message1, RpcCallback<Message> rpcCallback) {
        /**
         * 打包参数，递交网络发送
         * rpc调用参数格式：header_size + service_name + method_name + args
         */
        Descriptors.ServiceDescriptor sd = methodDescriptor.getService();
        String serviceName = sd.getName();
        String methodName = methodDescriptor.getName();

        //序列化头部信息
        RpcMetaProto.RpcMeta.Builder meta_builder = RpcMetaProto.RpcMeta.newBuilder();
        meta_builder.setServiceName(serviceName);
        meta_builder.setMethodName(methodName);
        byte[] metabuf = meta_builder.build().toByteArray();

        //参数
        byte[] argbuf = message.toByteArray();

        // 组织rpc参数信息
        ByteBuf buf = Unpooled.buffer(4 + metabuf.length + argbuf.length);
        buf.writeInt(metabuf.length);
        buf.writeBytes(metabuf);
        buf.writeBytes(argbuf);

        // 待发送的数据
        byte[] sendbuf = buf.array();

        // 通过网络发送rpc调用请求信息
        Socket client = null;
        OutputStream out = null;
        InputStream in = null;

        try {
            client = new Socket();
            client.connect(new InetSocketAddress("127.0.0.1",6000));
            out = client.getOutputStream();
            in = client.getInputStream();

            //发送数据
            out.write(sendbuf);
            out.flush();

            //wait等待rpc调用响应
            ByteArrayOutputStream recvbuf = new ByteArrayOutputStream();
            byte[] rbuf = new byte[1024];
            int size = in.read(rbuf);
            /**
             * 这里的size有可能是0 ，因为RpcProvider封装Response响应参数的时候，如果响应参数的成员变量的
             * 值都是默认值，实际上RpcProvier递给RpcServer就是一个空数据
             */
            if(size > 0) {
                recvbuf.write(rbuf, 0, size);
                rpcCallback.run(message1.getParserForType().parseFrom(recvbuf.toByteArray()));
            } else {
                rpcCallback.run(message1.getParserForType().parseFrom(new byte[0]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(out != null) {
                    out.close();
                }
                if(in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if(client != null) {
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
