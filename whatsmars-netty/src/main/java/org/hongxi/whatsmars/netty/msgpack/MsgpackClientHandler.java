package org.hongxi.whatsmars.netty.msgpack;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side handler that sends {@link UserMessage} objects when the connection
 * is established and logs the echoed response from the server.
 */
public class MsgpackClientHandler extends SimpleChannelInboundHandler<UserMessage> {

    private static final Logger logger = LoggerFactory.getLogger(MsgpackClientHandler.class);

    private static final UserMessage[] MESSAGES = {
            new UserMessage(1L, "Alice", "alice@example.com", 28),
            new UserMessage(2L, "Bob", "bob@example.com", 32),
            new UserMessage(3L, "Charlie", "charlie@example.com", 25)
    };

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        for (UserMessage msg : MESSAGES) {
            logger.info("Sending: {}", msg);
            ctx.write(msg);
        }
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, UserMessage msg) {
        logger.info("Received echo: {}", msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught", cause);
        ctx.close();
    }
}
