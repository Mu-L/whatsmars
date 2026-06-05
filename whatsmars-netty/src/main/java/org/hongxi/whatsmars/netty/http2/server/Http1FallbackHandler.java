package org.hongxi.whatsmars.netty.http2.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * HTTP/1.1 降级回退 Handler。
 * <p>
 * 当客户端不支持 HTTP/2（未进行 h2c upgrade 或 ALPN 协商为 HTTP/1.1）时，
 * 使用此 Handler 以普通 HTTP/1.1 方式处理请求。
 */
public class Http1FallbackHandler extends SimpleChannelInboundHandler<HttpMessage> {

    private static final byte[] CONTENT = "Hello World - via HTTP/1.1 (no HTTP/2 upgrade)".getBytes(CharsetUtil.UTF_8);

    private HttpMessage currentRequest;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpMessage msg) {
        currentRequest = msg;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(CONTENT));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        if (currentRequest != null && HttpUtil.isKeepAlive(currentRequest)) {
            ctx.write(response);
        } else {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
