package org.hongxi.whatsmars.common.serialize;

import java.io.IOException;

/**
 * Created by shenhongxi on 2018/10/7.
 */
public interface Serialization {

	byte[] serialize(Object obj) throws IOException;

	<T> T deserialize(byte[] bytes, Class<T> clz) throws IOException, ClassNotFoundException;
}