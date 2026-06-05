package org.hongxi.whatsmars.netty.http2.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.DefaultHttp2WindowUpdateFrame;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2FrameStream;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.util.CharsetUtil;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * HTTP/2 服务端 Handler，基于 Frame API（现代 API）。
 * <p>
 * 与传统的 {@code Http2ConnectionHandler + Http2FrameListener} 方式不同，
 * Frame API 将 HTTP/2 帧抽象为 {@link Http2HeadersFrame}、{@link Http2DataFrame} 等对象，
 * 配合 {@link io.netty.handler.codec.http2.Http2FrameCodec} 使用，
 * handler 只需继承 {@link ChannelDuplexHandler} 并通过 instanceof 判断帧类型即可。
 * <p>
 * 标记为 @Sharable 是因为它不持有任何可变状态，可以被多个 stream channel 共享。
 */
@Sharable
public class Http2ServerHandler extends ChannelDuplexHandler {

    private static final ByteBuf RESPONSE_BYTES = unreleasableBuffer(
            copiedBuffer("Hello World", CharsetUtil.UTF_8)).asReadOnly();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            onHeadersRead(ctx, (Http2HeadersFrame) msg);
        } else if (msg instanceof Http2DataFrame) {
            onDataRead(ctx, (Http2DataFrame) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 收到 HEADERS 帧时，如果 endOfStream 为 true（如 GET 请求），直接返回响应。
     */
    private static void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers) {
        if (headers.isEndStream()) {
            ByteBuf content = ctx.alloc().buffer();
            content.writeBytes(RESPONSE_BYTES.duplicate());
            ByteBufUtil.writeAscii(content, " - via HTTP/2 Frame API");
            sendResponse(ctx, headers.stream(), content);
        }
    }

    /**
     * 收到 DATA 帧时，如果 endOfStream 为 true（如 POST 请求体结束），返回响应。
     * 同时需要发送 WINDOW_UPDATE 帧进行流控。
     */
    private static void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data) {
        Http2FrameStream stream = data.stream();
        if (data.isEndStream()) {
            sendResponse(ctx, stream, data.content().retain());
        } else {
            data.release();
        }
        // 手动发送 WINDOW_UPDATE 帧，更新流控窗口
        ctx.write(new DefaultHttp2WindowUpdateFrame(data.initialFlowControlledBytes()).stream(stream));
    }

    /**
     * 发送 HTTP/2 响应：先发 HEADERS 帧（状态码 200），再发 DATA 帧（响应体）。
     */
    private static void sendResponse(ChannelHandlerContext ctx, Http2FrameStream stream, ByteBuf payload) {
        Http2Headers headers = new DefaultHttp2Headers().status(OK.codeAsText());
        ctx.write(new DefaultHttp2HeadersFrame(headers).stream(stream));
        ctx.write(new DefaultHttp2DataFrame(payload, true).stream(stream));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
