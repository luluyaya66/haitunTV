package com.tv.mydiy.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.channel.ChannelGroup;

import java.util.List;

public class MainViewModel extends ViewModel {

    private static final String TAG = "MainViewModel";

    private final MutableLiveData<Channel> currentChannel = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentGroupIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> currentChannelIndex = new MutableLiveData<>(0);

    private final MutableLiveData<Boolean> isChannelListLoaded = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isPlayerManagerInitialized = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isSplashScreenHidden = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> hasLoggedInitializationComplete = new MutableLiveData<>(false);

    private final MutableLiveData<List<ChannelGroup>> channelGroups = new MutableLiveData<>();

    private long firstChannelChangeTime = -1;
    private static final long AUTH_CHECK_INTERVAL = 60 * 60 * 1000;

    private int reloadAttempts = 0;
    private static final int MAX_RELOAD_ATTEMPTS = 3;

    private boolean isBackPressedOnce = false;

    public MainViewModel() {
    }

    public LiveData<Channel> getCurrentChannelLiveData() {
        return currentChannel;
    }

    public Channel getCurrentChannel() {
        return currentChannel.getValue();
    }

    public void setCurrentChannel(Channel currentChannel) {
        this.currentChannel.setValue(currentChannel);
    }

    public LiveData<Integer> getCurrentGroupIndexLiveData() {
        return currentGroupIndex;
    }

    public int getCurrentGroupIndex() {
        Integer value = currentGroupIndex.getValue();
        return value != null ? value : 0;
    }

    public void setCurrentGroupIndex(int currentGroupIndex) {
        this.currentGroupIndex.setValue(currentGroupIndex);
    }

    public LiveData<Integer> getCurrentChannelIndexLiveData() {
        return currentChannelIndex;
    }

    public int getCurrentChannelIndex() {
        Integer value = currentChannelIndex.getValue();
        return value != null ? value : 0;
    }

    public void setCurrentChannelIndex(int currentChannelIndex) {
        this.currentChannelIndex.setValue(currentChannelIndex);
    }

    public LiveData<Boolean> getIsChannelListLoadedLiveData() {
        return isChannelListLoaded;
    }

    public boolean isChannelListLoaded() {
        Boolean value = isChannelListLoaded.getValue();
        return value != null && value;
    }

    public void setChannelListLoaded(boolean channelListLoaded) {
        this.isChannelListLoaded.setValue(channelListLoaded);
    }

    public LiveData<Boolean> getIsPlayerManagerInitializedLiveData() {
        return isPlayerManagerInitialized;
    }

    public boolean isPlayerManagerInitialized() {
        Boolean value = isPlayerManagerInitialized.getValue();
        return value != null && value;
    }

    public void setPlayerManagerInitialized(boolean playerManagerInitialized) {
        this.isPlayerManagerInitialized.setValue(playerManagerInitialized);
    }

    public LiveData<Boolean> getIsSplashScreenHiddenLiveData() {
        return isSplashScreenHidden;
    }

    public boolean isSplashScreenHidden() {
        Boolean value = isSplashScreenHidden.getValue();
        return value != null && value;
    }

    public void setSplashScreenHidden(boolean splashScreenHidden) {
        this.isSplashScreenHidden.setValue(splashScreenHidden);
    }

    public LiveData<Boolean> getHasLoggedInitializationCompleteLiveData() {
        return hasLoggedInitializationComplete;
    }

    public boolean isHasLoggedInitializationComplete() {
        Boolean value = hasLoggedInitializationComplete.getValue();
        return value != null && value;
    }

    public void setHasLoggedInitializationComplete(boolean hasLoggedInitializationComplete) {
        this.hasLoggedInitializationComplete.setValue(hasLoggedInitializationComplete);
    }

    public LiveData<List<ChannelGroup>> getChannelGroupsLiveData() {
        return channelGroups;
    }

    public List<ChannelGroup> getChannelGroups() {
        return channelGroups.getValue();
    }

    public void setChannelGroups(List<ChannelGroup> channelGroups) {
        this.channelGroups.setValue(channelGroups);
    }

    public long getFirstChannelChangeTime() {
        return firstChannelChangeTime;
    }

    public void setFirstChannelChangeTime(long firstChannelChangeTime) {
        this.firstChannelChangeTime = firstChannelChangeTime;
    }

    public int getReloadAttempts() {
        return reloadAttempts;
    }

    public void setReloadAttempts(int reloadAttempts) {
        this.reloadAttempts = reloadAttempts;
    }

    public void incrementReloadAttempts() {
        this.reloadAttempts++;
    }

    public boolean canReload() {
        return reloadAttempts < MAX_RELOAD_ATTEMPTS;
    }

    public boolean isBackPressedOnce() {
        return isBackPressedOnce;
    }

    public void setBackPressedOnce(boolean backPressedOnce) {
        isBackPressedOnce = backPressedOnce;
    }

    public void resetFirstChannelChangeTime() {
        firstChannelChangeTime = -1;
    }

    public void recordFirstChannelChange() {
        if (firstChannelChangeTime == -1) {
            firstChannelChangeTime = System.currentTimeMillis();
        }
    }

    public boolean shouldCheckAuthorization(long currentTime) {
        return firstChannelChangeTime == -1 || 
               (currentTime - firstChannelChangeTime) > AUTH_CHECK_INTERVAL;
    }

    public void resetPlayerManagerInitialized() {
        isPlayerManagerInitialized.setValue(false);
    }

    public void resetInitializationFlags() {
        isPlayerManagerInitialized.setValue(false);
        hasLoggedInitializationComplete.setValue(false);
    }
}
