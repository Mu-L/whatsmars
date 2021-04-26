package org.hongxi.whatsmars.boot.sample.webflux.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Created by shenhongxi on 2021/4/22.
 */
@Slf4j
@Order(-1)
@Component
public class LogFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        preHandle(exchange);
        return chain.filter(exchange)
                .doFinally(signalType -> postHandle(exchange));
    }

    private void preHandle(ServerWebExchange exchange) {
        log.info("preHandle");
        exchange.getAttributes().put("test", true);
        throw new RuntimeException("test exception");
    }

    private void postHandle(ServerWebExchange exchange) {
        log.info("postHandle");
    }
}