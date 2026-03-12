package com.tv.mydiy.ui.controller;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.channel.ChannelAdapter;
import com.tv.mydiy.channel.ChannelGroup;
import com.tv.mydiy.settings.FavoriteManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FavoriteController {
    private static final String TAG = "FavoriteController";

    private FavoriteManager favoriteManager;
    private FavoriteUpdateListener listener;

    public interface FavoriteUpdateListener {
        void onFavoriteUpdated();
        void runOnUiThread(Runnable runnable);
    }

    public FavoriteController() {
    }

    public void setFavoriteManager(FavoriteManager favoriteManager) {
        this.favoriteManager = favoriteManager;
    }

    public void setListener(FavoriteUpdateListener listener) {
        this.listener = listener;
    }

    public void updateFavoriteGroup(List<ChannelGroup> channelGroups) {
        if (listener == null) return;

        listener.runOnUiThread(() -> {
            updateOriginalGroupsFavoriteStatus(channelGroups);

            List<ChannelGroup> newChannelGroups = new ArrayList<>();
            ChannelGroup favoriteGroup = null;

            for (int i = 0; i < channelGroups.size(); i++) {
                ChannelGroup group = channelGroups.get(i);
                if (group != null && "我的收藏".equals(group.getName())) {
                    favoriteGroup = group;
                    break;
                }
            }

            if (favoriteGroup == null) {
                favoriteGroup = new ChannelGroup("我的收藏");
                newChannelGroups.add(favoriteGroup);
            } else {
                List<Channel> channelsToRemove = new ArrayList<>(favoriteGroup.getChannels());
                for (Channel channel : channelsToRemove) {
                    favoriteGroup.removeChannel(channel);
                }
                newChannelGroups.add(favoriteGroup);
            }

            Set<String> favoriteChannelNames = favoriteManager.getFavoriteChannelNames();

            for (ChannelGroup group : channelGroups) {
                if (group != null && !"我的收藏".equals(group.getName())) {
                    for (Channel channel : group.getChannels()) {
                        if (channel != null && favoriteChannelNames.contains(channel.getName())) {
                            Channel favoriteChannel = new Channel(channel.getName());
                            favoriteChannel.setTvgId(channel.getTvgId());
                            favoriteChannel.setTvgName(channel.getTvgName());
                            favoriteChannel.setTvgLogo(channel.getTvgLogo());
                            favoriteChannel.setGroupTitle(channel.getGroupTitle());
                            for (Channel.Source source : channel.getSources()) {
                                favoriteChannel.addSource(new Channel.Source(source.getUrl(), source.getName()));
                            }
                            favoriteChannel.setCurrentProgram(channel.getCurrentProgram());
                            favoriteChannel.setFavorite(true);
                            favoriteGroup.addChannel(favoriteChannel);
                        }
                    }
                }
            }

            for (ChannelGroup group : channelGroups) {
                if (group != null && !"我的收藏".equals(group.getName())) {
                    newChannelGroups.add(group);
                }
            }

            synchronized (channelGroups) {
                channelGroups.clear();
                channelGroups.addAll(newChannelGroups);
            }

            listener.onFavoriteUpdated();
        });
    }

    private void updateOriginalGroupsFavoriteStatus(List<ChannelGroup> channelGroups) {
        if (channelGroups != null && favoriteManager != null) {
            Set<String> favoriteChannelNames = favoriteManager.getFavoriteChannelNames();

            for (ChannelGroup group : channelGroups) {
                if (group != null && !"我的收藏".equals(group.getName()) && group.getChannels() != null) {
                    for (Channel channel : group.getChannels()) {
                        if (channel != null) {
                            boolean isFavorite = favoriteChannelNames.contains(channel.getName());
                            channel.setFavorite(isFavorite);
                        }
                    }
                }
            }
        }
    }

    public void handleLongPressFavorite(RecyclerView channelList, ChannelAdapter channelAdapter) {
        if (channelAdapter != null && channelList != null) {
            View focusedView = channelList.getFocusedChild();
            if (focusedView != null) {
                int focusedPosition = channelList.getChildAdapterPosition(focusedView);
                if (focusedPosition != RecyclerView.NO_POSITION) {
                    List<ChannelGroup> groups = channelAdapter.getChannelGroups();
                    if (groups != null && !groups.isEmpty()) {
                        for (ChannelGroup group : groups) {
                            if (group != null && group.getChannels() != null) {
                                if (focusedPosition < group.getChannels().size()) {
                                    Channel channel = group.getChannels().get(focusedPosition);
                                    if (channel != null) {
                                        channelAdapter.toggleFavoriteOnUiThread(channel);
                                    }
                                    break;
                                } else {
                                    focusedPosition -= group.getChannels().size();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void release() {
        favoriteManager = null;
        listener = null;
    }
}
