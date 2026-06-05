package org.hongxi.whatsmars.netty.http2.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2ResetFrame;
import io.netty.handler.codec.http2.Http2StreamFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * HTTP/2 客户端响应 Handler，处理 stream 级别的帧对象。
 * <p>
 * 在 Multiplex API 下，每个 HTTP/2 stream 是独立的 {@link io.netty.handler.codec.http2.Http2StreamChannel}，
 * 此 handler 被添加到 stream channel 的 pipeline 中，只接收该 stream 的帧：
 * <ul>
 *   <li>{@link Http2HeadersFrame} - 响应头（状态码、headers）</li>
 *   <li>{@link Http2DataFrame} - 响应体数据</li>
 *   <li>{@link Http2ResetFrame} - 流重置（RST_STREAM）</li>
 * </ul>
 */
public class Http2ClientHandler extends SimpleChannelInboundHandler<Http2StreamFrame> {
    private static final Logger logger = LoggerFactory.getLogger(Http2ClientHandler.class);

    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile boolean success;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2StreamFrame msg) {
        if (msg instanceof Http2HeadersFrame) {
            Http2HeadersFrame headers = (Http2HeadersFrame) msg;
            logger.info("Received HEADERS: status={}, endStream={}", headers.headers().status(), headers.isEndStream());
            if (headers.isEndStream()) {
                success = "200".contentEquals(headers.headers().status());
                latch.countDown();
            }
        } else if (msg instanceof Http2DataFrame) {
            Http2DataFrame data = (Http2DataFrame) msg;
            logger.info("Received DATA: {}, endStream={}", data.content().toString(io.netty.util.CharsetUtil.UTF_8), data.isEndStream());
            if (data.isEndStream()) {
                success = true;
                latch.countDown();
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof Http2ResetFrame) {
            Http2ResetFrame reset = (Http2ResetFrame) evt;
            logger.warn("Stream reset by server, errorCode={}", reset.errorCode());
            latch.countDown();
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught", cause);
        latch.countDown();
        ctx.close();
    }

    /**
     * 等待响应完成。
     *
     * @param timeout 超时时间（秒）
     * @return 是否成功收到 200 响应
     */
    public boolean awaitResponse(int timeout) throws InterruptedException {
        latch.await(timeout, TimeUnit.SECONDS);
        return success;
    }
}
