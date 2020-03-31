package top.dzou.drpc.manager;

import top.dzou.drpc.model.enums.SerializerEnum;
import top.dzou.drpc.serialize.SerializerDispatcher;

/**
 * Created by dingxiang
 *
 * @date 2020/3/31
 */
public class DRpcContext {

    private static volatile DRpcContext INSTANCE;

    public static DRpcContext getInstance() {
        synchronized (DRpcContext.class) {
            if (INSTANCE == null) {
                synchronized (DRpcContext.class) {
                    INSTANCE = new DRpcContext();
                }
            }
        }
        return INSTANCE;
    }

    private SerializerDispatcher serializerDispatcher = new SerializerDispatcher();
    private SerializerEnum serializer;

    public SerializerEnum getSerializer() {
        return this.serializer == null ? SerializerEnum.FILE : this.serializer;
    }

    public void setSerializer(SerializerEnum serializerEnum) {
        this.serializer = serializerEnum;
    }

    public SerializerDispatcher getSerializerDispatcher() {
        return serializerDispatcher;
    }

    public void setSerializerDispatcher(SerializerDispatcher serializerDispatcher) {
        this.serializerDispatcher = serializerDispatcher;
    }
}
