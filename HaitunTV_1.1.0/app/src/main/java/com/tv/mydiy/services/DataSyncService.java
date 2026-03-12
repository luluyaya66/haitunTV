package com.tv.mydiy.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import java.util.List;
import com.tv.mydiy.R;

import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.channel.ChannelGroup;
import com.tv.mydiy.channel.ChannelParser;

public class DataSyncService extends JobIntentService {

    private static final String TAG = "DataSyncService";
    public static final String ACTION_SYNC_CHANNELS_ONLY = "sync_channels_only";
    public static final String ACTION_PERIODIC_SYNC = "periodic_sync";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Log.i(TAG, "Starting data synchronization...");

        String action = intent.getAction();
        if (ACTION_SYNC_CHANNELS_ONLY.equals(action)) {
            syncChannelListOnly();
        } else if (ACTION_PERIODIC_SYNC.equals(action)) {
            periodicSync();
        } else {
            // 默认行为也可以同步频道
            syncChannelListOnly();
        }
    }

    private void syncChannelListOnly() {
        Log.d(TAG, "Syncing channel list only, preserving other settings...");
        performChannelSync();
    }
    

    private void periodicSync() {
        Log.d(TAG, "Performing periodic sync...");
        performChannelSync();
    }

    private void performChannelSync() {
        try {
            // 从应用资源获取URL
            String remoteUrl = getApplicationContext().getString(R.string.default_channel_url);
            List<ChannelGroup> channelGroups = ChannelParser.parseFromMainUrlWithFallback(remoteUrl);
            saveChannelGroups(channelGroups);
            Log.i(TAG, "Channel list synchronization completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Channel list synchronization failed", e);
        }
    }

    private void saveChannelGroups(List<ChannelGroup> channelGroups) {
        if (channelGroups == null) {
            Log.w(TAG, "Channel groups list is null, skipping save");
            return;
        }
        
        try {
            SharedPreferences prefs = getSharedPreferences("channel_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            // 保存频道组数量
            editor.putInt("channel_group_count", channelGroups.size());
            
            // 保存每个频道组的信息
            for (int i = 0; i < channelGroups.size(); i++) {
                ChannelGroup group = channelGroups.get(i);
                editor.putString("channel_group_" + i + "_name", group.getName());
                editor.putInt("channel_group_" + i + "_channel_count", group.getChannelCount());
                
                // 保存频道信息
                List<Channel> channels = group.getChannels();
                editor.putInt("channel_group_" + i + "_channels_size", channels.size());
                for (int j = 0; j < channels.size(); j++) {
                    Channel channel = channels.get(j);
                    editor.putString("channel_group_" + i + "_channel_" + j + "_name", channel.getName());
                    editor.putString("channel_group_" + i + "_channel_" + j + "_tvgId", channel.getTvgId());
                    editor.putString("channel_group_" + i + "_channel_" + j + "_tvgName", channel.getTvgName());
                    editor.putString("channel_group_" + i + "_channel_" + j + "_tvgLogo", channel.getTvgLogo());
                    editor.putString("channel_group_" + i + "_channel_" + j + "_groupTitle", channel.getGroupTitle());
                    editor.putInt("channel_group_" + i + "_channel_" + j + "_sourceCount", channel.getSourceCount());
                    
                    // 保存频道源信息
                    for (int k = 0; k < channel.getSourceCount(); k++) {
                        Channel.Source source = channel.getSources().get(k);
                        editor.putString("channel_group_" + i + "_channel_" + j + "_source_" + k + "_url", source.getUrl());
                        editor.putString("channel_group_" + i + "_channel_" + j + "_source_" + k + "_name", source.getName());
                    }
                }
            }
            
            editor.putBoolean("channels_updated", true);
            editor.putLong("last_sync_time", System.currentTimeMillis());
            editor.apply();
            Log.i(TAG, "Channel groups parsed and saved: " + channelGroups.size() + " groups");
        } catch (Exception e) {
            Log.e(TAG, "Error saving channel groups", e);
        }
    }
}