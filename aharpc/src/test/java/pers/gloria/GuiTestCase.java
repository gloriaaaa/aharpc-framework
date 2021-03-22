package pers.gloria;
/**
 * 事件回调操作
 *
 * 描述: 模拟界面类   接收用户发起的事件，  处理处理完成，显示结果
 * 需求：
 * 1. 下载完成后，需要显示信息
 * 2. 下载过程中，需要显示下载进度
 *
 * 代码某处： a.该干什么事情了  b.以及这个事情该怎么做 =》 在此处直接调用一个函数就可以完成了
 *
 * @Author gloria
 */
public class GuiTestCase implements INotifyCallBack{
    private DownLoad download;

    public GuiTestCase() {
        this.download = new DownLoad(this);
    }

    /**
     * 下载文件
     * @param file
     */
    public void downLoadFile(String file) {
        System.out.println("begin start download file:" + file);
        download.start(file);
    }

    /**
     * 显示下载进度的方法
     * @param file
     * @param progress
     */
    public void progress(String file, int progress) {
        System.out.println("dowload file:" + file + " progress:" + progress + "%");
    }

    /**
     * 显示文件下载完成了
     * @param file
     */
    public void result(String file) {
        System.out.println("download file:" + file + " over.");
    }

    public static void main(String[] args) {
        GuiTestCase gui = new GuiTestCase();
        gui.downLoadFile("我要学Java");
    }
}

// 把需要上报的事件都定义在接口里面
interface INotifyCallBack {
    void progress(String file, int progress);
    void result(String file);
}

/**
 * 负责下载内容的类
 */
class DownLoad {

    private INotifyCallBack cb; // 面向接口的编程

    public DownLoad(INotifyCallBack cb) {
        this.cb = cb;
    }

    /**
     * 底层执行下载任务的方法
     * @param file
     */
    public void start(String file) {
        int count = 0;

        try {
            while(count <= 100) {
                cb.progress(file, count); // 上报下载的进度
                Thread.sleep(1000);
                count += 20;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cb.result(file); // 上报文件下载完成
    }
}