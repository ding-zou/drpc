package top.dzou.drpc;

import top.dzou.drpc.client.DRpcClient;
import top.dzou.drpc.model.enums.SerializerEnum;
import top.dzou.drpc.model.enums.SocketEnum;

import java.io.IOException;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class RpcClientTest {

    public static void main(String[] args) throws IOException {
        DRpcClient client = new DRpcClient(SocketEnum.NIO,"localhost", 8088,60, SerializerEnum.PROTOBUF);
        long c = System.currentTimeMillis();
        HelloService service = client.getRemoteProxyObj(HelloService.class);
        System.out.println(service.sayHi(Hello.HelloArg.newBuilder().setName("test protobuf").build()));
//        System.out.println(service.sayHi("test"));
        long e = System.currentTimeMillis();
        System.out.println(e - c);
    }
}
