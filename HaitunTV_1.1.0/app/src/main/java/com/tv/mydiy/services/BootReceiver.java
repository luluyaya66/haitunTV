package com.tv.mydiy.services;

import com.tv.mydiy.network.NetworkStateReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.tv.mydiy.settings.SettingsManager;
import com.tv.mydiy.ui.MainActivity;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device boot completed, checking network...");
            
            // 检查用户是否启用了开机自启功能
            SettingsManager settingsManager = new SettingsManager(context);
            boolean isAutoStartEnabled = settingsManager.isAutoStart();
            
            if (!isAutoStartEnabled) {
                Log.d(TAG, "开机自启功能未启用，不启动服务");
                return;
            }

            // Check network connectivity before syncing
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                Log.d(TAG, "Network available, starting main activity...");
                // 启动主Activity，真正启动应用
                Intent startIntent = new Intent(context, MainActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startIntent.setAction(Intent.ACTION_MAIN);
                startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                
                try {
                    context.startActivity(startIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to start main activity: " + e.getMessage());
                    // 尝试使用不同的启动方式
                    startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(startIntent);
                }
            } else {
                Log.w(TAG, "Network not available, will retry when connected");
                
                // 发送广播，当网络连接恢复时NetworkStateReceiver会处理重试逻辑
                Intent networkIntent = new Intent(NetworkStateReceiver.ACTION_NETWORK_RETRY);
                networkIntent.setClass(context, NetworkStateReceiver.class);
                context.sendBroadcast(networkIntent);
            }
        }
    }
}