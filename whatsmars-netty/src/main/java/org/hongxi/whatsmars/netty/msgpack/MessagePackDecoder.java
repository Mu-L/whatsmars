package org.hongxi.whatsmars.netty.msgpack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import java.util.List;

/**
 * Custom Netty decoder that deserializes MessagePack binary data into {@link UserMessage}.
 * <p>
 * This decoder reads MessagePack-formatted bytes from the inbound {@link ByteBuf}
 * and converts them back into {@link UserMessage} POJO instances.
 * <p>
 * Note: A {@link io.netty.handler.codec.LengthFieldBasedFrameDecoder} must be placed
 * before this decoder in the pipeline to ensure each message is a complete frame.
 */
public class MessagePackDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);

        try (MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(bytes)) {
            UserMessage msg = new UserMessage();

            // Unpack map header
            int mapSize = unpacker.unpackMapHeader();
            for (int i = 0; i < mapSize; i++) {
                String key = unpacker.unpackString();
                switch (key) {
                    case "id" -> msg.setId(unpacker.unpackLong());
                    case "name" -> msg.setName(unpacker.unpackString());
                    case "email" -> msg.setEmail(unpacker.unpackString());
                    case "age" -> msg.setAge(unpacker.unpackInt());
                    default -> unpacker.skipValue();
                }
            }

            out.add(msg);
        }
    }
}
