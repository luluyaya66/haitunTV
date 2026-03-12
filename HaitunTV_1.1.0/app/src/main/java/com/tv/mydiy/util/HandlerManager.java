package com.tv.mydiy.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

/**
 * 统一的Handler管理器，用于替代多个独立的Handler实例
 * 提供内存泄漏防护和统一的生命周期管理
 */
public class HandlerManager {
    
    private final SafeHandler mainHandler;
    private final SafeHandler timeHandler;
    private final SafeHandler infoBarHandler;
    private final SafeHandler exitHandler;
    private final SafeHandler loadingProgressHandler;
    
    /**
     * 构造函数
     * @param lifecycleOwner 拥有生命周期的组件（如Activity）
     */
    public HandlerManager(Object lifecycleOwner) {
        this.mainHandler = new SafeHandler(lifecycleOwner);
        this.timeHandler = new SafeHandler(lifecycleOwner);
        this.infoBarHandler = new SafeHandler(lifecycleOwner);
        this.exitHandler = new SafeHandler(lifecycleOwner);
        this.loadingProgressHandler = new SafeHandler(lifecycleOwner);
    }
    
    // 获取各种专用Handler
    public SafeHandler getMainHandler() { return mainHandler; }
    public SafeHandler getTimeHandler() { return timeHandler; }
    public SafeHandler getInfoBarHandler() { return infoBarHandler; }
    public SafeHandler getExitHandler() { return exitHandler; }
    public SafeHandler getLoadingProgressHandler() { return loadingProgressHandler; }
    
    /**
     * 安全的Handler实现，防止内存泄漏
     */
    public static class SafeHandler extends Handler {
        private final WeakReference<Object> ownerRef;
        
        public SafeHandler(Object lifecycleOwner) {
            super(Looper.getMainLooper());
            this.ownerRef = new WeakReference<>(lifecycleOwner);
        }
        
        @Override
        public void handleMessage(@NonNull Message msg) {
            Object owner = ownerRef.get();
            if (owner != null) {
                super.handleMessage(msg);
            }
        }

    }
    
    /**
     * 释放所有Handler资源
     */
    public void release() {
        mainHandler.removeCallbacksAndMessages(null);
        timeHandler.removeCallbacksAndMessages(null);
        infoBarHandler.removeCallbacksAndMessages(null);
        exitHandler.removeCallbacksAndMessages(null);
        loadingProgressHandler.removeCallbacksAndMessages(null);
    }
}