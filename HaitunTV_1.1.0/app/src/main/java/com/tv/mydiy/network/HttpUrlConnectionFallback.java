package com.tv.mydiy.network;

import android.os.Build;

import com.tv.mydiy.util.LogUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class HttpUrlConnectionFallback {
    private static final String TAG = "HttpUrlConnectionFallback";
    private static final int MAX_REDIRECTS = 5;
    
    private static SSLSocketFactory tlsSocketFactory = null;
    
    static {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                tlsSocketFactory = new TLSSocketFactoryCompat(new javax.net.ssl.TrustManager[]{TLSSocketFactoryCompat.getTrustAllManager()});
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Failed to initialize TLS socket factory", e);
        }
    }
    
    public static String fetchUrl(String urlString) throws IOException {
        return fetchUrl(urlString, "UTF-8", 0);
    }
    
    public static String fetchUrl(String urlString, String charset) throws IOException {
        return fetchUrl(urlString, charset, 0);
    }
    
    private static String fetchUrl(String urlString, String charset, int redirectCount) throws IOException {
        if (redirectCount >= MAX_REDIRECTS) {
            throw new IOException("Too many redirects: " + redirectCount);
        }
        
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        
        try {
            LogUtil.d(TAG, "Using HttpURLConnection for: " + urlString + " (redirect: " + redirectCount + ")");
            
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            
            if (urlConnection instanceof HttpsURLConnection && tlsSocketFactory != null) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) urlConnection;
                httpsConnection.setSSLSocketFactory(tlsSocketFactory);
                httpsConnection.setHostnameVerifier((hostname, session) -> true);
                LogUtil.d(TAG, "Applied TLS socket factory to HttpsURLConnection");
            }
            
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(30000);
            urlConnection.setReadTimeout(60000);
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android TV; Android 4.4) AppleWebKit/537.36");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip");
            urlConnection.setUseCaches(false);
            urlConnection.setInstanceFollowRedirects(false);
            
            disableConnectionReuseIfNecessary();
            
            int responseCode = urlConnection.getResponseCode();
            LogUtil.d(TAG, "Response code: " + responseCode + " for " + urlString);
            
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || 
                responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                responseCode == 307 || responseCode == 308) {
                
                String newUrl = urlConnection.getHeaderField("Location");
                if (newUrl != null && !newUrl.isEmpty()) {
                    LogUtil.d(TAG, "Redirecting to: " + newUrl);
                    urlConnection.disconnect();
                    return fetchUrl(newUrl, charset, redirectCount + 1);
                }
            }
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            
            InputStream inputStream = urlConnection.getInputStream();
            
            String contentEncoding = urlConnection.getHeaderField("Content-Encoding");
            if (contentEncoding != null && contentEncoding.contains("gzip")) {
                inputStream = new GZIPInputStream(inputStream);
            }
            
            String detectedCharset = detectCharset(urlConnection, charset);
            reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName(detectedCharset)));
            
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            
            return result.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, "Failed to close reader", e);
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
    
    private static void disableConnectionReuseIfNecessary() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }
    
    private static String detectCharset(HttpURLConnection connection, String defaultCharset) {
        String contentType = connection.getContentType();
        if (contentType != null) {
            for (String part : contentType.split(";")) {
                part = part.trim();
                if (part.toLowerCase().startsWith("charset=")) {
                    return part.substring(8);
                }
            }
        }
        return defaultCharset;
    }
}
