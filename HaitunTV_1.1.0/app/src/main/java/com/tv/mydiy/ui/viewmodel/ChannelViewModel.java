package com.tv.mydiy.ui.viewmodel;

import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.channel.ChannelGroup;

import java.util.List;

public class ChannelViewModel {

    public interface OnChannelChangeListener {
        void onChannelChanged(Channel channel, int groupIndex, int channelIndex);
        void onChannelLoadError(String error);
    }

    private OnChannelChangeListener listener;

    public ChannelViewModel() {
    }

    public void setOnChannelChangeListener(OnChannelChangeListener listener) {
        this.listener = listener;
    }

    public boolean isValidGroupIndex(List<ChannelGroup> groups, int index) {
        return groups != null && index >= 0 && index < groups.size();
    }

    public boolean isValidChannelIndex(ChannelGroup group, int index) {
        return group != null && index >= 0 && index < group.getChannelCount();
    }

    public Channel getChannel(List<ChannelGroup> groups, int groupIndex, int channelIndex) {
        if (isValidGroupIndex(groups, groupIndex)) {
            ChannelGroup group = groups.get(groupIndex);
            if (isValidChannelIndex(group, channelIndex)) {
                return group.getChannel(channelIndex);
            }
        }
        return null;
    }

    public int findGroupIndexForChannel(List<ChannelGroup> groups, Channel channel) {
        if (groups == null || channel == null) {
            return -1;
        }
        for (int i = 0; i < groups.size(); i++) {
            ChannelGroup group = groups.get(i);
            for (int j = 0; j < group.getChannelCount(); j++) {
                if (channel.equals(group.getChannel(j))) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int findChannelIndexInGroup(ChannelGroup group, Channel channel) {
        if (group == null || channel == null) {
            return -1;
        }
        for (int i = 0; i < group.getChannelCount(); i++) {
            if (channel.equals(group.getChannel(i))) {
                return i;
            }
        }
        return -1;
    }

    public int[] findChannelIndices(List<ChannelGroup> groups, Channel channel) {
        int[] result = {-1, -1};
        if (groups == null || channel == null) {
            return result;
        }
        for (int i = 0; i < groups.size(); i++) {
            ChannelGroup group = groups.get(i);
            for (int j = 0; j < group.getChannelCount(); j++) {
                if (channel.equals(group.getChannel(j))) {
                    result[0] = i;
                    result[1] = j;
                    return result;
                }
            }
        }
        return result;
    }

    public int[] getNextChannelIndices(List<ChannelGroup> groups, int currentGroupIndex, int currentChannelIndex, boolean crossGroup) {
        int newGroupIndex = currentGroupIndex;
        int newChannelIndex = currentChannelIndex + 1;

        if (!isValidGroupIndex(groups, newGroupIndex)) {
            return new int[]{currentGroupIndex, currentChannelIndex};
        }

        ChannelGroup currentGroup = groups.get(newGroupIndex);
        
        if (!crossGroup) {
            if (newChannelIndex >= currentGroup.getChannelCount()) {
                newChannelIndex = 0;
            }
        } else {
            if (newChannelIndex >= currentGroup.getChannelCount()) {
                newGroupIndex++;
                newChannelIndex = 0;
                
                if (!isValidGroupIndex(groups, newGroupIndex)) {
                    newGroupIndex = 0;
                }
                
                ChannelGroup newGroup = groups.get(newGroupIndex);
                if (newGroup.getChannelCount() <= 0) {
                    int originalGroupIndex = newGroupIndex;
                    int attempts = 0;
                    while (attempts < groups.size()) {
                        newGroupIndex++;
                        if (!isValidGroupIndex(groups, newGroupIndex)) {
                            newGroupIndex = 0;
                        }
                        if (newGroupIndex == originalGroupIndex) {
                            break;
                        }
                        newGroup = groups.get(newGroupIndex);
                        if (newGroup.getChannelCount() > 0) {
                            break;
                        }
                        attempts++;
                    }
                }
            }
        }

        return new int[]{newGroupIndex, newChannelIndex};
    }

    public int[] getPreviousChannelIndices(List<ChannelGroup> groups, int currentGroupIndex, int currentChannelIndex, boolean crossGroup) {
        int newGroupIndex = currentGroupIndex;
        int newChannelIndex = currentChannelIndex - 1;

        if (!isValidGroupIndex(groups, newGroupIndex)) {
            return new int[]{currentGroupIndex, currentChannelIndex};
        }

        ChannelGroup currentGroup = groups.get(newGroupIndex);
        
        if (!crossGroup) {
            if (newChannelIndex < 0) {
                newChannelIndex = currentGroup.getChannelCount() - 1;
                if (newChannelIndex < 0) {
                    newChannelIndex = 0;
                }
            }
        } else {
            if (newChannelIndex < 0) {
                newGroupIndex--;
                
                if (newGroupIndex < 0) {
                    newGroupIndex = groups.size() - 1;
                }
                
                if (isValidGroupIndex(groups, newGroupIndex)) {
                    ChannelGroup newGroup = groups.get(newGroupIndex);
                    newChannelIndex = newGroup.getChannelCount() - 1;
                    
                    if (newChannelIndex < 0) {
                        int originalGroupIndex = newGroupIndex;
                        int attempts = 0;
                        while (attempts < groups.size()) {
                            newGroupIndex--;
                            if (newGroupIndex < 0) {
                                newGroupIndex = groups.size() - 1;
                            }
                            if (newGroupIndex == originalGroupIndex) {
                                break;
                            }
                            newGroup = groups.get(newGroupIndex);
                            newChannelIndex = newGroup.getChannelCount() - 1;
                            if (newChannelIndex >= 0) {
                                break;
                            }
                            attempts++;
                        }
                    }
                }
            }
        }

        return new int[]{newGroupIndex, newChannelIndex};
    }
}
