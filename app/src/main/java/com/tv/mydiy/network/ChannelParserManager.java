package com.tv.mydiy.network;

import android.util.Log;
import com.tv.mydiy.channel.ChannelGroup;
import com.tv.mydiy.channel.ChannelParser;
import java.util.List;

public class ChannelParserManager {
    private static final String TAG = "ChannelParserManager";
    
    private final com.tv.mydiy.network.NetworkManager networkManager;
    
    public ChannelParserManager(com.tv.mydiy.network.NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    public void parseFromUrlAsync(String url, ChannelParserCallback callback) {
        if (networkManager == null) {
            if (callback != null) {
                callback.onError("Network manager is not initialized");
            }
            return;
        }
        
        networkManager.fetchUrlContentAsync(url, new com.tv.mydiy.network.NetworkManager.NetworkCallback() {
            @Override
            public void onSuccess(String content) {
                try {
                    List<ChannelGroup> result = ChannelParser.parseContent(content);
                    Log.d(TAG, "Successfully parsed " + result.size() + " channel groups from " + url);
                    if (callback != null) {
                        callback.onSuccess(result);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing channel list from " + url, e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching channel list from " + url + ": " + error);
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    public void parseFromMainUrlWithFallbackAsync(String mainUrl, ChannelParserCallback callback) {
        if (networkManager == null) {
            if (callback != null) {
                callback.onError("Network manager is not initialized");
            }
            return;
        }
        
        // 首先从主地址获取备用地址列表
        networkManager.fetchUrlContentAsync(mainUrl, new com.tv.mydiy.network.NetworkManager.NetworkCallback() {
            @Override
            public void onSuccess(String content) {
                try {
                    Log.d(TAG, "Received content length: " + (content != null ? content.length() : 0));
                    List<String> playlistUrls = ChannelParser.extractPlaylistUrls(content, mainUrl);
                    Log.d(TAG, "Extracted playlist URLs count: " + playlistUrls.size());
                    
                    if (playlistUrls.isEmpty()) {
                        // 如果没有提取到URL，尝试直接解析主地址的内容
                        Log.w(TAG, "未能提取到备用地址列表，尝试直接解析主地址内容");
                        try {
                            List<ChannelGroup> groups = ChannelParser.parseContent(content);
                            if (groups != null && !groups.isEmpty()) {
                                if (callback != null) {
                                    callback.onSuccess(groups);
                                }
                                return;
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "直接解析主地址内容失败: " + e.getMessage());
                        }
                        
                        if (callback != null) {
                            callback.onError("无法获取频道清单地址列表");
                        }
                        return;
                    }
                    
                    // 尝试按顺序从每个地址加载频道列表
                    tryNextUrl(playlistUrls, 0, callback);
                } catch (Exception e) {
                    Log.e(TAG, "Error extracting playlist URLs from " + mainUrl, e);
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching playlist URLs from " + mainUrl + ": " + error);
                if (callback != null) {
                    callback.onError(error);
                }
            }
        });
    }

    private void tryNextUrl(List<String> urls, int index, ChannelParserCallback callback) {
        if (index >= urls.size()) {
            // 所有URL都尝试完毕仍未成功
            if (callback != null) {
                callback.onError("所有频道清单地址均加载失败");
            }
            return;
        }
        
        String url = urls.get(index);
        if (url == null || url.isEmpty()) {
            // 跳过空URL，尝试下一个
            tryNextUrl(urls, index + 1, callback);
            return;
        }
        
        Log.d(TAG, "尝试加载频道清单: " + url);
        
        networkManager.fetchUrlContentAsync(url, new com.tv.mydiy.network.NetworkManager.NetworkCallback() {
            @Override
            public void onSuccess(String content) {
                try {
                    List<ChannelGroup> groups = ChannelParser.parseContent(content);
                    if (groups != null && !groups.isEmpty()) {
                        Log.d(TAG, "成功从 " + url + " 加载频道清单");
                        if (callback != null) {
                            callback.onSuccess(groups);
                        }
                    } else {
                        // 当前URL加载失败，继续尝试下一个
                        Log.w(TAG, "从 " + url + " 加载的频道清单为空，尝试下一个地址");
                        tryNextUrl(urls, index + 1, callback);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析频道清单失败: " + url, e);
                    // 当前URL解析失败，继续尝试下一个
                    tryNextUrl(urls, index + 1, callback);
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "加载频道清单失败: " + url + ", 错误: " + error);
                // 当前URL加载失败，继续尝试下一个
                tryNextUrl(urls, index + 1, callback);
            }
        });
    }

    public interface ChannelParserCallback {
        void onSuccess(List<ChannelGroup> channelGroups);
        void onError(String error);
    }

}
