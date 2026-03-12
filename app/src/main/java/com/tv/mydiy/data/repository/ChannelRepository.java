package com.tv.mydiy.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.channel.ChannelGroup;
import com.tv.mydiy.channel.ChannelManager;
import com.tv.mydiy.settings.SettingsManager;

import java.util.List;

public class ChannelRepository {

    private static final String TAG = "ChannelRepository";

    private final ChannelManager channelManager;
    private final MutableLiveData<List<ChannelGroup>> channelGroups = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ChannelRepository(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public LiveData<List<ChannelGroup>> getChannelGroups() {
        return channelGroups;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadChannelList(Context context, SettingsManager settingsManager) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        channelManager.loadChannelList(context, settingsManager, new ChannelManager.ChannelLoadCallback() {
            @Override
            public void onSuccess(List<ChannelGroup> groups) {
                channelGroups.setValue(groups);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    public Channel findChannel(List<ChannelGroup> groups, int groupIndex, int channelIndex) {
        if (groups == null || groupIndex < 0 || groupIndex >= groups.size()) {
            return null;
        }
        ChannelGroup group = groups.get(groupIndex);
        if (group == null || channelIndex < 0 || channelIndex >= group.getChannelCount()) {
            return null;
        }
        return group.getChannel(channelIndex);
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

    public int[] getNextChannelIndices(List<ChannelGroup> groups, int currentGroupIndex, int currentChannelIndex, boolean crossGroup) {
        int newGroupIndex = currentGroupIndex;
        int newChannelIndex = currentChannelIndex + 1;

        if (groups == null || newGroupIndex < 0 || newGroupIndex >= groups.size()) {
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

                if (newGroupIndex >= groups.size()) {
                    newGroupIndex = 0;
                }

                ChannelGroup newGroup = groups.get(newGroupIndex);
                if (newGroup.getChannelCount() <= 0) {
                    int originalGroupIndex = newGroupIndex;
                    int attempts = 0;
                    while (attempts < groups.size()) {
                        newGroupIndex++;
                        if (newGroupIndex >= groups.size()) {
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

        if (groups == null || newGroupIndex < 0 || newGroupIndex >= groups.size()) {
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

                if (newGroupIndex >= 0 && newGroupIndex < groups.size()) {
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
