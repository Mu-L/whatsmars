package org.hongxi.whatsmars.netty.http2.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP/2 服务端示例，使用 Netty 现代 API（Frame API + Multiplex API）。
 * <p>
 * <b>与传统 API 的区别：</b>
 * <pre>
 * 传统 API（不推荐）：
 *   Http2ConnectionHandler + Http2FrameListener
 *   - 需要实现十几个回调方法（onSettingsRead, onPingRead, onPriorityRead...）
 *   - 手动管理 stream ID
 *   - handler 不可共享（每个连接需要新实例）
 *
 * 现代 API（本示例）：
 *   Http2FrameCodec + Http2MultiplexHandler
 *   - 帧以对象形式传递（Http2HeadersFrame, Http2DataFrame）
 *   - 每个 stream 是独立的子 Channel（类似 HTTP/1.1 的连接抽象）
 *   - handler 可标记 @Sharable 共享
 *   - 代码量大幅减少
 * </pre>
 * <p>
 * 启动后可以通过以下方式测试：
 * <ul>
 *   <li>明文模式：{@code curl --http2 http://127.0.0.1:8080/}</li>
 *   <li>SSL 模式：{@code curl --http2 -k https://127.0.0.1:8443/}</li>
 * </ul>
 *
 * @see Http2ServerHandler      帧处理 handler
 * @see Http2ServerInitializer  pipeline 配置
 * @see Http2OrHttpHandler      ALPN 协议协商
 */
public final class Http2Server {
    private static final Logger logger = LoggerFactory.getLogger(Http2Server.class);

    static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));

    public static void main(String[] args) throws Exception {
        final SslContext sslCtx = buildSslContext();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(group)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new Http2ServerInitializer(sslCtx));

            Channel ch = b.bind(PORT).sync().channel();

            logger.info("HTTP/2 server started (Frame API + Multiplex API).");
            logger.info("Open your HTTP/2-enabled client and navigate to " +
                    (SSL ? "https" : "http") + "://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 构建 SSL 上下文，配置 ALPN 以支持 HTTP/2 协议协商。
     * <p>
     * 优先使用 OpenSSL（通过 netty-tcnative），性能更好；
     * 如果 OpenSSL 不可用则回退到 JDK 内置 SSL。
     */
    private static SslContext buildSslContext() throws Exception {
        if (!SSL) {
            return null;
        }
        SslProvider provider = OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK;
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        return SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                .sslProvider(provider)
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        Protocol.ALPN,
                        SelectorFailureBehavior.NO_ADVERTISE,
                        SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2,
                        ApplicationProtocolNames.HTTP_1_1))
                .build();
    }
}
