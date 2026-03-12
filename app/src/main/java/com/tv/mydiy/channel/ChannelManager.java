package com.tv.mydiy.channel;

import android.content.Context;
import android.util.Log;

import com.tv.mydiy.network.ChannelParserManager;
import com.tv.mydiy.network.NetworkManager;
import com.tv.mydiy.settings.SettingsManager;

import java.util.List;

public class ChannelManager {
    private static final String TAG = "ChannelManager";
    
    private final ChannelParserManager channelParserManager;
    
    public ChannelManager(NetworkManager networkManager) {
        this.channelParserManager = new ChannelParserManager(networkManager);
    }
    
    /**
     * 加载频道列表
     * @param context 上下文
     * @param settingsManager 设置管理器
     * @param callback 回调接口
     */
    public void loadChannelList(Context context, SettingsManager settingsManager, ChannelLoadCallback callback) {
        Log.d(TAG, "开始加载频道列表");
        
        // 检查是否使用了自定义URL
        boolean useCustom = settingsManager.getSharedPreferences().getBoolean(SettingsManager.KEY_USE_CUSTOM_URL, false);
        if (useCustom) {
            // 直接从自定义URL加载频道列表（不是频道源清单）
            String customUrl = settingsManager.getCurrentChannelUrl(context);
            if (customUrl != null && !customUrl.isEmpty()) {
                channelParserManager.parseFromUrlAsync(customUrl, new ChannelParserManager.ChannelParserCallback() {
                    @Override
                    public void onSuccess(List<ChannelGroup> groups) {
                        if (callback != null) {
                            callback.onSuccess(groups);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        if (callback != null) {
                            callback.onError(error);
                        }
                    }
                });
            } else {
                Log.e(TAG, "Custom URL is null or empty");
                if (callback != null) {
                    callback.onError("自定义URL为空");
                }
            }
        } else {
            // 使用默认逻辑，从频道源清单加载频道列表
            String defaultChannelUrl = context.getString(com.tv.mydiy.R.string.default_channel_url);
            
            if (!defaultChannelUrl.isEmpty()) {
                channelParserManager.parseFromMainUrlWithFallbackAsync(defaultChannelUrl, new ChannelParserManager.ChannelParserCallback() {
                    @Override
                    public void onSuccess(List<ChannelGroup> groups) {
                        if (callback != null) {
                            callback.onSuccess(groups);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        if (callback != null) {
                            callback.onError(error);
                        }
                    }
                });
            } else {
                Log.e(TAG, "Default channel URL is null or empty");
                if (callback != null) {
                    callback.onError("默认频道URL为空");
                }
            }
        }
    }
    
    /**
     * 频道加载回调接口
     */
    public interface ChannelLoadCallback {
        void onSuccess(List<ChannelGroup> channelGroups);
        void onError(String error);
    }
}