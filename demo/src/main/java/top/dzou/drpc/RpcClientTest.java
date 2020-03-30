package top.dzou.drpc;

import top.dzou.drpc.client.DRpcClient;
import top.dzou.drpc.model.enums.SocketEnum;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class RpcClientTest {

    public static void main(String[] args) throws IOException {
        long c = System.currentTimeMillis();
        HelloService service = DRpcClient.getRemoteProxyObj(SocketEnum.NIO, HelloService.class, new InetSocketAddress("localhost", 8088));
        System.out.println(service.sayHi("test"));
        long e = System.currentTimeMillis();
        System.out.println(e - c);
    }
}
