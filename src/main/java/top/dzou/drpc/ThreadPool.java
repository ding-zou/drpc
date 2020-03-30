package top.dzou.drpc;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class ThreadPool {

    private static volatile ThreadPoolExecutor executor;

    public static void init() {
        if (executor == null) {
            synchronized (ThreadPool.class) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(10, 20, 200, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
                }
            }
        }
    }

    public static void addTask(Runnable task) {
        if (executor == null) {
            init();
        }
        executor.execute(task);
    }
}
