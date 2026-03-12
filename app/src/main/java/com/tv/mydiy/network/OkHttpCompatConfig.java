package com.tv.mydiy.network;

import android.os.Build;

import com.tv.mydiy.util.Constants;
import com.tv.mydiy.util.LogUtil;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

public class OkHttpCompatConfig {
    private static final String TAG = "OkHttpCompatConfig";
    
    public static OkHttpClient.Builder createCompatBuilder(
            int connectTimeout, 
            int readTimeout, 
            int writeTimeout,
            ConnectionPool connectionPool,
            Dispatcher dispatcher) {
        
        int finalConnectTimeout = connectTimeout;
        int finalReadTimeout = readTimeout;
        int finalWriteTimeout = writeTimeout;
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finalConnectTimeout = Constants.ANDROID_4_CONNECT_TIMEOUT_SECONDS;
            finalReadTimeout = Constants.ANDROID_4_READ_TIMEOUT_SECONDS;
            finalWriteTimeout = Constants.ANDROID_4_WRITE_TIMEOUT_SECONDS;
            LogUtil.d(TAG, "Using Android 4 optimized timeouts");
        }
        
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(finalConnectTimeout, TimeUnit.SECONDS)
                .readTimeout(finalReadTimeout, TimeUnit.SECONDS)
                .writeTimeout(finalWriteTimeout, TimeUnit.SECONDS)
                .connectionPool(connectionPool)
                .dispatcher(dispatcher)
                .retryOnConnectionFailure(true)
                .followRedirects(true)
                .followSslRedirects(true);
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            configureForOldAndroid(builder);
        }
        
        return builder;
    }
    
    private static void configureForOldAndroid(OkHttpClient.Builder builder) {
        try {
            LogUtil.d(TAG, "Configuring OkHttp for Android 4 compatibility");
            
            X509TrustManager trustManager = TLSSocketFactoryCompat.getTrustAllManager();
            TLSSocketFactoryCompat socketFactory = new TLSSocketFactoryCompat(new TrustManager[]{trustManager});
            
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
            
            builder.sslSocketFactory(socketFactory, trustManager);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    LogUtil.d(TAG, "Hostname verification for: " + hostname);
                    return true;
                }
            });
            
            LogUtil.d(TAG, "Successfully configured OkHttp for Android 4");
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LogUtil.e(TAG, "Failed to configure TLS for old Android", e);
        }
    }
}
