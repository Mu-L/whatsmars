package org.hongxi.whatsmars.common.serialize;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;

import java.io.IOException;

/**
 * fastjson 序列化
 * 
 * <pre>
 * 对于嵌套场景无法支持
 * </pre>
 *
 * @author hongxi
 */
public class FastJson2Serialization implements Serialization {

    private final Fastjson2SecurityFilter securityFilter = new Fastjson2SecurityFilter();

    @Override
    public byte[] serialize(Object data) throws IOException {
        return JSONB.toBytes(
                data,
                JSONWriter.Feature.WriteClassName,
                JSONWriter.Feature.FieldBased,
                JSONWriter.Feature.ErrorOnNoneSerializable,
                JSONWriter.Feature.ReferenceDetection,
                JSONWriter.Feature.WriteNulls,
                JSONWriter.Feature.NotWriteDefaultValue,
                JSONWriter.Feature.NotWriteHashMapArrayListClassName,
                JSONWriter.Feature.WriteNameAsSymbol);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws IOException {
        return JSONB.parseObject(
                data,
                clazz,
                securityFilter,
                JSONReader.Feature.UseDefaultConstructorAsPossible,
                JSONReader.Feature.ErrorOnNoneSerializable,
                JSONReader.Feature.IgnoreAutoTypeNotMatch,
                JSONReader.Feature.UseNativeObject,
                JSONReader.Feature.FieldBased);
    }

    /**
     * 获取安全过滤器，可用于配置白名单/黑名单或切换检查模式。
     *
     * @return 安全过滤器实例
     */
    public Fastjson2SecurityFilter getSecurityFilter() {
        return securityFilter;
    }

}