package pers.gloria.provider;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 描述: rpc方法发布的站点，只需要一个站点就可以发布当前主机上所有的rpc方法了  用单例模式设计RpcProvider
 *
 * @Author gloria
 */
public class RpcProvider {
    private static final String SERVER_IP = "ip";
    private static final String SERVER_PORT = "port";
    private static final String ZK_SERVER = "zookeeper";
    private String serverIp;
    private int serverPort;
    private String zkServer;

    /**
     * 启动Rpc站点提供服务
     */
    public void start() {
        serviceMap.forEach((k, v)->{
            System.out.println(k);
            v.methodMap.forEach((a,b)->System.out.println(a));
        });
        System.out.println("rpc server start at " + serverIp + ":" + serverPort);
    }
    /**
     * 服务方法的类型信息
     */
    private class ServiceInfo {
        public ServiceInfo() {
            this.service = null;
            this.methodMap = new HashMap<>();
        }
        Service service;
        Map<String, Descriptors.MethodDescriptor> methodMap;//只是读操作，用hashmap也ok，线程
    }
    /**
     * 包含所有的rpc服务对象和服务方法
     */
    private Map<String, ServiceInfo> serviceMap;
    /**
     * 注册rpc服务方法   只要支持rpc方法的类，都实现了com.google.protobuf.Service这个接口
     * @param service
     */
    public void registerRpcService(Service service) {
        Descriptors.ServiceDescriptor sd = service.getDescriptorForType();
        // 获取服务对象的名称
        String serviceName = sd.getName();
        ServiceInfo si = new ServiceInfo();
        si.service = service;
        // 获取服务对象的所有服务方法列表
        List<Descriptors.MethodDescriptor> methodList = sd.getMethods();
        methodList.forEach(method->{
            // 获取服务方法名字
            String methodName = method.getName();
            si.methodMap.put(methodName, method);
        });
        serviceMap.put(serviceName, si);
    }

    /**
     * 封装RpcProvider对象创建的细节,最简单，懒汉，静态内部类
     * 先对构造方法私有化
     * 定义一个rpcprovider唯一的一个实例instance
     */
    public static class Builder {
        private static RpcProvider INSTANCE = new RpcProvider();

        /**
         * 从配置文件中读取rpc server的ip和port，给INSTANCE对象初始化数据
          * @param file
         */
        public RpcProvider build (String file)
        {
            Properties pro = new Properties();
            try {
                pro.load(Builder.class.getClassLoader().getResourceAsStream(file));
                INSTANCE.setServerIp(pro.getProperty(SERVER_IP));
                INSTANCE.setServerPort(Integer.parseInt(pro.getProperty(SERVER_PORT)));
                INSTANCE.setZkServer(pro.getProperty(ZK_SERVER));
                return INSTANCE;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }
//        /**
//         * 通过builder创建一个RpcProvider对象
//         * @return
//         */
//        public RpcProvider build() {
//            return INSTANCE;
//        }
    }

    private void setZkServer(String property) {
        this.zkServer = zkServer;
    }

    private void setServerPort(int parseInt) {
        this.zkServer = zkServer;
    }

    private void setServerIp(String property) {
        this.serverIp = serverIp;
    }

    /**
     * 返回一个对象建造器
     * @return
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    private RpcProvider(){
        this.serviceMap = new HashMap<>();
    }
}
