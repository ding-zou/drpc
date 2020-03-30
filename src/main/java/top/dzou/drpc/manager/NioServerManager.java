package top.dzou.drpc.manager;

import org.apache.log4j.Logger;
import top.dzou.drpc.util.ThreadPool;
import top.dzou.drpc.task.RpcNioServerTask;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class NioServerManager extends IServerManager {

    private static final Logger logger = Logger.getLogger(NioServerManager.class);

    private static Map<SocketChannel, String> clientMap = new HashMap<>();
    private ByteBuffer readHeaderBuffer = ByteBuffer.allocate(4);
    private ByteBuffer readBodyBuffer;
    private ServerSocketChannel serverSocketChannel;
    private SocketChannel socketChannel;

    public void startNioSelect(Selector selector, Map<String, Class> serviceRegistry) throws IOException {
        while (true) {
            int num = selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            selectionKeys.forEach(selectionKey -> {
                try {
                    if (selectionKey.isAcceptable()) {
                        serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel client = serverSocketChannel.accept();
                        client.configureBlocking(false);
                        String key = "";
                        client.register(selector, SelectionKey.OP_READ);
                        key = "[" + UUID.randomUUID() + "]";
                        logger.info(key + ":连接成功!");
                        clientMap.put(client, key);
                    } else if (selectionKey.isReadable()) {
                        socketChannel = (SocketChannel) selectionKey.channel();
                        byte[] bytes = readMsgFromClient(socketChannel);
                        if (bytes != null && bytes.length > 0) {
                            // 读取之后将任务放入线程池异步返回
                            RpcNioServerTask task = new RpcNioServerTask(bytes, socketChannel, serviceRegistry);
                            ThreadPool.addTask(task);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("nio server error " + e.getMessage());
                }
            });
            selectionKeys.clear();//每次处理完一个SelectionKey的事件，把该SelectionKey删除
        }
    }

    private byte[] readMsgFromClient(SocketChannel channel) {
        readHeaderBuffer.clear();
        try {
            // 首先读取消息头、消息体的长度
            int headCount = channel.read(readHeaderBuffer);
            if (headCount < 0) {
                return null;
            }
            readHeaderBuffer.flip();
            int length = readHeaderBuffer.getInt();
            // 读取消息体
            readBodyBuffer = ByteBuffer.allocate(length);
            int bodyCount = channel.read(readBodyBuffer);
            if (bodyCount < 0) {
                return null;
            }
            return readBodyBuffer.array();
        } catch (IOException e) {
            logger.error("读取数据异常");
            e.printStackTrace();
            return null;
        }
    }
}
