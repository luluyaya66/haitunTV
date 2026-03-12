package com.tv.mydiy.network;

import android.annotation.SuppressLint;
import android.os.Build;

import com.tv.mydiy.util.LogUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TLSSocketFactoryCompat extends SSLSocketFactory {
    private static final String TAG = "TLSSocketFactoryCompat";
    
    private final SSLSocketFactory delegate;
    private final String[] enabledProtocols;
    private final String[] enabledCipherSuites;

    public TLSSocketFactoryCompat() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);
        this.delegate = sslContext.getSocketFactory();
        this.enabledProtocols = getSupportedProtocols();
        this.enabledCipherSuites = getSupportedCipherSuitesInternal();
        LogUtil.d(TAG, "TLSSocketFactoryCompat initialized with protocols: " + Arrays.toString(enabledProtocols));
    }

    public TLSSocketFactoryCompat(TrustManager[] trustManagers) throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, null);
        this.delegate = sslContext.getSocketFactory();
        this.enabledProtocols = getSupportedProtocols();
        this.enabledCipherSuites = getSupportedCipherSuitesInternal();
        LogUtil.d(TAG, "TLSSocketFactoryCompat initialized with trust managers");
    }

    private String[] getSupportedProtocols() {
        List<String> protocols = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            protocols.add("TLSv1");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            protocols.add("TLSv1.1");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            protocols.add("TLSv1.2");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            protocols.add("TLSv1.3");
        }
        
        return protocols.toArray(new String[0]);
    }

    private String[] getSupportedCipherSuitesInternal() {
        List<String> cipherSuites = new ArrayList<>();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            cipherSuites.add("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");
            cipherSuites.add("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA");
            cipherSuites.add("TLS_RSA_WITH_AES_128_CBC_SHA");
            cipherSuites.add("TLS_RSA_WITH_AES_256_CBC_SHA");
            cipherSuites.add("TLS_DHE_RSA_WITH_AES_128_CBC_SHA");
            cipherSuites.add("TLS_DHE_RSA_WITH_AES_256_CBC_SHA");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cipherSuites.add("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
            cipherSuites.add("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384");
        }
        
        return cipherSuites.toArray(new String[0]);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return enabledCipherSuites;
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return enabledCipherSuites;
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        Socket sslSocket = delegate.createSocket(socket, host, port, autoClose);
        enableTLSProtocols(sslSocket, host);
        return sslSocket;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket sslSocket = delegate.createSocket(host, port);
        enableTLSProtocols(sslSocket, host);
        return sslSocket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket sslSocket = delegate.createSocket(host, port, localAddress, localPort);
        enableTLSProtocols(sslSocket, host);
        return sslSocket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket sslSocket = delegate.createSocket(host, port);
        enableTLSProtocols(sslSocket, host.getHostAddress());
        return sslSocket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket sslSocket = delegate.createSocket(host, port, localAddress, localPort);
        enableTLSProtocols(sslSocket, host.getHostAddress());
        return sslSocket;
    }

    private void enableTLSProtocols(Socket socket, String host) {
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            try {
                String[] availableProtocols = sslSocket.getSupportedProtocols();
                String[] availableCipherSuites = sslSocket.getSupportedCipherSuites();
                
                LogUtil.d(TAG, "Connecting to " + host);
                LogUtil.d(TAG, "Available protocols: " + Arrays.toString(availableProtocols));
                LogUtil.d(TAG, "Available cipher suites: " + Arrays.toString(availableCipherSuites));
                
                List<String> validProtocols = new ArrayList<>();
                for (String protocol : enabledProtocols) {
                    for (String available : availableProtocols) {
                        if (protocol.equals(available)) {
                            validProtocols.add(protocol);
                            break;
                        }
                    }
                }
                
                List<String> validCipherSuites = new ArrayList<>();
                for (String suite : enabledCipherSuites) {
                    for (String available : availableCipherSuites) {
                        if (suite.equals(available)) {
                            validCipherSuites.add(suite);
                            break;
                        }
                    }
                }
                
                if (!validProtocols.isEmpty()) {
                    sslSocket.setEnabledProtocols(validProtocols.toArray(new String[0]));
                    LogUtil.d(TAG, "Enabled protocols: " + validProtocols);
                }
                
                if (!validCipherSuites.isEmpty()) {
                    sslSocket.setEnabledCipherSuites(validCipherSuites.toArray(new String[0]));
                    LogUtil.d(TAG, "Enabled cipher suites: " + validCipherSuites);
                }
                
            } catch (Exception e) {
                LogUtil.w(TAG, "Failed to set TLS protocols/ciphers for " + host + ", using defaults", e);
            }
        }
    }

    public static X509TrustManager getTrustAllManager() {
        return new X509TrustManager() {
            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[0];
            }
        };
    }
}
