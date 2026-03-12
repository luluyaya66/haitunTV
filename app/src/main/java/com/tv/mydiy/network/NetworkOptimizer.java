package com.tv.mydiy.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.tv.mydiy.util.LogUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkOptimizer {
    private static final String TAG = "NetworkOptimizer";
    
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return false;
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                android.net.Network[] networks = cm.getAllNetworks();
                for (android.net.Network network : networks) {
                    NetworkInfo info = cm.getNetworkInfo(network);
                    if (info != null && info.isConnected()) {
                        return true;
                    }
                }
            } else {
                NetworkInfo[] info = cm.getAllNetworkInfo();
                if (info != null) {
                    for (NetworkInfo anInfo : info) {
                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Error checking network availability", e);
        }
        return false;
    }
    
    public static boolean isWifiConnected(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return false;
            }
            
            NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return networkInfo != null && networkInfo.isConnected();
        } catch (Exception e) {
            LogUtil.e(TAG, "Error checking WiFi connection", e);
            return false;
        }
    }
    
    public static boolean isEthernetConnected(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return false;
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
                return networkInfo != null && networkInfo.isConnected();
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Error checking Ethernet connection", e);
        }
        return false;
    }
    
    public static boolean isHostReachable(String host, int timeoutMs) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isReachable(timeoutMs);
        } catch (UnknownHostException e) {
            LogUtil.e(TAG, "Host not found: " + host, e);
            return false;
        } catch (Exception e) {
            LogUtil.e(TAG, "Error checking host reachability: " + host, e);
            return false;
        }
    }
    
    public static String getNetworkType(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return "Unknown";
            }
            
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork == null || !activeNetwork.isConnected()) {
                return "Disconnected";
            }
            
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    return "WiFi";
                case ConnectivityManager.TYPE_MOBILE:
                    return "Mobile";
                case ConnectivityManager.TYPE_ETHERNET:
                    return "Ethernet";
                default:
                    return "Other";
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Error getting network type", e);
            return "Unknown";
        }
    }
}
