package top.dzou.drpc.client;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public interface ConnectionPool<T> {
    T getSocket();
    void release(T con);
}
