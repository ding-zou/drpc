package top.dzou.drpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class ServerImpl implements Server {

    private static final HashMap<String, Class> serviceRegistry = new HashMap<String, Class>();

    private static boolean isRunning = false;

    private static int port;

    public ServerImpl(int port) {
        this.port = port;
    }

    public void stop() {
        isRunning = false;
    }

    public void start() throws IOException{
        start(SocketEnum.NIO);
    }
    public void start(SocketEnum socketEnum) throws IOException {
        if(socketEnum.equals(SocketEnum.SOCKET)) {
            ServerSocket server = new ServerSocket();
            server.bind(new InetSocketAddress(port));
            System.out.println("start server");
            ServerManager.startSocketSelect(server,serviceRegistry);
        }else if(SocketEnum.NIO.equals(socketEnum)){
            ServerSocketChannel server = ServerSocketChannel.open();
            ServerSocket serverSocket = server.socket();
            serverSocket.bind(new InetSocketAddress(port));
            Selector selector = Selector.open();
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
            ServerManager.startNioSelect(selector,serviceRegistry);
        }
    }

    public void register(Class serviceInterface, Class impl) {
        serviceRegistry.put(serviceInterface.getName(), impl);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getPort() {
        return port;
    }

}
