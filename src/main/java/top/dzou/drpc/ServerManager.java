package top.dzou.drpc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class ServerManager {

    private static int CAPACITY = 1024;

    private static ByteBuffer readBuffer = ByteBuffer.allocate(CAPACITY);

    private static ByteBuffer writeBuffer = ByteBuffer.allocate(CAPACITY);
    private static Map<SocketChannel, String> clientMap = new HashMap<>();

    public static void startNioSelect(Selector selector, HashMap<String, Class> serviceRegistry) throws IOException {
        while (true) {
            int num = selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            selectionKeys.forEach(selectionKey -> {
                try {
                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel client = null;
                        client = server.accept();
                        client.configureBlocking(false);
                        String key = "";
                        client.register(selector, SelectionKey.OP_READ);
                        key = "[" + UUID.randomUUID() + ":gzh]";
                        System.out.println(key + ":连接成功!");
                        clientMap.put(client, key);
                    } else if (selectionKey.isReadable()) {
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        byte[] bytes = readMsgFromClient(channel);
                        if (bytes != null && bytes.length > 0) {
                            // 读取之后将任务放入线程池异步返回
                            RpcNioMultServerTask task = new RpcNioMultServerTask(bytes, channel,serviceRegistry);
                            ThreadPool.addTask(task);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();

                }
            });
            selectionKeys.clear();//每次处理完一个SelectionKey的事件，把该SelectionKey删除
        }
    }

    public static byte[] readMsgFromClient(SocketChannel channel) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        try {
            // 首先读取消息头（自己设计的协议头，此处是消息体的长度）
            int headCount = channel.read(byteBuffer);
            if (headCount < 0) {
                return null;
            }
            byteBuffer.flip();
            int length = byteBuffer.getInt();
            // 读取消息体
            byteBuffer = ByteBuffer.allocate(length);
            int bodyCount = channel.read(byteBuffer);
            if (bodyCount < 0) {
                return null;
            }
            return byteBuffer.array();
        } catch (IOException e) {
            System.out.println("读取数据异常");
            e.printStackTrace();
            return null;
        }
    }

    public static void startSocketSelect(ServerSocket server, HashMap<String, Class> serviceRegistry) throws IOException {
        try {
            while (true) {
                // 1.监听客户端的TCP连接，接到TCP连接后将其封装成task，由线程池执行
                ThreadPool.addTask(new ServerManager.ServiceTask(serviceRegistry, server.accept()));
            }
        } finally {
            server.close();
        }
    }

    static class ServiceTask implements Runnable {
        Socket client = null;
        Map<String, Class> serviceRegistry;

        public ServiceTask(Map<String, Class> serviceRegistry, Socket client) {
            this.client = client;
            this.serviceRegistry = serviceRegistry;
        }

        public void run() {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;
            try {
                // 2.将客户端发送的码流反序列化成对象，反射调用服务实现者，获取执行结果
                input = new ObjectInputStream(client.getInputStream());
                String serviceName = input.readUTF();
                String methodName = input.readUTF();
                Class<?>[] parameterTypes = (Class<?>[]) input.readObject();
                Object[] arguments = (Object[]) input.readObject();
                Class serviceClass = serviceRegistry.get(serviceName);
                if (serviceClass == null) {
                    throw new ClassNotFoundException(serviceName + " not found");
                }
                Method method = serviceClass.getMethod(methodName, parameterTypes);
                Object result = method.invoke(serviceClass.newInstance(), arguments);

                // 3.将执行结果反序列化，通过socket发送给客户端
                output = new ObjectOutputStream(client.getOutputStream());
                output.writeObject(result);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    static class RpcNioMultServerTask implements Runnable {

        private byte[] bytes;

        private SocketChannel channel;
        private Map<String,Class> serviceRegistry;

        public RpcNioMultServerTask(byte[] bytes, SocketChannel channel,Map<String,Class> serviceRegistry) {
            this.bytes = bytes;
            this.channel = channel;
            this.serviceRegistry = serviceRegistry;
        }

        @Override
        public void run() {
            if (bytes != null && bytes.length > 0 && channel != null) {
                // 反序列化
                MethodInvokeModel methodInvokeModel = (MethodInvokeModel) SerializeUtil.unSerialize(bytes);
                // 调用服务并序列化结果然后返回
                try {
                    requestHandle(serviceRegistry,methodInvokeModel, channel);
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void requestHandle(Map<String,Class>serviceRegistry,MethodInvokeModel methodInvokeModel, SocketChannel channel) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException {
//            Long requestId = requstObject.getRequestId();

            String serviceName = methodInvokeModel.getInterfaceName();
            String methodName = methodInvokeModel.getMethodName();
            Class<?>[] parameterTypes = (Class<?>[]) methodInvokeModel.getParamsType();
            Object[] arguments = (Object[]) methodInvokeModel.getParams();
            Class serviceClass = serviceRegistry.get(serviceName);
            if (serviceClass == null) {
                throw new ClassNotFoundException(serviceName + " not found");
            }
            Method method = serviceClass.getMethod(methodName, parameterTypes);
            Object result = method.invoke(serviceClass.newInstance(), arguments);

            // 3.将执行结果反序列化，通过socket发送给客户端
            byte[] bytes = SerializeUtil.serialize(result);
            ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 4);
            // 为了便于客户端获得请求ID，直接将id写在头部（这样客户端直接解析即可获得，不需要将所有消息反序列化才能得到）
            // 然后写入消息题的长度，最后写入返回内容
//                buffer.putLong(requestId);
            buffer.putInt(bytes.length);
            buffer.put(bytes);
            buffer.flip();
            channel.write(buffer);
        }
    }

}
