package org.hongxi.whatsmars.netty.http2.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP/2 客户端示例，使用 Netty 现代 API（Frame API + Multiplex API）。
 * <p>
 * <b>与传统 API 客户端的区别：</b>
 * <pre>
 * 传统 API：
 *   - 使用 InboundHttp2ToHttpAdapter 将帧适配为 HTTP 对象
 *   - 手动管理 stream ID（从 3 开始，每次 +2）
 *   - 需要 Http2SettingsHandler 等待连接建立
 *
 * 现代 API（本示例）：
 *   - Http2FrameCodec + Http2MultiplexHandler
 *   - 通过 Http2StreamChannelBootstrap 打开 stream（自动分配 stream ID）
 *   - 每个 stream 是独立的 Http2StreamChannel，操作更直观
 * </pre>
 * <p>
 * 使用方式：先启动 {@link org.hongxi.whatsmars.netty.http2.server.Http2Server}，然后运行本客户端。
 * <ul>
 *   <li>明文模式：直接运行（默认连接 8080 端口）</li>
 *   <li>SSL 模式：{@code java -Dssl ... }（连接 8443 端口）</li>
 * </ul>
 */
public final class Http2Client {
    private static final Logger logger = LoggerFactory.getLogger(Http2Client.class);

    static final boolean SSL = System.getProperty("ssl") != null;
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));
    static final String PATH = System.getProperty("path", "/");

    public static void main(String[] args) throws Exception {
        final SslContext sslCtx = buildSslContext();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.remoteAddress(HOST, PORT);
            b.handler(new ChannelInitializer<NioSocketChannel>() {
                @Override
                protected void initChannel(NioSocketChannel ch) {
                    if (sslCtx != null) {
                        ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()));
                    }
                    // 现代 API 核心：Http2FrameCodec + Http2MultiplexHandler
                    ch.pipeline().addLast(Http2FrameCodecBuilder.forClient().build());
                    ch.pipeline().addLast(new Http2MultiplexHandler(new io.netty.channel.ChannelDuplexHandler()));
                }
            });

            // 建立连接
            Channel channel = b.connect().syncUninterruptibly().channel();
            logger.info("Connected to [{}:{}]", HOST, PORT);

            // 通过 Multiplex API 打开一个 stream channel
            Http2StreamChannelBootstrap streamBootstrap = new Http2StreamChannelBootstrap(channel);
            Http2ClientHandler responseHandler = new Http2ClientHandler();
            Http2StreamChannel streamChannel = streamBootstrap.open().syncUninterruptibly().getNow();
            streamChannel.pipeline().addLast(responseHandler);

            // 构造并发送 HTTP/2 GET 请求（HEADERS 帧）
            DefaultHttp2Headers headers = new DefaultHttp2Headers();
            headers.method("GET");
            headers.path(PATH);
            headers.scheme(SSL ? "https" : "http");
            headers.authority(HOST + ':' + PORT);
            Http2HeadersFrame headersFrame = new DefaultHttp2HeadersFrame(headers, true);
            streamChannel.writeAndFlush(headersFrame);
            logger.info("Sent HTTP/2 GET request to {}", PATH);

            // 等待响应
            if (responseHandler.awaitResponse(5)) {
                logger.info("HTTP/2 request completed successfully.");
            } else {
                logger.error("Did not get HTTP/2 response in expected time.");
            }

            channel.close().syncUninterruptibly();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    private static SslContext buildSslContext() throws Exception {
        if (!SSL) {
            return null;
        }
        SslProvider provider = SslProvider.isAlpnSupported(SslProvider.OPENSSL)
                ? SslProvider.OPENSSL : SslProvider.JDK;
        return SslContextBuilder.forClient()
                .sslProvider(provider)
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        Protocol.ALPN,
                        SelectorFailureBehavior.NO_ADVERTISE,
                        SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2,
                        ApplicationProtocolNames.HTTP_1_1))
                .build();
    }
}
