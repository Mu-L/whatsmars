package org.hongxi.whatsmars.sentinel.httpclient.controller;

import com.alibaba.csp.sentinel.adapter.apache.httpclient5.SentinelApacheHttpClient5Handler;
import com.alibaba.csp.sentinel.adapter.apache.httpclient5.config.SentinelApacheHttpClientConfig;
import com.alibaba.csp.sentinel.adapter.apache.httpclient5.extractor.ApacheHttpClientResourceExtractor;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.ChainElement;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

/**
 * @author shen.hongxi
 */
@RestController
@RequestMapping("/httpclient")
public class ApacheHttpClientTestController {

    @Value("${server.port:8080}")
    private Integer port;

    @RequestMapping("/back")
    public String back() {
        System.out.println("back");
        return "Welcome Back!";
    }

    @RequestMapping("/back/{id}")
    public String back(@PathVariable String id) {
        System.out.println("back");
        return "Welcome Back! " + id;
    }

    @RequestMapping("/sync")
    public String sync() throws Exception {
        CloseableHttpClient httpclient = HttpClients.custom()
                .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), "sentinel",
                    new SentinelApacheHttpClient5Handler())
                .build();

        String url = "http://localhost:" + port + "/httpclient/back";
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpClient client = httpclient) {
            return client.execute(httpGet, response -> response.getEntity() != null ?
                EntityUtils.toString(response.getEntity()) : null);
        }
    }

    @RequestMapping("/sync/{id}")
    public String sync(@PathVariable String id) throws Exception {
        SentinelApacheHttpClientConfig config = new SentinelApacheHttpClientConfig();
        config.setExtractor(new ApacheHttpClientResourceExtractor() {
            @Override
            public String extractor(ClassicHttpRequest request) {
                String contains = "/httpclient/back/";
                String uri;
                try {
                    uri = request.getUri().toString();
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                if (uri.contains(contains)) {
                    uri = uri.substring(0, uri.indexOf(contains) + contains.length()) + "{id}";
                }
                return request.getMethod() + ":" + uri;
            }
        });
        CloseableHttpClient httpclient = HttpClients.custom()
                .addExecInterceptorBefore(ChainElement.MAIN_TRANSPORT.name(), "sentinel",
                    new SentinelApacheHttpClient5Handler(config))
                .build();
        String url = "http://localhost:" + port + "/httpclient/back/" + id;
        HttpGet httpGet = new HttpGet(url);
        try (CloseableHttpClient client = httpclient) {
            return client.execute(httpGet, response -> response.getEntity() != null ?
                EntityUtils.toString(response.getEntity()) : null);
        }
    }
}
