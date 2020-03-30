package top.dzou.drpc.manager;

import top.dzou.drpc.task.RpcSocketServerTask;
import top.dzou.drpc.util.ThreadPool;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class SocketServerManager extends IServerManager{
    public void startSocketSelect(ServerSocket server, Map<String, Class> serviceRegistry) throws IOException {
        try {
            while (true) {
                // 1.监听客户端的TCP连接，接到TCP连接后将其封装成task，由线程池执行
                ThreadPool.addTask(new RpcSocketServerTask(serviceRegistry, server.accept()));
            }
        } finally {
            server.close();
        }
    }
}
