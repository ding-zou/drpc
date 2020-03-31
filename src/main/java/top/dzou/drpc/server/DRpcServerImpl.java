package top.dzou.drpc.server;

import org.apache.log4j.Logger;
import top.dzou.drpc.manager.DRpcContext;
import top.dzou.drpc.manager.IServerManager;
import top.dzou.drpc.manager.NioServerManager;
import top.dzou.drpc.manager.SocketServerManager;
import top.dzou.drpc.model.enums.SerializerEnum;
import top.dzou.drpc.model.enums.SocketEnum;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class DRpcServerImpl implements DRpcServer {

    private static Logger logger = Logger.getLogger(DRpcServerImpl.class);

    private static final Map<String, Class> serviceRegistry = new HashMap<>();
    private SocketEnum socketEnum;
    private static boolean isRunning = false;
    private ServerSocket socketServer;
    private ServerSocketChannel nioServer;
    private Selector nioSelector;
    private IServerManager manager;

    private DRpcContext dRpcContext;
    private static int port;

    public DRpcServerImpl(int port, SocketEnum socketEnum, SerializerEnum serializerEnum) {
        this.port = port;
        this.socketEnum = socketEnum;
        dRpcContext = DRpcContext.getInstance();
        dRpcContext.setSerializer(serializerEnum);
        if (socketEnum.equals(SocketEnum.SOCKET)) {
            manager = new SocketServerManager();
        } else if (SocketEnum.NIO.equals(socketEnum)) {
            manager = new NioServerManager();
        }
    }

    public void stop() {
        isRunning = false;
    }

    public void start() throws IOException {
        SocketEnum socketEnum = this.socketEnum == null ? SocketEnum.NIO : this.socketEnum;
        start(socketEnum);
    }

    private void start(SocketEnum socketEnum) throws IOException {
        if (socketEnum.equals(SocketEnum.SOCKET)) {
            manager.startSocketSelect(socketServer, serviceRegistry);
        } else if (SocketEnum.NIO.equals(socketEnum)) {
            manager.startNioSelect(nioSelector, serviceRegistry);
        }
    }


    public void register(Class serviceInterface, Class impl) {
        serviceRegistry.put(serviceInterface.getName(), impl);
        try {
            initSocket();
        } catch (IOException e) {
            logger.error("初始化socket失败" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initSocket() throws IOException {
        if (socketEnum.equals(SocketEnum.SOCKET)) {
            socketServer = new ServerSocket();
            socketServer.bind(new InetSocketAddress(port));
        } else if (SocketEnum.NIO.equals(socketEnum)) {
            nioServer = ServerSocketChannel.open();
            ServerSocket serverSocket = nioServer.socket();
            serverSocket.bind(new InetSocketAddress(port));
            nioSelector = Selector.open();
            nioServer.configureBlocking(false);
            nioServer.register(nioSelector, SelectionKey.OP_ACCEPT);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getPort() {
        return port;
    }

}
