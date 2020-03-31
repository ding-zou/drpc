package top.dzou.drpc.serialize;

import com.google.protobuf.Message;
import top.dzou.drpc.manager.DRpcContext;
import top.dzou.drpc.model.enums.SerializerEnum;

import java.nio.ByteBuffer;

/**
 * Created by dingxiang
 *
 * @date 2020/3/31
 */
public class SerializerDispatcher {

    public byte[] dispatchSerialize(Object result){
        SerializerEnum serializer = DRpcContext.getInstance().getSerializer();
        if(serializer == SerializerEnum.PROTOBUF){
            return ProtobufSerializer.serialize((Message) result);
        }else if(serializer == SerializerEnum.FILE){
            return FileSerializer.serialize(result);
        }
        return new byte[0];
    }

    public Object dispatchDeserialize(byte[] bytes){
        SerializerEnum serializer = DRpcContext.getInstance().getSerializer();
        if(serializer == SerializerEnum.PROTOBUF){
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            byte[] header = new byte[ProtobufSerializer.HEADER_LENGTH];
            buffer.get(header);
            int i = ProtobufSerializer.readInteger(header,0);
            int i2 = ProtobufSerializer.readInteger(header,4);
//                        ByteBuffer b1 = buffer.get(new byte[ProtobufSerializer.HEADER_LENGTH]);
            byte[] protobufClassNameBytes = new byte[i];
            byte[] protobufPayloadBytes = new byte[i2];
            buffer.get(protobufClassNameBytes);
            buffer.get(protobufPayloadBytes);

//                        int i = buffer.getInt(0);
//                        int i2 = buffer.getInt(4);
            return ProtobufSerializer.deserialize(ByteBuffer.wrap(protobufClassNameBytes),ByteBuffer.wrap(protobufPayloadBytes));
        }else if(serializer == SerializerEnum.FILE){
            return FileSerializer.deserialize(bytes);
        }
        return new Object();
    }
}
