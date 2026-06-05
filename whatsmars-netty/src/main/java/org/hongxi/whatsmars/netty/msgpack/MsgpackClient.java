package org.hongxi.whatsmars.netty.msgpack;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessagePack-based client example.
 * <p>
 * Sends {@link UserMessage} objects encoded with MessagePack
 * and receives echoed responses from the server.
 */
public final class MsgpackClient {

    private static final Logger logger = LoggerFactory.getLogger(MsgpackClient.class);

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8091"));

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
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
                     p.addLast(new MsgpackClientHandler());
                 }
             });

            logger.info("Connecting to {}:{}...", HOST, PORT);
            b.connect(HOST, PORT).sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
