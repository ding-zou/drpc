package top.dzou.drpc;

import top.dzou.drpc.model.enums.SerializerEnum;
import top.dzou.drpc.model.enums.SocketEnum;
import top.dzou.drpc.server.DRpcServer;
import top.dzou.drpc.server.DRpcServerImpl;

import java.io.IOException;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class RpcServerTest {
    public static void main(String[] args) {
        try {
            DRpcServer serviceServer = new DRpcServerImpl(8088, SocketEnum.NIO, SerializerEnum.PROTOBUF);
            serviceServer.register(HelloService.class, HelloServiceImpl.class);
            serviceServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
