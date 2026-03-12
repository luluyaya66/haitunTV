package com.tv.mydiy.network;

import android.net.TrafficStats;

import com.tv.mydiy.util.LogUtil;
import com.tv.mydiy.util.LruCacheUtil;
import com.tv.mydiy.util.UiEventUtils;
import com.tv.mydiy.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.io.PushbackInputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Dispatcher;
import okhttp3.ConnectionPool;

public class NetworkManager {
    private static final String TAG = "NetworkManager";
    private static final int CONNECT_TIMEOUT = Constants.NETWORK_CONNECT_TIMEOUT_SECONDS; // 连接超时
    private static final int READ_TIMEOUT = Constants.NETWORK_READ_TIMEOUT_SECONDS;    // 读取超时
    private static final int WRITE_TIMEOUT = Constants.NETWORK_WRITE_TIMEOUT_SECONDS;   // 写入超时
    
    private final ExecutorService executorService;
    private final OkHttpClient okHttpClient;
    private final LruCacheUtil cacheUtil = LruCacheUtil.getInstance(); // LRU缓存工具
    
    public NetworkManager() {
        executorService = Executors.newSingleThreadExecutor();
        
        ConnectionPool connectionPool = new ConnectionPool(Constants.NETWORK_CONNECTION_POOL_MAX_IDLE, Constants.NETWORK_CONNECTION_POOL_MAX_IDLE, TimeUnit.MINUTES);
        Dispatcher dispatcher = new Dispatcher(executorService);
        dispatcher.setMaxRequests(Constants.NETWORK_DISPATCHER_MAX_REQUESTS);
        dispatcher.setMaxRequestsPerHost(Constants.NETWORK_DISPATCHER_MAX_REQUESTS_PER_HOST);
        
        okHttpClient = OkHttpCompatConfig.createCompatBuilder(
                CONNECT_TIMEOUT,
                READ_TIMEOUT,
                WRITE_TIMEOUT,
                connectionPool,
                dispatcher
        ).build();
    }
    
    /**
     * 异步获取URL内容
     * @param urlString URL地址
     * @param callback 回调接口
     */
    public void fetchUrlContentAsync(String urlString, NetworkCallback callback) {
        executorService.execute(() -> {
            // 为网络请求添加标记，解决StrictMode的UntaggedSocketViolation问题
            int threadId = (int) Thread.currentThread().getId();
            TrafficStats.setThreadStatsTag(threadId);
            
            try {
                String content = fetchUrlContent(urlString);
                if (callback != null) {
                    // 确保回调在主线程中执行
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    UiEventUtils.postToUiThread(mainHandler, () -> callback.onSuccess(content));
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "获取URL内容失败: " + urlString, e);
                if (callback != null) {
                    // 确保回调在主线程中执行
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    UiEventUtils.postToUiThread(mainHandler, () -> callback.onError("网络请求失败: " + e.getMessage() + " URL: " + urlString));
                }
            } finally {
                // 清除线程统计标记
                TrafficStats.clearThreadStatsTag();
            }
        });
    }
    
    /**
     * 同步获取URL内容
     * @param urlString URL地址
     * @return 返回获取到的内容
     * @throws IOException 网络异常
     */
    public String fetchUrlContent(String urlString) throws IOException {
        String cachedContent = cacheUtil.get("network_" + urlString, String.class, Constants.NETWORK_CACHE_DURATION_MILLIS);
        if (cachedContent != null) {
            LogUtil.d(TAG, "从缓存中获取URL内容: " + urlString);
            return cachedContent;
        }
        
        LogUtil.d(TAG, "Fetching URL content: " + urlString);
        
        String content = null;
        IOException lastException = null;
        
        if (urlString.startsWith("https://")) {
            try {
                try {
                    content = fetchWithOkHttp(urlString);
                } catch (IOException e) {
                    LogUtil.w(TAG, "OkHttp HTTPS failed, trying HttpURLConnection HTTPS", e);
                    lastException = e;
                    content = HttpUrlConnectionFallback.fetchUrl(urlString);
                }
            } catch (IOException e) {
                LogUtil.w(TAG, "Both HTTPS methods failed, trying HTTP", e);
                lastException = e;
                String httpUrl = "http://" + urlString.substring(8);
                try {
                    content = fetchWithOkHttp(httpUrl);
                } catch (IOException e2) {
                    LogUtil.w(TAG, "OkHttp HTTP failed, trying HttpURLConnection HTTP", e2);
                    content = HttpUrlConnectionFallback.fetchUrl(httpUrl);
                }
            }
        } else {
            try {
                content = fetchWithOkHttp(urlString);
            } catch (IOException e) {
                LogUtil.w(TAG, "OkHttp failed, trying HttpURLConnection", e);
                lastException = e;
                content = HttpUrlConnectionFallback.fetchUrl(urlString);
            }
        }
        
        if (content != null && !content.isEmpty()) {
            cacheUtil.put("network_" + urlString, content);
            return content;
        }
        
        if (lastException != null) {
            throw lastException;
        }
        throw new IOException("Failed to fetch content from URL: " + urlString);
    }
    
    private String fetchWithOkHttp(String urlString) throws IOException {
        Request request = new Request.Builder()
                .url(urlString)
                .addHeader("User-Agent", Constants.USER_AGENT)
                .addHeader("Accept-Encoding", Constants.ACCEPT_ENCODING)
                .build();
        
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("HTTP错误码: " + response.code());
            }
            
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("响应体为空");
            }
            
            String contentEncoding = response.header("Content-Encoding");
            String contentType = response.header("Content-Type");
            String content;
            
            String charset = Constants.DEFAULT_CHARSET;
            if (contentType != null) {
                java.util.regex.Pattern charsetPattern = java.util.regex.Pattern.compile("charset=([^;]+)");
                java.util.regex.Matcher matcher = charsetPattern.matcher(contentType);
                if (matcher.find()) {
                    charset = Objects.requireNonNull(matcher.group(1)).trim();
                }
            }
            
            LogUtil.d(TAG, "Content-Encoding: " + contentEncoding + ", Content-Type: " + contentType);
            
            byte[] bytes = responseBody.bytes();
            if ((contentEncoding != null && contentEncoding.contains("gzip")) || isGzipStream(bytes)) {
                LogUtil.d(TAG, "Decompressing gzip content");
                java.io.ByteArrayInputStream bais = null;
                java.io.InputStream decompressedStream = null;
                java.io.ByteArrayOutputStream baos = null;
                try {
                    bais = new java.io.ByteArrayInputStream(bytes);
                    decompressedStream = handleNestedGzip(bais, urlString);
                    baos = new java.io.ByteArrayOutputStream();
                    byte[] buffer = new byte[Constants.BUFFER_SIZE];
                    int len;
                    while ((len = decompressedStream.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }
                    content = baos.toString(charset);
                } finally {
                    com.tv.mydiy.util.LruCacheUtil.closeQuietly(baos);
                    com.tv.mydiy.util.LruCacheUtil.closeQuietly(decompressedStream);
                    com.tv.mydiy.util.LruCacheUtil.closeQuietly(bais);
                }
            } else {
                content = new String(bytes, java.nio.charset.Charset.forName(charset));
            }
            
            return content;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
    
    /**
     * 检查字节流是否为gzip格式
     * @param bytes 字节流
     * @return 是否为gzip格式
     */
    private boolean isGzipStream(byte[] bytes) {
        // GZIP文件头部为0x1f8b
        return bytes.length > 2 && bytes[0] == (byte) 0x1f && bytes[1] == (byte) 0x8b;
    }
    
    /**
     * 处理可能的嵌套GZ压缩
     * @param inputStream 输入流
     * @param url URL地址（用于日志）
     * @return 处理后的输入流
     */
    private InputStream handleNestedGzip(InputStream inputStream, String url) {
        try {
            // 检查是否是嵌套的GZIP文件
            PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream, 2);
            byte[] header = new byte[2];
            int bytesRead = pushbackInputStream.read(header);
            if (bytesRead == 2) {
                pushbackInputStream.unread(header, 0, bytesRead);
                // GZIP文件头部为0x1f8b
                if (header[0] == (byte) 0x1f && header[1] == (byte) 0x8b) {
                    // 是GZIP格式，解压后再检查是否还是GZIP格式（嵌套压缩）
                    GZIPInputStream gzipInputStream = new GZIPInputStream(pushbackInputStream);
                    return handleNestedGzip(gzipInputStream, url + " (nested)");
                }
            }
            return pushbackInputStream;
        } catch (Exception e) {
            LogUtil.w(TAG, "Error handling nested GZIP, using original stream: " + e.getMessage());
            return inputStream;
        }
    }

    /**
     * 网络请求回调接口
     */
    public interface NetworkCallback {
        void onSuccess(String content);
        void onError(String error);
    }

}