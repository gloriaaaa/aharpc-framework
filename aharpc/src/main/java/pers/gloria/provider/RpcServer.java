package pers.gloria.provider;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ObjectEncoder;
import pers.gloria.RpcMetaProto;
import pers.gloria.callback.INotifyProvider;

/**
 *描述: Rpc服务器端，使用netty开发  apache mina
 */
public class RpcServer {
    private INotifyProvider notifyProvider;

    public RpcServer(INotifyProvider notify) {
        this.notifyProvider = notify;
    }

    public void start(String ip, int port)
    {
        // 创建主事件循环，对应I/O线程，主要用来处理新用户的连接事件
        EventLoopGroup mainGroup = new NioEventLoopGroup(1);
        // 创建worker工作线程事件循环，主要用来处理已连接用户的可读写事件
        EventLoopGroup workerGroup = new NioEventLoopGroup(3);
        // netty网络服务的启动辅助类
        ServerBootstrap b = new ServerBootstrap();
        b.group(mainGroup, workerGroup)
                .channel(NioServerSocketChannel.class)  // 底层使用Java Nio Selector模型
                .option(ChannelOption.SO_BACKLOG, 1024)   // 设置TCP参数,3次握手全连接用户队列长度
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        /**
                         * 1.设置数据的编码和解码器   网络的字节流 《=》 业务要处理的数据类型
                         * 2.设置具体的处理器回调
                         */
                        channel.pipeline().addLast(new ObjectEncoder()); // 编码
                        channel.pipeline().addLast(new RpcServerChannel()); // 设置事件回调处理器

                    }
                });// 注册事件回调，把业务层代码和网络层的代码完全区分开
        try{
            // 阻塞，开启网络服务
            ChannelFuture f = b.bind(ip, port).sync();//同步等待try catch
            // 关闭网络服务
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            mainGroup.shutdownGracefully();
        }
    }

    /**
     * 继承自netty的ChannelInboundHandlerAdapter适配器类，主要提供相应的回调操作
     * 我负责做,netty负责调,netty知道事情什么时候发生,我知道事情发生了该做什么事情
     */
    private class RpcServerChannel extends ChannelInboundHandlerAdapter{
        /**
         * 处理接收到的事件
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            /**
             * ByteBuf<=netty   类似java nio的ByteBuff
             * request就是远端发送过来的rpc调用请求包含的所有的信息参数
             *
             * header_size + UserServiceRpclogin + zhangsan123456(LoginRequest)
             * 20 + UserServiceRpclogin + 参数数据
             */
            ByteBuf request = (ByteBuf)msg;

            // 1.先读取头部信息的长度
            int header_size = request.readInt();

            // 2.读取头部信息（服务对象名称和服务方法名称）
            byte[] metabuf = new byte[header_size];
            request.readBytes(metabuf);

            // 3. 反序列化生成RpcMeta
            RpcMetaProto.RpcMeta rpcmeta = RpcMetaProto.RpcMeta.parseFrom(metabuf);
            String serviceName = rpcmeta.getServiceName();
            String methodName = rpcmeta.getMethodName();

            // 4. 读取rpc方法的参数
            byte[] argbuf = new byte[request.readableBytes()];
            request.readBytes(argbuf);

            // 5. serviceName methodName argbuf
            byte[] response = notifyProvider.notify(serviceName, methodName, argbuf);

            // 6. 把rpc方法调用的响应response通过网络发给rpc调用方
            ByteBuf buf = Unpooled.buffer(response.length);
            buf.writeBytes(response);
            ChannelFuture f = ctx.writeAndFlush(buf);

            // 7. 模拟http响应完成后，直接关闭连接
            if(f.sync().isSuccess()) {
                ctx.close();
            }
        }

        /**
         * 连接异常处理
         * @param ctx
         * @param cause
         * @throws Exception
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }
    }
}

