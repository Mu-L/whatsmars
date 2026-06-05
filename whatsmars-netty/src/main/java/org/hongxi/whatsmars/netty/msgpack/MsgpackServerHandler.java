package org.hongxi.whatsmars.netty.msgpack;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server-side handler that receives deserialized {@link UserMessage} objects
 * and echoes them back to the client.
 */
public class MsgpackServerHandler extends SimpleChannelInboundHandler<UserMessage> {

    private static final Logger logger = LoggerFactory.getLogger(MsgpackServerHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, UserMessage msg) {
        logger.info("Received: {}", msg);

        // Echo back the received message
        ctx.writeAndFlush(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught", cause);
        ctx.close();
    }
}
