package top.dzou.drpc.server;

import java.io.IOException;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public interface DRpcServer {
    void stop();

    void start() throws IOException;

    void register(Class serviceInterface, Class impl);

    boolean isRunning();

    int getPort();
}
