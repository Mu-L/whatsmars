package org.hongxi.whatsmars.boot.sample.web.filter;

import org.hongxi.whatsmars.boot.sample.web.support.WebUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shenhongxi on 2021/4/25.
 */
@Order(-4)
@Component
public class AccessFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AccessFilter.class);

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        // servletPath 不带 context-path，而 requestURI 带有 context-path
        request.setAttribute(WebUtils.SHOULD_NOT_FILTER_ATTR, shouldNotFilter(request.getServletPath()));
        filterChain.doFilter(request, response);
        log.info("request {} cost {}ms", request.getRequestURI(), System.currentTimeMillis() - start);
    }

    private boolean shouldNotFilter(String path) {
        if (path == null) {
            return false;
        }
        if (path.equals("/health") || path.startsWith("/actuator")
                || path.startsWith("/error")) {
            return true;
        }
        return path.contains(".") && Arrays.stream(WebUtils.EXCLUDE_RESOURCE_PATHS)
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }
}
