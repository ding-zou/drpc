package top.dzou.drpc;

import java.io.IOException;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public interface Server {
    public void stop();

    public void start() throws IOException;

    public void register(Class serviceInterface, Class impl);

    public boolean isRunning();

    public int getPort();
}
