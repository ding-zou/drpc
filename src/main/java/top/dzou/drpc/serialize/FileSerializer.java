package top.dzou.drpc.serialize;

import java.io.*;

/**
 * Created by dingxiang
 *
 * @date 2020/3/30
 */
public class FileSerializer implements ISerializer {
    public static byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            byte[] bytes = bos.toByteArray();
            return bytes;
        } catch (IOException e) {
            System.out.println("序列化对象出错！");
            e.printStackTrace();
            return null;
        }
    }

    public static Object deserialize(byte[] bytes) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();
        } catch (IOException e) {
            System.out.println("反序列化出错！");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            System.out.println("反序列化出错！");
            e.printStackTrace();
            return null;
        }
    }
}
