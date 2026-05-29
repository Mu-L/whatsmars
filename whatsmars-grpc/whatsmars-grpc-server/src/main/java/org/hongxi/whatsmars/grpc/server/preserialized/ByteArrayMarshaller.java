package org.hongxi.whatsmars.grpc.server.preserialized;

import com.google.common.io.ByteStreams;
import io.grpc.MethodDescriptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A marshaller that produces a byte[] instead of decoding into typical POJOs. It can be used for
 * any message type.
 */
final class ByteArrayMarshaller implements MethodDescriptor.Marshaller<byte[]> {
    @Override
    public byte[] parse(InputStream stream) {
        try {
            return ByteStreams.toByteArray(stream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public InputStream stream(byte[] b) {
        return new ByteArrayInputStream(b);
    }
}