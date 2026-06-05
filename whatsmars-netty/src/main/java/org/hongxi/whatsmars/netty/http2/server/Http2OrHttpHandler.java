package org.hongxi.whatsmars.netty.http2.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;

/**
 * ALPN 协议协商 Handler。
 * <p>
 * 当 TLS 连接建立后，ALPN 会自动协商协议。如果协商结果是 HTTP/2，
 * 则在 pipeline 中添加 {@link io.netty.handler.codec.http2.Http2FrameCodec}
 * 和 {@link Http2MultiplexHandler}；如果是 HTTP/1.1，则回退到普通 HTTP 处理。
 * <p>
 * 这是现代 HTTP/2 API 的典型用法：
 * <ul>
 *   <li>{@link io.netty.handler.codec.http2.Http2FrameCodec} - 帧编解码（替代传统 Http2ConnectionHandler）</li>
 *   <li>{@link Http2MultiplexHandler} - 多路复用，每个 stream 是独立的子 Channel</li>
 * </ul>
 */
public class Http2OrHttpHandler extends ApplicationProtocolNegotiationHandler {

    private static final int MAX_CONTENT_LENGTH = 1024 * 1024;

    private final Http2ServerHandler http2ServerHandler;

    public Http2OrHttpHandler() {
        super(ApplicationProtocolNames.HTTP_1_1);
        this.http2ServerHandler = new Http2ServerHandler();
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            configureHttp2(ctx.pipeline());
            return;
        }
        if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            configureHttp1(ctx.pipeline());
            return;
        }
        throw new IllegalStateException("unknown protocol: " + protocol);
    }

    /**
     * 配置 HTTP/2 pipeline：FrameCodec + MultiplexHandler。
     * <p>
     * Http2MultiplexHandler 为每个 HTTP/2 stream 创建独立的子 Channel，
     * 子 Channel 的 pipeline 中放置 Http2ServerHandler 来处理帧对象。
     */
    private void configureHttp2(ChannelPipeline pipeline) {
        pipeline.addLast(Http2FrameCodecBuilder.forServer().build());
        pipeline.addLast(new Http2MultiplexHandler(http2ServerHandler));
    }

    /**
     * 配置 HTTP/1.1 pipeline（降级回退）。
     */
    private void configureHttp1(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
        pipeline.addLast(new Http1FallbackHandler());
    }
}
