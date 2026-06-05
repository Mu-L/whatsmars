package org.hongxi.whatsmars.netty.http2.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP/2 服务端 Channel 初始化器。
 * <p>
 * 支持两种 HTTP/2 连接方式：
 * <ol>
 *   <li><b>SSL/TLS + ALPN</b>：通过 TLS 扩展自动协商协议（浏览器标准方式）</li>
 *   <li><b>Cleartext h2c Upgrade</b>：明文 HTTP 升级到 HTTP/2（支持 prior knowledge 直接升级）</li>
 * </ol>
 * <p>
 * pipeline 结构对比：
 * <pre>
 * 传统 API: SslHandler → Http2ConnectionHandler(Http2FrameListener)
 * 现代 API: SslHandler → Http2FrameCodec → Http2MultiplexHandler → [子 Channel: Http2ServerHandler]
 * </pre>
 */
public class Http2ServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger logger = LoggerFactory.getLogger(Http2ServerInitializer.class);

    private final SslContext sslCtx;
    private final Http2ServerHandler http2ServerHandler;

    public Http2ServerInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
        this.http2ServerHandler = new Http2ServerHandler();
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        if (sslCtx != null) {
            configureSsl(ch);
        } else {
            configureClearText(ch);
        }
    }

    /**
     * SSL 模式：通过 ALPN 协商 HTTP/2 或 HTTP/1.1。
     */
    private void configureSsl(SocketChannel ch) {
        ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()), new Http2OrHttpHandler());
    }

    /**
     * 明文模式：支持 h2c upgrade 和 prior knowledge。
     * <p>
     * {@link CleartextHttp2ServerUpgradeHandler} 会自动判断：
     * <ul>
     *   <li>如果客户端发送了 HTTP/1.1 Upgrade 请求头 → 执行 h2c 升级</li>
     *   <li>如果客户端直接发送 HTTP/2 连接前言（prior knowledge）→ 直接进入 HTTP/2</li>
     *   <li>否则 → 按普通 HTTP/1.1 处理</li>
     * </ul>
     */
    private void configureClearText(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        HttpServerCodec sourceCodec = new HttpServerCodec();
        HttpServerUpgradeHandler upgradeHandler = new HttpServerUpgradeHandler(sourceCodec, protocol -> {
            if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
                return new Http2ServerUpgradeCodec(
                        Http2FrameCodecBuilder.forServer().build(),
                        new Http2MultiplexHandler(http2ServerHandler));
            }
            return null;
        });

        // CleartextHttp2ServerUpgradeHandler 同时处理 h2c upgrade 和 prior knowledge
        CleartextHttp2ServerUpgradeHandler cleartextHandler =
                new CleartextHttp2ServerUpgradeHandler(sourceCodec, upgradeHandler,
                        new ChannelInitializer<Channel>() {
                            @Override
                            protected void initChannel(Channel ch) {
                                // prior knowledge: 客户端直接发 HTTP/2 连接前言
                                ch.pipeline().addLast(Http2FrameCodecBuilder.forServer().build());
                                ch.pipeline().addLast(new Http2MultiplexHandler(http2ServerHandler));
                            }
                        });

        p.addLast(cleartextHandler);
        p.addLast(new io.netty.channel.SimpleChannelInboundHandler<HttpMessage>() {
            @Override
            protected void channelRead0(io.netty.channel.ChannelHandlerContext ctx, HttpMessage msg) {
                // 走到这里说明没有升级，客户端直接发的 HTTP/1.1
                logger.warn("Directly talking HTTP/1.1 (no upgrade attempted)");
                ChannelPipeline pipeline = ctx.pipeline();
                pipeline.addAfter(ctx.name(), null, new Http1FallbackHandler());
                pipeline.replace(this, null, new HttpObjectAggregator(1024 * 1024));
                ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
            }
        });
    }
}
