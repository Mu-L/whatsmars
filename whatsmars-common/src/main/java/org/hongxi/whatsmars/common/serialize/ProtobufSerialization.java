package org.hongxi.whatsmars.common.serialize;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.MessageLite;

import java.io.*;
import java.lang.reflect.Method;

/**
 * protobuf序列化器,支持基本数据类型及其包装类、String、Throwable、Protobuf2/3对象
 *
 * @author shenhongxi 2019/8/5
 */
public class ProtobufSerialization implements Serialization {

    @Override
    public byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        // 对throwable使用 java ObjectOutputStream进行序列化
        if (Throwable.class.isAssignableFrom(obj.getClass())) {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
        } else {
            CodedOutputStream output = CodedOutputStream.newInstance(baos);
            serialize(output, obj);
            output.flush();
        }

        return baos.toByteArray();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clz) throws IOException, ClassNotFoundException {
        // 对throwable使用 java ObjectInputStream进行反序列化
        if (Throwable.class.isAssignableFrom(clz)) {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            return (T) ois.readObject();
        } else {
            CodedInputStream in = CodedInputStream.newInstance(bytes);
            return (T) deserialize(in, clz);
        }

    }

    protected void serialize(CodedOutputStream output, Object obj) throws IOException {
        if (obj == null) {
            output.writeBoolNoTag(true);
        } else {
            output.writeBoolNoTag(false);
            Class<?> clazz = obj.getClass();
            if (clazz == Integer.class) {
                output.writeSInt32NoTag((Integer) obj);
            } else if (clazz == Long.class) {
                output.writeSInt64NoTag((Long) obj);
            } else if (clazz == Boolean.class) {
                output.writeBoolNoTag((Boolean) obj);
            } else if (clazz == Byte.class) {
                output.writeRawByte((Byte) obj);
            } else if (clazz == Character.class) {
                output.writeSInt32NoTag((Character) obj);
            } else if (clazz == Short.class) {
                output.writeSInt32NoTag((Short) obj);
            } else if (clazz == Double.class) {
                output.writeDoubleNoTag((Double) obj);
            } else if (clazz == Float.class) {
                output.writeFloatNoTag((Float) obj);
            } else if (clazz == String.class) {
                output.writeStringNoTag(obj.toString());
            } else if (MessageLite.class.isAssignableFrom(clazz)) {
                output.writeMessageNoTag((MessageLite) obj);
            } else {
                throw new IllegalArgumentException("can't serialize " + clazz);
            }
        }
    }

    protected Object deserialize(CodedInputStream in, Class<?> clazz) throws IOException {
        if (in.readBool()) {
            return null;
        } else {
            Object value;
            if (clazz == Integer.class) {
                value = in.readSInt32();
            } else if (clazz == Long.class) {
                value = in.readSInt64();
            } else if (clazz == Boolean.class) {
                value = in.readBool();
            } else if (clazz == Byte.class) {
                value = in.readRawByte();
            } else if (clazz == Character.class) {
                value = (char) in.readSInt32();
            } else if (clazz == Short.class) {
                value = (short) in.readSInt32();
            } else if (clazz == Double.class) {
                value = in.readDouble();
            } else if (clazz == Float.class) {
                value = in.readFloat();
            } else if (clazz == String.class) {
                value = in.readString();
            } else if (MessageLite.class.isAssignableFrom(clazz)) {
                try {
                    // 可以考虑缓存一下method
                    Method method = clazz.getDeclaredMethod("newBuilder", null);
                    MessageLite.Builder builder = (MessageLite.Builder) method.invoke(null, null);
                    in.readMessage(builder, null);
                    value = builder.build();
                } catch (Exception e) {
                    throw new IOException(e);
                }
            } else {
                throw new IllegalArgumentException("can't serialize " + clazz);
            }

            return value;
        }
    }
}