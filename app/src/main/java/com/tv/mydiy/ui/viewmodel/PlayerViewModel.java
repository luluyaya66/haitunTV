package com.tv.mydiy.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.tv.mydiy.channel.Channel;

public class PlayerViewModel extends ViewModel {

    public interface PlayerStateListener {
        void onPlayerStateChanged(int state);
        void onPlayerError(String error);
        void onSourceChanged(Channel channel, int sourceIndex);
    }

    private PlayerStateListener listener;
    
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isBuffering = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> currentSourceIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Channel> currentChannel = new MutableLiveData<>();
    private final MutableLiveData<String> playerError = new MutableLiveData<>();
    private final MutableLiveData<Integer> playerState = new MutableLiveData<>();

    public PlayerViewModel() {
    }

    public void setPlayerStateListener(PlayerStateListener listener) {
        this.listener = listener;
    }

    public LiveData<Channel> getCurrentChannelLiveData() {
        return currentChannel;
    }

    public Channel getCurrentChannel() {
        return currentChannel.getValue();
    }

    public void setCurrentChannel(Channel channel) {
        this.currentChannel.setValue(channel);
    }

    public LiveData<Boolean> getIsPlayingLiveData() {
        return isPlaying;
    }

    public boolean isPlaying() {
        Boolean value = isPlaying.getValue();
        return value != null && value;
    }

    public void setPlaying(boolean playing) {
        this.isPlaying.setValue(playing);
    }

    public LiveData<Boolean> getIsBufferingLiveData() {
        return isBuffering;
    }

    public boolean isBuffering() {
        Boolean value = isBuffering.getValue();
        return value != null && value;
    }

    public void setBuffering(boolean buffering) {
        this.isBuffering.setValue(buffering);
    }

    public LiveData<Integer> getCurrentSourceIndexLiveData() {
        return currentSourceIndex;
    }

    public int getCurrentSourceIndex() {
        Integer value = currentSourceIndex.getValue();
        return value != null ? value : 0;
    }

    public void setCurrentSourceIndex(int currentSourceIndex) {
        this.currentSourceIndex.setValue(currentSourceIndex);
    }

    public LiveData<String> getPlayerErrorLiveData() {
        return playerError;
    }

    public LiveData<Integer> getPlayerStateLiveData() {
        return playerState;
    }

    public int getNextSourceIndex(Channel channel) {
        if (channel == null || channel.getSources() == null || channel.getSources().isEmpty()) {
            return 0;
        }
        Integer currentIndex = currentSourceIndex.getValue();
        int nextIndex = (currentIndex != null ? currentIndex : 0) + 1;
        if (nextIndex >= channel.getSources().size()) {
            nextIndex = 0;
        }
        return nextIndex;
    }

    public int getPreviousSourceIndex(Channel channel) {
        if (channel == null || channel.getSources() == null || channel.getSources().isEmpty()) {
            return 0;
        }
        Integer currentIndex = currentSourceIndex.getValue();
        int prevIndex = (currentIndex != null ? currentIndex : 0) - 1;
        if (prevIndex < 0) {
            prevIndex = channel.getSources().size() - 1;
        }
        return prevIndex;
    }

    public boolean hasMultipleSources(Channel channel) {
        return channel != null && channel.getSources() != null && channel.getSources().size() > 1;
    }

    public String getSourceInfo(Channel channel) {
        if (channel == null || channel.getSources() == null || channel.getSources().isEmpty()) {
            return "1/1";
        }
        Integer currentIndex = currentSourceIndex.getValue();
        return ((currentIndex != null ? currentIndex : 0) + 1) + "/" + channel.getSources().size();
    }

    public void resetPlayerState() {
        isPlaying.setValue(false);
        isBuffering.setValue(false);
        currentSourceIndex.setValue(0);
    }

    public void notifyPlayerStateChanged(int state) {
        playerState.setValue(state);
        if (listener != null) {
            listener.onPlayerStateChanged(state);
        }
    }

    public void notifyPlayerError(String error) {
        playerError.setValue(error);
        if (listener != null) {
            listener.onPlayerError(error);
        }
    }

    public void notifySourceChanged(Channel channel, int sourceIndex) {
        currentSourceIndex.setValue(sourceIndex);
        if (listener != null) {
            listener.onSourceChanged(channel, sourceIndex);
        }
    }
}
