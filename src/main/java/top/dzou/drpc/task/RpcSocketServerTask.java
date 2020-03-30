package top.dzou.drpc.task;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class RpcSocketServerTask implements Runnable {
    private static Logger logger = Logger.getLogger(RpcSocketServerTask.class);
    Socket client = null;
    Map<String, Class> serviceRegistry;

    public RpcSocketServerTask(Map<String, Class> serviceRegistry, Socket client) {
        this.client = client;
        this.serviceRegistry = serviceRegistry;
    }

    public void run() {
        ObjectInputStream input = null;
        ObjectOutputStream output = null;
        try {
            // 将客户端发送的码流反序列化成对象，反射调用服务实现者，获取执行结果
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

            // 将执行结果反序列化，通过socket发送给客户端
            output = new ObjectOutputStream(client.getOutputStream());
            output.writeObject(result);
        } catch (Exception e) {
            logger.error("server执行rpc调用失败" + e.getMessage());
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
