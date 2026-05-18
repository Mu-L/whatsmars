package org.hongxi.whatsmars.common.serialize;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author shenhongxi 2019/8/5
 */
public class SerializationTest {

    @Test
    public void testHessian() throws Exception {
        Serialization serialization = new HessianSerialization();
        User user = new User("hongxi", 30);
        byte[] bytes = serialization.serialize(user);
        user = serialization.deserialize(bytes, User.class);
        assertEquals("hongxi", user.name);
        assertEquals(30, user.age);
    }

    @Test
    public void testJava() throws Exception {
        Serialization serialization = new JavaSerialization();
        User user = new User("hongxi", 30);
        byte[] bytes = serialization.serialize(user);
        user = serialization.deserialize(bytes, User.class);
        assertEquals("hongxi", user.name);
        assertEquals(30, user.age);
    }

    @Test
    public void testJson() throws Exception {
        Serialization serialization = new FastJsonSerialization();
        User user = new User("hongxi", 30);
        byte[] bytes = serialization.serialize(user);
        user = serialization.deserialize(bytes, User.class);
        assertEquals("hongxi", user.name);
        assertEquals(30, user.age);
    }

    @Test
    public void testProtobuf() throws Exception {
        Serialization serialization = new ProtobufSerialization();
        // 9种基本类型之外的其他类型必须实现 MessageLite 接口
        Integer data = 1;
        byte[] bytes = serialization.serialize(data);
        data = serialization.deserialize(bytes, Integer.class);
        assertEquals(1, data);
    }

    @Test
    public void testHessianMulti() throws Exception {
        Serialization serialization = new HessianSerialization();
        User user = new User("hongxi", 30);
        Object[] data = new Object[]{user, 123, "xxx", false};
        byte[] bytes = serialization.serializeMulti(data);

        Class[] classes = new Class[data.length];
        for (int i = 0; i < data.length; i++) {
            classes[i] = data[i].getClass();
        }
        data = serialization.deserializeMulti(bytes, classes);
        assertEquals(4, data.length);
        assertEquals("xxx", data[2].toString());
    }

    @Test
    public void testJavaMulti() throws Exception {
        Serialization serialization = new JavaSerialization();
        User user = new User("hongxi", 30);
        Object[] data = new Object[]{user, 123, "xxx", false};
        byte[] bytes = serialization.serializeMulti(data);

        Class[] classes = new Class[data.length];
        for (int i = 0; i < data.length; i++) {
            classes[i] = data[i].getClass();
        }
        data = serialization.deserializeMulti(bytes, classes);
        assertEquals(4, data.length);
        assertEquals("xxx", data[2].toString());
    }

    @Test
    public void testJsonMulti() throws Exception {
        Serialization serialization = new FastJsonSerialization();
        User user = new User("hongxi", 30);
        Object[] data = new Object[]{user, 123, "xxx", false};
        byte[] bytes = serialization.serializeMulti(data);

        Class[] classes = new Class[data.length];
        for (int i = 0; i < data.length; i++) {
            classes[i] = data[i].getClass();
        }
        data = serialization.deserializeMulti(bytes, classes);
        assertEquals(4, data.length);
        assertEquals("xxx", data[2].toString());
    }

    @Test
    public void testProtobufMulti() throws Exception {
        Serialization serialization = new ProtobufSerialization();
        Object[] data = new Object[]{9999L, 123, "xxx", false};
        byte[] bytes = serialization.serializeMulti(data);

        Class[] classes = new Class[data.length];
        for (int i = 0; i < data.length; i++) {
            classes[i] = data[i].getClass();
        }
        data = serialization.deserializeMulti(bytes, classes);
        assertEquals(4, data.length);
        assertEquals("xxx", data[2].toString());
    }

}
