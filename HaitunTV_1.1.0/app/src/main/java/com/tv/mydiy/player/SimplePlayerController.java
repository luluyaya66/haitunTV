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
    
    // 添加超时处理相关变量
    private static final int PLAY_TIMEOUT = 10000; // 10秒超时
    private final Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;

    public SimplePlayerController(Activity activity, IjkPlayerAdapter playerAdapter) {
        this.activity = activity;
        this.playerAdapter = playerAdapter;
        
        // 设置播放器准备好的监听器
        if (playerAdapter != null) {
            playerAdapter.setOnPreparedListener(mp -> {
                Log.d(TAG, "Player prepared, starting playback");
                // 播放器准备完成后手动调用start
                playerAdapter.start();
            });
            
            // 设置播放器信息监听器，用于检测视频渲染开始事件
            playerAdapter.setOnInfoListener((mp, what, extra) -> {
                if (what == IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    Log.d(TAG, "Video rendering started, canceling timeout timer");
                    // 视频开始渲染，取消超时计时器
                    cancelTimeoutTimer();
                }
                return false;
            });
        }
    }
    
    /**
     * 播放指定频道
     *
     * @param channel 要播放的频道
     */
    public void playChannel(Channel channel) {
        if (channel == null) {
            Log.e(TAG, "Channel is null");
            return;
        }
        
        // 保存当前频道引用

        // Log.d(TAG, "playChannel called with: " + channel.getName());
        
        // 开始超时计时
        startTimeoutTimer(channel);
        
        String url = channel.getCurrentSourceUrl();
        // Log.d(TAG, "Playing channel: " + channel.getName() + ", URL: " + url);
        
        if (playerAdapter != null) {
            try {
                // 移除对SourceValidator的调用，直接使用原始URL
                // String processedUrl = SourceValidator.validateAndProcessUrl(url);
                // Log.d(TAG, "Playing URL: " + url);
                
                // 在播放前重置播放器状态
                playerAdapter.stop();
                playerAdapter.setDataSource(url);
                playerAdapter.prepareAsync();
                // Log.d(TAG, "Player started successfully");
                
                // 更新底部频道信息栏显示
                updateChannelInfoBar(channel);
            } catch (Exception e) {
                Log.e(TAG, "Failed to play channel: " + e.getMessage(), e);
                // 尝试切换到下一个源
                switchToNextSource(channel);
            }
        } else {
            Log.e(TAG, "PlayerAdapter is null");
        }
    }
    
    /**
     * 开始超时计时
     * 
     * @param channel 当前播放的频道
     */
    private void startTimeoutTimer(Channel channel) {
        // 先移除之前的超时任务
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        
        // 创建新的超时任务
        timeoutRunnable = () -> {
            // Log.d(TAG, "播放超时，尝试切换到下一个源");
            handleTimeoutSwitchSource(channel);
        };
        
        // 启动超时计时
        timeoutHandler.postDelayed(timeoutRunnable, PLAY_TIMEOUT);
    }
    
    /**
     * 取消超时计时（公共方法，供外部调用）
     */
    public void cancelTimeoutTimer() {
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutRunnable = null;
        }
    }
    
    /**
     * 取消所有计时器任务（包括超时计时器和播放进度更新等）
     */
    public void cancelAllTimers() {
        // 取消超时计时器
        cancelTimeoutTimer();
        
        // 如果还有其他计时器任务，也可以在这里取消
        // 例如更新播放进度的任务等
        timeoutHandler.removeCallbacksAndMessages(null);
    }
    
    /**
     * 切换到下一个源
     *
     * @param channel 当前频道
     */
    public void switchToNextSource(Channel channel) {
        // 取消当前的超时计时
        cancelTimeoutTimer();
        
        if (channel == null) {
            Log.e(TAG, "Channel is null");
            return;
        }
        
        List<Channel.Source> sources = channel.getSources();
        if (sources == null || sources.size() <= 1) {
            // Log.d(TAG, "No alternative sources available");
            // 如果没有备用源，则尝试切换到下一个频道
            tryPlayNextChannel();
            return;
        }
        
        int currentSourceIndex = channel.getCurrentSourceIndex();
        int nextSourceIndex = (currentSourceIndex + 1) % sources.size();
        
        // 循环选择源，不再尝试切换到下一个频道
        channel.setCurrentSourceIndex(nextSourceIndex);
        playChannel(channel);
        
        // 更新底部频道信息栏显示
        updateChannelInfoBar(channel);
        
        // 不再显示源切换提示
    }
    
    /**
     * 尝试播放下一个频道
     */
    private void tryPlayNextChannel() {
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            // 使用反射调用方法，避免直接依赖
            try {
                java.lang.reflect.Method method = MainActivity.class.getMethod("tryPlayNextChannel");
                method.invoke(mainActivity);
            } catch (Exception e) {
                Log.e(TAG, "Failed to call tryPlayNextChannel", e);
            }
        }
    }
    
    /**
     * 切换到上一个源
     *
     * @param channel 当前频道
     */
    public void switchToPreviousSource(Channel channel) {
        // 取消当前的超时计时
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
        
        // 循环选择源，不再尝试切换到上一个频道
        channel.setCurrentSourceIndex(previousSourceIndex);
        playChannel(channel);
        
        // 更新底部频道信息栏显示
        updateChannelInfoBar(channel);
        
        // 不再显示源切换提示
    }

    /**
     * 处理超时换源
     *
     * @param channel 当前频道
     */
    public void handleTimeoutSwitchSource(Channel channel) {
        // 取消当前的超时计时
        cancelTimeoutTimer();
        
        // 直接调用切换到下一个源的方法
        switchToNextSource(channel);
    }
    
    /**
     * 更新底部频道信息栏显示
     * 
     * @param channel 当前频道
     */
    private void updateChannelInfoBar(Channel channel) {
        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.runOnUiThread(() -> {
                mainActivity.updateChannelInfo(channel);
                mainActivity.showChannelInfoBar(channel);
            });
        }
    }

    /**
     * 暂停播放
     */
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