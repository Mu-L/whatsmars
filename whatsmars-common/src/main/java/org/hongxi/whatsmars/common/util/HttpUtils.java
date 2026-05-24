package org.hongxi.whatsmars.common.util;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.InputStreamEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.Timeout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    private static final Timeout DEFAULT_CONNECT_TIMEOUT = Timeout.ofSeconds(30);
    private static final Timeout DEFAULT_CONNECTION_REQUEST_TIMEOUT = Timeout.ofSeconds(30);

    private static final CloseableHttpClient httpClient = createHttpClient();

    private static CloseableHttpClient createHttpClient() {
        // Build connection config
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .build();

        // Build connection manager
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        // Build request config
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(DEFAULT_CONNECTION_REQUEST_TIMEOUT)
                .build();

        // Build HTTP client
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    // ==================== GET Request ====================

    /**
     * Execute GET request and return response as String
     */
    public static String get(String url) {
        return get(url, null, null);
    }

    /**
     * Execute GET request with query parameters
     */
    public static String get(String url, Map<String, String> params) {
        return get(url, params, null);
    }

    /**
     * Execute GET request with query parameters and headers
     */
    public static String get(String url, Map<String, String> params, Map<String, String> headers) {
        String fullUrl = buildUrl(url, params);
        HttpGet request = new HttpGet(fullUrl);
        addHeaders(request, headers);
        return execute(request);
    }

    /**
     * Execute GET request and return response as byte array
     */
    public static byte[] getAsBytes(String url, Map<String, String> params, Map<String, String> headers) {
        String fullUrl = buildUrl(url, params);
        HttpGet request = new HttpGet(fullUrl);
        addHeaders(request, headers);
        return executeAsBytes(request);
    }

    /**
     * Execute GET request and return InputStream for streaming response
     */
    public static InputStream getAsStream(String url, Map<String, String> params, Map<String, String> headers) {
        String fullUrl = buildUrl(url, params);
        HttpGet request = new HttpGet(fullUrl);
        addHeaders(request, headers);
        return executeAsStream(request);
    }

    // ==================== POST Request ====================

    /**
     * Execute POST request with form data
     */
    public static String post(String url, Map<String, String> formData) {
        return post(url, formData, null);
    }

    /**
     * Execute POST request with form data and headers
     */
    public static String post(String url, Map<String, String> formData, Map<String, String> headers) {
        HttpPost request = new HttpPost(url);
        addHeaders(request, headers);
        if (formData != null && !formData.isEmpty()) {
            List<NameValuePair> pairs = buildNameValuePairs(formData);
            request.setEntity(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
        }
        return execute(request);
    }

    /**
     * Execute POST request with JSON body
     */
    public static String postJson(String url, String jsonBody) {
        return postJson(url, jsonBody, null);
    }

    /**
     * Execute POST request with JSON body and headers
     */
    public static String postJson(String url, String jsonBody, Map<String, String> headers) {
        HttpPost request = new HttpPost(url);
        addHeaders(request, headers);
        request.setHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        return execute(request);
    }

    /**
     * Execute POST request with raw body
     */
    public static String postBody(String url, String body, ContentType contentType) {
        return postBody(url, body, contentType, null);
    }

    /**
     * Execute POST request with raw body and headers
     */
    public static String postBody(String url, String body, ContentType contentType, Map<String, String> headers) {
        HttpPost request = new HttpPost(url);
        addHeaders(request, headers);
        request.setEntity(new StringEntity(body, contentType));
        return execute(request);
    }

    /**
     * Execute POST request with InputStream body
     */
    public static String postStream(String url, InputStream inputStream, ContentType contentType) {
        return postStream(url, inputStream, contentType, null);
    }

    /**
     * Execute POST request with InputStream body and headers
     */
    public static String postStream(String url, InputStream inputStream, ContentType contentType, Map<String, String> headers) {
        HttpPost request = new HttpPost(url);
        addHeaders(request, headers);
        request.setEntity(new InputStreamEntity(inputStream, contentType));
        return execute(request);
    }

    // ==================== Multipart Upload ====================

    /**
     * Upload files via multipart/form-data
     */
    public static String upload(String url, Map<String, Object> parts) {
        return upload(url, parts, null);
    }

    /**
     * Upload files via multipart/form-data with headers
     */
    public static String upload(String url, Map<String, Object> parts, Map<String, String> headers) {
        HttpPost request = new HttpPost(url);
        addHeaders(request, headers);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(StandardCharsets.UTF_8);

        if (parts != null) {
            for (Map.Entry<String, Object> entry : parts.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value instanceof String) {
                    builder.addTextBody(key, (String) value, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8));
                } else if (value instanceof byte[]) {
                    builder.addBinaryBody(key, (byte[]) value);
                } else if (value instanceof File) {
                    builder.addBinaryBody(key, (File) value);
                } else if (value instanceof InputStream) {
                    builder.addBinaryBody(key, (InputStream) value);
                } else if (value != null) {
                    builder.addTextBody(key, String.valueOf(value), ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8));
                }
            }
        }

        request.setEntity(builder.build());
        return execute(request);
    }

    // ==================== Download ====================

    /**
     * Download file to output stream
     */
    public static void download(String url, OutputStream outputStream) {
        download(url, null, outputStream);
    }

    /**
     * Download file to output stream with parameters
     */
    public static void download(String url, Map<String, String> params, OutputStream outputStream) {
        String fullUrl = buildUrl(url, params);
        HttpGet request = new HttpGet(fullUrl);

        try {
            httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                if (statusCode < 200 || statusCode >= 300) {
                    throw new HttpException("HTTP error: " + statusCode);
                }

                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new HttpException("Response entity is null");
                }

                try (InputStream inputStream = entity.getContent()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                } finally {
                    EntityUtils.consume(entity);
                }
                return null;
            });
        } catch (IOException e) {
            throw new HttpException("Failed to download: " + e.getMessage(), e);
        }
    }

    // ==================== Core Methods ====================

    /**
     * Execute request and return response as String
     */
    public static String execute(HttpUriRequest request) {
        try {
            return httpClient.execute(request, response -> {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                }
                String result = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                EntityUtils.consume(entity);
                return result;
            });
        } catch (IOException e) {
            throw new HttpException("Request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Execute request and return response as byte array
     */
    public static byte[] executeAsBytes(HttpUriRequest request) {
        try {
            return httpClient.execute(request, response -> {
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    return null;
                }
                byte[] result = EntityUtils.toByteArray(entity);
                EntityUtils.consume(entity);
                return result;
            });
        } catch (IOException e) {
            throw new HttpException("Request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Execute request and return response as InputStream
     */
    public static InputStream executeAsStream(HttpUriRequest request) {
        try {
            // 使用executeOpen获取响应，这样可以保持连接打开直到流被关闭
            ClassicHttpResponse response = httpClient.executeOpen(null, request, null);
            int statusCode = response.getCode();
            if (statusCode < 200 || statusCode >= 300) {
                EntityUtils.consume(response.getEntity());
                throw new HttpException("HTTP error: " + statusCode);
            }
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // 返回一个包装流，在关闭时同时关闭响应
                return new InputStreamWrapper(entity.getContent(), response);
            }
            return null;
        } catch (IOException e) {
            throw new HttpException("Request failed: " + e.getMessage(), e);
        }
    }

    /**
     * 输入流包装类，用于在关闭流时同时关闭HTTP响应
     */
    private static class InputStreamWrapper extends InputStream {
        private final InputStream delegate;
        private final ClassicHttpResponse response;
        private volatile boolean closed = false;

        public InputStreamWrapper(InputStream delegate, ClassicHttpResponse response) {
            this.delegate = delegate;
            this.response = response;
        }

        @Override
        public int read() throws IOException {
            ensureOpen();
            return delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            ensureOpen();
            return delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            ensureOpen();
            return delegate.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            ensureOpen();
            return delegate.skip(n);
        }

        @Override
        public int available() throws IOException {
            ensureOpen();
            return delegate.available();
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            closed = true;
            try {
                delegate.close();
            } finally {
                try {
                    response.close();
                } catch (IOException e) {
                    // 忽略关闭响应时的异常
                }
            }
        }

        @Override
        public synchronized void mark(int readlimit) {
            delegate.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            ensureOpen();
            delegate.reset();
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }

        private void ensureOpen() throws IOException {
            if (closed) {
                throw new IOException("Stream already closed");
            }
        }
    }

    // ==================== Utility Methods ====================

    private static void addHeaders(HttpUriRequest request, Map<String, String> headers) {
        // Add custom headers
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    private static List<NameValuePair> buildNameValuePairs(Map<String, String> data) {
        List<NameValuePair> pairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        return pairs;
    }

    private static String buildUrl(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }

        StringBuilder sb = new StringBuilder(url);
        boolean hasQuery = url.contains("?");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                sb.append(hasQuery ? "&" : "?");
                sb.append(encode(entry.getKey()));
                sb.append("=");
                sb.append(encode(entry.getValue()));
                hasQuery = true;
            }
        }
        return sb.toString();
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    // ==================== HTTP Exception ====================

    public static class HttpException extends RuntimeException {
        public HttpException(String message) {
            super(message);
        }

        public HttpException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
