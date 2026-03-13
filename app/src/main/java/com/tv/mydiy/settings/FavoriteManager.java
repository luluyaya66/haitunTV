package com.tv.mydiy.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.tv.mydiy.channel.Channel;
import java.util.HashSet;
import java.util.Set;

public class FavoriteManager {
    private static final String TAG = "FavoriteManager";
    private static final String FAVORITE_PREFS_NAME = "favorite_channels";
    private static final String FAVORITE_CHANNELS_KEY = "favorite_channels_list";
    
    private final SharedPreferences prefs;
    
    public FavoriteManager(Context context) {
        prefs = context.getSharedPreferences(FAVORITE_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void addFavorite(Channel channel) {
        if (channel != null && channel.getName() != null) {
            Set<String> favorites = getFavoriteChannelNames();
            favorites.add(channel.getName());
            saveFavoriteChannels(favorites);
            Log.d(TAG, "添加收藏: " + channel.getName());
        }
    }

    public void removeFavorite(Channel channel) {
        if (channel != null && channel.getName() != null) {
            Set<String> favorites = getFavoriteChannelNames();
            favorites.remove(channel.getName());
            saveFavoriteChannels(favorites);
            Log.d(TAG, "移除收藏: " + channel.getName());
        }
    }

    public boolean isFavorite(Channel channel) {
        if (channel != null && channel.getName() != null) {
            Set<String> favorites = getFavoriteChannelNames();
            return favorites.contains(channel.getName());
        }
        return false;
    }

    public Set<String> getFavoriteChannelNames() {
        Set<String> defaultSet = new HashSet<>();
        return prefs.getStringSet(FAVORITE_CHANNELS_KEY, defaultSet);
    }

    private void saveFavoriteChannels(Set<String> favorites) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(FAVORITE_CHANNELS_KEY, favorites);
        editor.apply();
    }

    public boolean toggleFavorite(Channel channel) {
        if (channel != null && channel.getName() != null) {
            boolean isFavorite = isFavorite(channel);
            Log.d(TAG, "切换频道收藏状态: " + channel.getName() + ", 当前状态: " + isFavorite);
            if (isFavorite) {
                removeFavorite(channel);
            } else {
                addFavorite(channel);
            }
            boolean newState = !isFavorite;
            Log.d(TAG, "频道收藏新状态: " + channel.getName() + ", 新状态: " + newState);
            return newState;
        }
        Log.d(TAG, "无法切换收藏状态: channel或channel.getName()为null");
        return false;
    }



}
