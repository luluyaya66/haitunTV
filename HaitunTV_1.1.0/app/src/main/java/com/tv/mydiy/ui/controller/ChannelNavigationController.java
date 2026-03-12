package com.tv.mydiy.ui.controller;

import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.channel.ChannelGroup;

import java.util.List;

public class ChannelNavigationController {
    private static final String TAG = "ChannelNavigationController";

    private ChannelNavigationListener listener;

    public interface ChannelNavigationListener {
        void changeChannel(int direction);
    }

    public ChannelNavigationController() {
    }

    public void setListener(ChannelNavigationListener listener) {
        this.listener = listener;
    }

    public void tryPlayNextChannel() {
        if (listener != null) {
            listener.changeChannel(1);
        }
    }

    public int findGroupIndexForChannel(Channel channel, List<ChannelGroup> channelGroups) {
        if (channelGroups != null && channel != null) {
            for (int i = 0; i < channelGroups.size(); i++) {
                ChannelGroup group = channelGroups.get(i);
                if (group != null && group.containsChannel(channel)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int findChannelIndexInGroup(int groupIndex, Channel channel, List<ChannelGroup> channelGroups) {
        if (channelGroups != null && groupIndex >= 0 && groupIndex < channelGroups.size()) {
            ChannelGroup group = channelGroups.get(groupIndex);
            if (group != null) {
                return group.getChannelIndex(channel);
            }
        }
        return -1;
    }

    public int[] findChannelIndices(Channel channel, List<ChannelGroup> channelGroups) {
        if (channelGroups != null && channel != null) {
            for (int groupIndex = 0; groupIndex < channelGroups.size(); groupIndex++) {
                ChannelGroup group = channelGroups.get(groupIndex);
                if (group != null) {
                    int channelIndex = group.getChannelIndex(channel);
                    if (channelIndex != -1) {
                        return new int[]{groupIndex, channelIndex};
                    }
                }
            }
        }
        return new int[]{-1, -1};
    }

    public Channel getChannelToPlay(List<ChannelGroup> channelGroups, 
                                    int lastGroupIndex, int lastChannelPosition) {
        if (channelGroups == null || channelGroups.isEmpty()) {
            return null;
        }

        Channel lastPlayedChannel = null;
        if (lastGroupIndex >= 0 && lastGroupIndex < channelGroups.size() &&
            lastChannelPosition >= 0) {
            ChannelGroup lastGroup = channelGroups.get(lastGroupIndex);
            if (lastGroup != null && lastChannelPosition < lastGroup.getChannelCount()) {
                lastPlayedChannel = lastGroup.getChannel(lastChannelPosition);
            }
        }

        if (lastPlayedChannel == null) {
            for (ChannelGroup group : channelGroups) {
                if (group != null && group.getChannelCount() > 0) {
                    for (int i = 0; i < group.getChannelCount(); i++) {
                        Channel channel = group.getChannel(i);
                        if (channel != null && channel.getSources() != null && !channel.getSources().isEmpty()) {
                            return channel;
                        }
                    }
                }
            }
        }

        return lastPlayedChannel;
    }

    public void release() {
        listener = null;
    }
}
