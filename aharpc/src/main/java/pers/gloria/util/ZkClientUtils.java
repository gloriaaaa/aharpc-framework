package pers.gloria.util;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 和zookeeper通信用的辅助工具类
 */
public class ZkClientUtils {
    private static String rootPath = "/arpc";
    private ZkClient zkClient;
    private Map<String, String> ephemeralMap = new HashMap<>();

    /**
     * 通过zk server字符串信息连接zkServer
     * @param serversList
     */
    public ZkClientUtils(String serversList){
        this.zkClient=new ZkClient(serversList,3000);
        // 如果root节点不存在，创建
        if(!this.zkClient.exists(rootPath)) {
            this.zkClient.createPersistent(rootPath, null);
        }
    }
    /**
     * 关闭和zkServer的连接
     */
    public void close(){
        this.zkClient.close();
    }
    /**
     * zk上创建临时性节点
     * @param path
     * @param data
     */
    public void createEphemeral(String path, String data) {
        path = rootPath + path;
        ephemeralMap.put(path, data);
        if(!this.zkClient.exists(path)) { // znode节点不存在，才创建
            this.zkClient.createEphemeral(path, data);
        }
    }

    /**
     * zk上创建永久性节点
     * @param path
     * @param data
     */
    public void createPersistent(String path, String data) {
        path = rootPath + path;
        if(!this.zkClient.exists(path)) { // znode节点不存在，才创建
            this.zkClient.createPersistent(path, data);
        }
    }
    /**
     * 读取znode节点的值
     * @param path
     * @return
     */
    public String readData(String path) {
        return this.zkClient.readData(rootPath+path, null);
    }

    public static void setRootPath(String rootPath) {
        ZkClientUtils.rootPath = rootPath;
    }

    public static String getRootPath() {
        return rootPath;
    }
    /**
     * 给zk上指定的znode添加watcher监听
     * @param path
     */
    public void addWatcher(String path) {
        this.zkClient.subscribeDataChanges(rootPath + path, new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            /**
             * 设置znode节点监听，因为如果zkclient断掉，由于zkserver无法及时获知zkclient的关闭状态，
             * 所以zkserver会等待session tiomeout时间以后，会把zkclient创建的临时节点全部删除掉，但是
             * 如果在session tiomeout时间内，又启动了同样的zkclient，那么等待session tiomeout时间超时以后
             * 原先创建的临时节点都没了
             * @param path
             * @throws Exception
             */
            @Override
            public void handleDataDeleted(String path) throws Exception {
                System.out.println("watcher -> handleDataDeleted : " + path);
                // 把删除掉的znode临时性节点重新创建一下
                String str = ephemeralMap.get(path);   // /nprpc/UserServiceRpc/reg
                if(str != null) {
                    zkClient.createEphemeral(path, str);
                }
            }
        });
    }
    /**
     * 测试zkclient工具类
     * @param args
     */
    public static void main(String[] args) {
        ZkClientUtils zk = new ZkClientUtils("127.0.0.1:2181");
        zk.createPersistent("/ProductService", "123456");
        System.out.println(zk.readData("/ProductService"));
        zk.close();
    }
}
