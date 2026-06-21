package org.hongxi.whatsmars.boot.sample.webflux.filter;

import org.hongxi.whatsmars.boot.sample.webflux.support.SessionContext;
import org.hongxi.whatsmars.boot.sample.webflux.support.WebUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shenhongxi on 2021/4/29.
 */
@Order(-2)
@Component
public class SessionFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionFilter.class);

    private static final String COOKIE_NAME = "SESSIONID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (exchange.getAttributeOrDefault(WebUtils.SHOULD_NOT_FILTER_ATTR, false)) {
            return chain.filter(exchange);
        }
        List<HttpCookie> httpCookies = exchange.getRequest().getCookies().get(COOKIE_NAME);
        String userId;
        if (!CollectionUtils.isEmpty(httpCookies)) {
            String sessionId = httpCookies.get(0).getValue();
            userId = getUserIdBySessionId(sessionId);

        } else {
            // just for test
            userId = exchange.getRequest().getQueryParams().getFirst("userId");
        }
        if (StringUtils.hasLength(userId)) {
            SessionContext sessionContext = new SessionContext();
            sessionContext.setUserId(userId);
            exchange.getAttributes().put(WebUtils.SESSION_CONTEXT_ATTR, sessionContext);
        }
        return chain.filter(exchange);
    }

    private String getUserIdBySessionId(String sessionId) {
        return null;
    }
}
