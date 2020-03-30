package top.dzou.drpc;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class RpcTest {

    public static void main(String[] args) throws IOException {
        new Thread(new Runnable() {
            public void run() {
                try {
                    ServerImpl serviceServer = new ServerImpl(8088);
                    serviceServer.register(HelloService.class, HelloServiceImpl.class);
                    serviceServer.start(SocketEnum.NIO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        long c = System.currentTimeMillis();
        HelloService service = RpcClient.getRemoteProxyObj(SocketEnum.NIO,HelloService.class, new InetSocketAddress("localhost", 8088));
        System.out.println(service.sayHi("test"));
        long e = System.currentTimeMillis();
        System.out.println(e-c);
    }
}
