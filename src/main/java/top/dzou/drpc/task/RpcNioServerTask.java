package top.dzou.drpc.task;

import org.apache.log4j.Logger;
import top.dzou.drpc.model.MethodInvokeModel;
import top.dzou.drpc.util.SerializeUtil;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class RpcNioServerTask implements Runnable {

    private static Logger logger = Logger.getLogger(RpcNioServerTask.class);

    private byte[] bytes;

    private SocketChannel channel;
    private Map<String, Class> serviceRegistry;

    public RpcNioServerTask(byte[] bytes, SocketChannel channel, Map<String, Class> serviceRegistry) {
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
                requestNioServerHandle(serviceRegistry, methodInvokeModel, channel);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException | IOException e) {
                logger.error("server处理rpc调用失败" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void requestNioServerHandle(Map<String, Class> serviceRegistry, MethodInvokeModel methodInvokeModel, SocketChannel channel) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, IOException {
        String serviceName = methodInvokeModel.getInterfaceName();
        String methodName = methodInvokeModel.getMethodName();
        Class<?>[] parameterTypes = (Class<?>[]) methodInvokeModel.getParamsType();
        Object[] arguments = methodInvokeModel.getParams();
        logger.info("调用:service:" + serviceName + "method:" + methodName);
        Class serviceClass = serviceRegistry.get(serviceName);
        if (serviceClass == null) {
            logger.error("找不到service:" + serviceName);
            throw new ClassNotFoundException(serviceName + " not found");
        }
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        Object result = method.invoke(serviceClass.newInstance(), arguments);
        //将执行结果反序列化，通过channel发送给客户端
        byte[] bytes = SerializeUtil.serialize(result);
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 4);
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        channel.write(buffer);
    }
}
