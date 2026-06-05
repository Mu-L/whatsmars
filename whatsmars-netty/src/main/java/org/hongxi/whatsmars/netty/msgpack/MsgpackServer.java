package org.hongxi.whatsmars.netty.msgpack;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessagePack-based server example.
 * <p>
 * Uses a custom {@link MessagePackEncoder}/{@link MessagePackDecoder} pair
 * with length-field framing to handle message boundaries:
 * <ul>
 *   <li>{@link LengthFieldBasedFrameDecoder} - inbound frame splitting by 4-byte length prefix</li>
 *   <li>{@link MessagePackDecoder} - deserializes MessagePack binary → {@link UserMessage}</li>
 *   <li>{@link LengthFieldPrepender} - prepends 4-byte length to outbound messages</li>
 *   <li>{@link MessagePackEncoder} - serializes {@link UserMessage} → MessagePack binary</li>
 * </ul>
 */
public final class MsgpackServer {

    private static final Logger logger = LoggerFactory.getLogger(MsgpackServer.class);

    static final int PORT = Integer.parseInt(System.getProperty("port", "8091"));

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     // Inbound: frame decoder → MessagePack decoder
                     p.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 4));
                     p.addLast(new MessagePackDecoder());
                     // Outbound: length prepender → MessagePack encoder
                     p.addLast(new LengthFieldPrepender(4));
                     p.addLast(new MessagePackEncoder());
                     // Business handler
                     p.addLast(new MsgpackServerHandler());
                 }
             });

            logger.info("MessagePack server started on port {}", PORT);
            b.bind(PORT).sync().channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
