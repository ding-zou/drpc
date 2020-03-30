package top.dzou.drpc.client;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class NioChannelPool implements ConnectionPool<SocketChannel>{

    private static final Logger logger  = Logger.getLogger(NioChannelPool.class.getName());

    private String host;
    private int port;

    private List<SocketChannel> pool = new LinkedList<>();
    private static final int INIT_CONNECTION = 5;
    private Semaphore semaphore;

    public NioChannelPool(String host, int port) throws IOException {
        semaphore = new Semaphore(INIT_CONNECTION);
        this.host = host;
        this.port = port;

        for (int i = 0; i < INIT_CONNECTION; i++) {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.connect(new InetSocketAddress(host,port));
            pool.add(socketChannel);
        }
    }

    public SocketChannel getSocket() {
        try {
            logger.debug(Thread.currentThread().getName() + ":来获取连接");
            if (semaphore.availablePermits() <= 0) {
                logger.debug(Thread.currentThread().getName() + ":无可用连接，等待获取");
            }
            semaphore.acquire();
            synchronized (this) {
                SocketChannel con = pool.remove(0);
                logger.debug(Thread.currentThread().getName() + ":获取到连接," + con);
                return con;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void release(SocketChannel con) {
        synchronized (this) {
            if (con != null) {
                semaphore.release();
                logger.debug(Thread.currentThread().getName() + ":释放连接," + con);
                pool.add(con);
            }
        }
    }

}
