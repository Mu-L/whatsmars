package org.hongxi.whatsmars.common.serialize;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author shenhongxi 2019/8/5
 */
public class SerializationTest {

    @Test
    public void testHessian() throws Exception {
        Serialization serialization = new Hessian2Serialization();
        User user = new User("hongxi", 30);
        byte[] bytes = serialization.serialize(user);
        user = serialization.deserialize(bytes, User.class);
        assertEquals("hongxi", user.name());
        assertEquals(30, user.age());
    }

    @Test
    public void testJava() throws Exception {
        Serialization serialization = new JavaSerialization();
        User user = new User("hongxi", 30);
        byte[] bytes = serialization.serialize(user);
        user = serialization.deserialize(bytes, User.class);
        assertEquals("hongxi", user.name());
        assertEquals(30, user.age());
    }

    @Test
    public void testJson() throws Exception {
        Serialization serialization = new FastJsonSerialization();
        User user = new User("hongxi", 30);
        byte[] bytes = serialization.serialize(user);
        user = serialization.deserialize(bytes, User.class);
        assertEquals("hongxi", user.name());
        assertEquals(30, user.age());
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

}