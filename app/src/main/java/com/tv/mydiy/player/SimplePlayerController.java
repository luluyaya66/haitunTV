package com.tv.mydiy.player;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.ui.MainActivity;

import tv.danmaku.ijk.media.player.IMediaPlayer;

import java.util.List;

public class SimplePlayerController {
    private static final String TAG = "SimplePlayerController";
    
    private final Activity activity;
    private final IjkPlayerAdapter playerAdapter;

    private static final int PLAY_TIMEOUT = 10000; // 10秒超时
    private final Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;

    public SimplePlayerController(Activity activity, IjkPlayerAdapter playerAdapter) {
        this.activity = activity;
        this.playerAdapter = playerAdapter;

        if (playerAdapter != null) {
            playerAdapter.setOnPreparedListener(mp -> {
                Log.d(TAG, "Player prepared, starting playback");
                playerAdapter.start();
            });

            playerAdapter.setOnInfoListener((mp, what, extra) -> {
                if (what == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    Log.d(TAG, "Video rendering started, canceling timeout timer");

                    cancelTimeoutTimer();
                }
                return false;
            });
        }
    }

    public void playChannel(Channel channel) {
        if (channel == null) {
            Log.e(TAG, "Channel is null");
            return;
        }

        startTimeoutTimer(channel);
        
        String url = channel.getCurrentSourceUrl();
        // Log.d(TAG, "Playing channel: " + channel.getName() + ", URL: " + url);
        
        if (playerAdapter != null) {
            try {
                playerAdapter.stop();
                playerAdapter.setDataSource(url);
                playerAdapter.prepareAsync();

                updateChannelInfoBar(channel);
            } catch (Exception e) {
                Log.e(TAG, "Failed to play channel: " + e.getMessage(), e);
                switchToNextSource(channel);
            }
        } else {
            Log.e(TAG, "PlayerAdapter is null");
        }
    }

    private void startTimeoutTimer(Channel channel) {
        // 先移除之前的超时任务
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }

        timeoutRunnable = () -> {
            handleTimeoutSwitchSource(channel);
        };

        timeoutHandler.postDelayed(timeoutRunnable, PLAY_TIMEOUT);
    }

    public void cancelTimeoutTimer() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }

    public void cancelAllTimers() {
        // 取消超时计时器
        cancelTimeoutTimer();
        timeoutHandler.removeCallbacksAndMessages(null);
    }

    public void switchToNextSource(Channel channel) {

        cancelTimeoutTimer();
        
        if (channel == null) {
            Log.e(TAG, "Channel is null");
            return;
        }
        
        List<Channel.Source> sources = channel.getSources();
        if (sources == null || sources.size() <= 1) {

            tryPlayNextChannel();
            return;
        }
        
        int currentSourceIndex = channel.getCurrentSourceIndex();
        int nextSourceIndex = (currentSourceIndex + 1) % sources.size();

        channel.setCurrentSourceIndex(nextSourceIndex);
        playChannel(channel);
 
        updateChannelInfoBar(channel);

    }
    

    private void tryPlayNextChannel() {
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;

            try {
                java.lang.reflect.Method method = MainActivity.class.getMethod("tryPlayNextChannel");
                method.invoke(mainActivity);
            } catch (Exception e) {
                Log.e(TAG, "Failed to call tryPlayNextChannel", e);
            }
        }
    }

    public void switchToPreviousSource(Channel channel) {

        cancelTimeoutTimer();
        
        if (channel == null) {
            Log.e(TAG, "Channel is null");
            return;
        }
        
        List<Channel.Source> sources = channel.getSources();
        if (sources == null || sources.size() <= 1) {
            // Log.d(TAG, "No alternative sources available");
            return;
        }
        
        int currentSourceIndex = channel.getCurrentSourceIndex();
        int previousSourceIndex = (currentSourceIndex - 1 + sources.size()) % sources.size();

        channel.setCurrentSourceIndex(previousSourceIndex);
        playChannel(channel);

        updateChannelInfoBar(channel);

    }


    public void handleTimeoutSwitchSource(Channel channel) {

        cancelTimeoutTimer();

        switchToNextSource(channel);
    }
    

    private void updateChannelInfoBar(Channel channel) {
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.runOnUiThread(() -> {
                mainActivity.updateChannelInfo(channel);
                mainActivity.showChannelInfoBar(channel);
            });
        }
    }

    public void pause() {
        if (playerAdapter != null) {
            try {
                playerAdapter.pause();
            } catch (Exception e) {
                Log.e(TAG, "Failed to pause player", e);
            }
        }
    }

}
