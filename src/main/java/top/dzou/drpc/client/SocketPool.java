package top.dzou.drpc.client;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class SocketPool implements ConnectionPool<Socket>{

    private String host;
    private int port;

    private List<Socket> pool = new LinkedList<>();
    private static final int INIT_CONNECTION = 5;
    private Semaphore semaphore;

    public SocketPool(String host,int port) throws IOException {
        semaphore = new Semaphore(INIT_CONNECTION);
        this.host = host;
        this.port = port;

        for (int i = 0; i < INIT_CONNECTION; i++) {
            pool.add(new Socket(host, port));
        }
    }

    public Socket getSocket() {
        try {
            System.out.println(Thread.currentThread().getName() + ":来获取连接");
            if (semaphore.availablePermits() <= 0) {
                System.out.println(Thread.currentThread().getName() + ":无可用连接，等待获取");
            }
            semaphore.acquire();
            synchronized (this) {
                Socket con = pool.remove(0);
                System.out.println(Thread.currentThread().getName() + ":获取到连接," + con);
                return con;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void release(Socket con) {
        synchronized (this) {
            if (con != null) {
                semaphore.release();
                System.out.println(Thread.currentThread().getName() + ":释放连接," + con);
                pool.add(con);
            }
        }
    }

}
