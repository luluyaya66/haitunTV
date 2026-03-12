package com.tv.mydiy.ui;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import com.tv.mydiy.util.LogUtil;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

public class MyTVApplication extends Application {
    private static final String TAG = "MyTVApplication";
    
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();

        initializeApp();
        initializeNetworkOptimization();
    }
    
    private void initializeApp() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LogUtil.e(TAG, "Unhandled exception in thread: " + thread.getName(), throwable);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
            }
            System.exit(1);
        });
    }
    
    private void initializeNetworkOptimization() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            LogUtil.d(TAG, "Applying network optimizations for Android 4");
            enableStrictMode();
        }
    }
    
    private void enableStrictMode() {
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll()
                    .build();
            StrictMode.setThreadPolicy(policy);
        } catch (Exception e) {
            LogUtil.w(TAG, "Failed to set StrictMode", e);
        }
    }
}