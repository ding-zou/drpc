package top.dzou.drpc.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.Selector;
import java.util.Map;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public abstract class IServerManager {
    public void startNioSelect(Selector selector, Map<String, Class> serviceRegistry) throws IOException{

    }
    public void startSocketSelect(ServerSocket server, Map<String, Class> serviceRegistry) throws IOException{

    }
}
