package top.dzou.drpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class RpcClient<T> {

    public static <T> T getRemoteProxyObj(SocketEnum socketEnum, final Class<?> serviceInterface, final InetSocketAddress addr) {
        // 1.将本地的接口调用转换成JDK的动态代理，在动态代理中实现接口的远程调用
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 2.创建Socket客户端，根据指定地址连接远程服务提供者
                        switch (socketEnum) {
                            case NIO:
                                return nioClientHandler(serviceInterface, addr, method, args);
                            case SOCKET:
                                return socketClientHandler(serviceInterface, addr, method, args);
                            default:
                                return null;
                        }
                    }
                });
    }

    private static Object socketClientHandler(final Class<?> serviceInterface, final InetSocketAddress addr, Method method, Object[] args) throws IOException {
        Socket socket = null;
        ObjectOutputStream output = null;
        ObjectInputStream input = null;
        try {
            socket = new Socket();
            socket.connect(addr);

            // 3.将远程服务调用所需的接口类、方法名、参数列表等编码后发送给服务提供者
            output = new ObjectOutputStream(socket.getOutputStream());
            output.writeUTF(serviceInterface.getName());
            output.writeUTF(method.getName());
            output.writeObject(method.getParameterTypes());
            output.writeObject(args);

            // 4.同步阻塞等待服务器返回应答，获取应答后返回
            input = new ObjectInputStream(socket.getInputStream());
            return input.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) socket.close();
            if (output != null) output.close();
            if (input != null) input.close();
        }
        return null;
    }

    private static Object nioClientHandler(final Class<?> serviceInterface, final InetSocketAddress addr, Method method, Object[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        socketChannel.connect(addr);
        MethodInvokeModel methodInvokeModel = new MethodInvokeModel(serviceInterface.getName(), method.getName(), method.getParameterTypes(), args);
        while (true) {
            int num = selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                if (selectionKey.isConnectable()) {
                    SocketChannel client = (SocketChannel) selectionKey.channel();
                    if (client.isConnectionPending()) {
                        client.finishConnect();
                    }
                    client.register(selector, SelectionKey.OP_WRITE);
                } else if (selectionKey.isWritable()) {
                    byte[] data = SerializeUtil.serialize(methodInvokeModel);
                    SocketChannel client = (SocketChannel) selectionKey.channel();
                    ByteBuffer writeBuffer = ByteBuffer.allocate(data.length + 4);
                    writeBuffer.clear();
                    writeBuffer.putInt(data.length);
                    writeBuffer.put(data);
                    writeBuffer.flip();
                    client.write(writeBuffer);
                    client.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    SocketChannel client = (SocketChannel) selectionKey.channel();
                    // 读取返回值长度
                    ByteBuffer byteBuffer = ByteBuffer.allocate(4);
                    int readHeadCount = client.read(byteBuffer);
                    if (readHeadCount < 0) {
                        return null;
                    }
                    // 将buffer切换为待读取状态
                    byteBuffer.flip();
                    int length = byteBuffer.getInt();

                    // 读取消息体
                    byteBuffer = ByteBuffer.allocate(length);
                    int readBodyCount = client.read(byteBuffer);
                    if (readBodyCount < 0) {
                        return null;
                    }
                    byte[] bytes = byteBuffer.array();
                    Object result = SerializeUtil.unSerialize(bytes);
                    return result;
                }
            }
            selectionKeys.clear();//每次处理完一个SelectionKey的事件，把该SelectionKey删除
        }
    }
}
