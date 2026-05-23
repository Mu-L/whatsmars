package org.hongxi.whatsmars.common.util;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HttpUtils {

    private static final CloseableHttpClient httpClient;
    public static final String CHARSET = "UTF-8";

    static {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(6000, TimeUnit.MILLISECONDS)
                .setConnectionRequestTimeout(6000, TimeUnit.MILLISECONDS)
                .build();
        httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .build();
    }

    public static String httpGet(String url) throws Exception {
        return httpGet(url, null);
    }

    public static String httpGet(String url, Map<String, String> params) throws Exception {
        return httpGet(url, params, CHARSET);
    }

    public static String httpGet(String url, Map<String, String> params, String charset) throws Exception {
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> pairs = new ArrayList<>(params.size());
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String value = entry.getValue();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            String queryString = EntityUtils.toString(new UrlEncodedFormEntity(pairs, Charset.forName(charset)));
            if (url.indexOf("?") > 0) {
                url += "&" + queryString;
            } else {
                url += "?" + queryString;
            }

        }
        HttpGet httpGet = new HttpGet(url);
        return httpClient.execute(httpGet, response -> {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                httpGet.cancel();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Charset.forName(charset));
            }
            EntityUtils.consume(entity);
            return result;
        });
    }

    public static String httpPost(String url, HttpEntity requestEntity) throws Exception {
        return httpPost(url, null, requestEntity);
    }

    public static String httpPost(String url, Map<String, String> params, HttpEntity requestEntity) throws Exception {
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> pairs = new ArrayList<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String value = entry.getValue();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            String queryString = EntityUtils.toString(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
            if (url.indexOf("?") > 0) {
                url += "&" + queryString;
            } else {
                url += "?" + queryString;
            }
        }
        HttpPost httpPost = new HttpPost(url);
        if (requestEntity != null) {
            httpPost.setEntity(requestEntity);
        }
        return httpClient.execute(httpPost, response -> {
            int statusCode = response.getCode();
            if (statusCode != 200) {
                httpPost.cancel();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            HttpEntity entity = response.getEntity();
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }
            EntityUtils.consume(entity);
            return result;
        });
    }

    public static String postBodyAsStream(String url, InputStream inputStream, String encoding) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        HttpEntity body = new InputStreamEntity(inputStream, ContentType.APPLICATION_OCTET_STREAM);
        httpPost.setEntity(body);
        return httpClient.execute(httpPost, response -> {
            HttpEntity entity = response.getEntity();
            int statusCode = response.getCode();
            if (statusCode != 200) {
                httpPost.cancel();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, Charset.forName(encoding));
            }
            EntityUtils.consume(entity);
            return result;
        });
    }

    public static String postBodyAsMultipart(String url, Map<String, Object> contentBodies) throws Exception {
        return postBodyAsMultipart(url, contentBodies, CHARSET);
    }

    public static String postBodyAsMultipart(String url, Map<String, Object> contentBodies, String charset) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder mb = MultipartEntityBuilder.create();
        mb.setCharset(Charset.forName(charset));
        for (Map.Entry<String, Object> entry : contentBodies.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                mb.addTextBody(entry.getKey(), (String) value, ContentType.TEXT_PLAIN.withCharset(Charset.forName(charset)));
            } else if (value instanceof byte[]) {
                mb.addBinaryBody(entry.getKey(), (byte[]) value);
            } else if (value instanceof InputStream) {
                mb.addBinaryBody(entry.getKey(), (InputStream) value);
            } else if (value instanceof File) {
                mb.addBinaryBody(entry.getKey(), (File) value);
            } else {
                mb.addTextBody(entry.getKey(), String.valueOf(value), ContentType.TEXT_PLAIN.withCharset(Charset.forName(charset)));
            }
        }
        httpPost.setEntity(mb.build());
        return httpClient.execute(httpPost, response -> {
            HttpEntity entity = response.getEntity();
            int statusCode = response.getCode();
            if (statusCode != 200) {
                httpPost.cancel();
                throw new RuntimeException("HttpClient,error status code :" + statusCode);
            }
            String result = null;
            if (entity != null) {
                result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }
            EntityUtils.consume(entity);
            return result;
        });
    }

    public static InputStream httpGetStream(String url, Map<String, String> params) throws Exception {
        if (params != null && !params.isEmpty()) {
            List<NameValuePair> pairs = new ArrayList<>(params.size());
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String value = entry.getValue();
                if (value != null) {
                    pairs.add(new BasicNameValuePair(entry.getKey(), value));
                }
            }
            String queryString = EntityUtils.toString(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
            if (url.indexOf("?") > 0) {
                url += "&" + queryString;
            } else {
                url += "?" + queryString;
            }

        }
        HttpGet httpGet = new HttpGet(url);
        ClassicHttpResponse response = httpClient.execute(httpGet);
        int statusCode = response.getCode();
        if (statusCode != 200) {
            httpGet.cancel();
            response.close();
            throw new RuntimeException("HttpClient,error status code :" + statusCode);
        }
        HttpEntity entity = response.getEntity();
        if (entity != null && entity.isStreaming()) {
            return entity.getContent();
        }
        response.close();
        return null;
    }

    public static String buildUrl(String url, Map<String, String> params, String charset) {
        try {
            if (params != null && !params.isEmpty()) {
                List<NameValuePair> pairs = new ArrayList<>(params.size());
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    String value = entry.getValue();
                    if (value != null) {
                        pairs.add(new BasicNameValuePair(entry.getKey(), value));
                    }
                }
                String queryString = EntityUtils.toString(new UrlEncodedFormEntity(pairs, Charset.forName(charset)));
                if (url.indexOf("?") > 0) {
                    url += "&" + queryString;
                } else {
                    url += "?" + queryString;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return url;
    }
}
