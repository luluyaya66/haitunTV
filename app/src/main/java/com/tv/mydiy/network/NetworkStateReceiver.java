package com.tv.mydiy.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.tv.mydiy.services.DataSyncService;

/**
 * 网络状态变化接收器
 * 监听网络连接状态变化
 */
public class NetworkStateReceiver extends BroadcastReceiver {
    
    private static final String TAG = "NetworkStateReceiver";
    public static final String ACTION_NETWORK_RETRY = "network_retry_action";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                        Log.i(TAG, "Network connected, starting deferred services...");
                        
                        // 网络连接恢复，启动数据同步服务
                        Intent syncIntent = new Intent(context, DataSyncService.class);
                        syncIntent.setAction(DataSyncService.ACTION_SYNC_CHANNELS_ONLY);
                        
                        // Android 8.0+ 需要使用startForegroundService启动前台服务
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(syncIntent);
                        } else {
                            context.startService(syncIntent);
                        }
                    } else {
                        Log.i(TAG, "Network disconnected");
                        // 可以在这里处理网络断开的逻辑
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error handling network state change: " + e.getMessage());
            }
        } else if (ACTION_NETWORK_RETRY.equals(intent.getAction())) {
            // 处理重试请求 - 检查是否启用了开机自启，然后启动主应用
            Log.d(TAG, "Received network retry request");
            
            // 检查用户是否启用了开机自启功能
            com.tv.mydiy.settings.SettingsManager settingsManager = new com.tv.mydiy.settings.SettingsManager(context);
            boolean isAutoStartEnabled = settingsManager.isAutoStart();
            
            if (!isAutoStartEnabled) {
                Log.d(TAG, "开机自启功能未启用，不启动服务");
                return;
            }
            
            // 检查网络连接
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            
            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                Log.d(TAG, "Network available after retry, starting main activity...");
                // 启动主Activity
                Intent startIntent = new Intent(context, com.tv.mydiy.ui.MainActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startIntent.setAction(Intent.ACTION_MAIN);
                startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                
                try {
                    context.startActivity(startIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to start main activity after retry: " + e.getMessage());
                    // 尝试使用不同的启动方式
                    startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(startIntent);
                }
            } else {
                Log.w(TAG, "Network still not available after retry");
            }
        }
    }
}