package org.hongxi.whatsmars.netty.msgpack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

/**
 * Custom Netty encoder that serializes {@link UserMessage} into MessagePack binary format.
 * <p>
 * This encoder converts a {@link UserMessage} POJO into a compact binary representation
 * using the MessagePack format, which is more efficient than JSON for network transmission.
 * <p>
 * Wire format: [4-byte length prefix][MessagePack payload]
 * <br>
 * The length prefix is handled by {@link io.netty.handler.codec.LengthFieldPrepender}
 * in the pipeline, so this encoder only writes the MessagePack payload.
 */
public class MessagePackEncoder extends MessageToByteEncoder<UserMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, UserMessage msg, ByteBuf out) throws Exception {
        try (MessageBufferPacker packer = MessagePack.newDefaultBufferPacker()) {
            // Pack as a map with 4 entries
            packer.packMapHeader(4);
            packer.packString("id");
            packer.packLong(msg.getId());
            packer.packString("name");
            packer.packString(msg.getName());
            packer.packString("email");
            packer.packString(msg.getEmail());
            packer.packString("age");
            packer.packInt(msg.getAge());

            byte[] bytes = packer.toByteArray();
            out.writeBytes(bytes);
        }
    }
}
