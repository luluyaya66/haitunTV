package com.tv.mydiy.ui;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tv.mydiy.channel.ChannelAdapter;
import com.tv.mydiy.epg.EpgController;
import com.tv.mydiy.epg.EpgManager;
import com.tv.mydiy.settings.SettingsManager;
import com.tv.mydiy.util.HandlerManager;
import com.tv.mydiy.util.Constants;
import com.tv.mydiy.util.UiUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * UI更新管理器，用于集中管理应用中的UI显示更新逻辑
 */
public class UiUpdater {
    private final TextView timeDisplay;
    private final TextView networkSpeed;
    private final ChannelAdapter channelAdapter;
    private final EpgController epgController;
    private final SettingsManager settingsManager;
    
    // 时间格式化器
    private final SimpleDateFormat timeFormat;
    
    // 时间更新处理器
    private final Runnable timeRunnable;
    
    // 网络速度监控相关变量
    private long lastTotalRxBytes = 0;
    private long lastTimeStamp = 0;
    private NetworkSpeedRunnable networkSpeedRunnable;
    
    // Handler管理器
    private final HandlerManager handlerManager;
    
    // 用于防止递归调用的标志位
    private boolean isUpdatingDisplaySettings = false;
    private boolean previousShowEpgSetting = false;
    
    public UiUpdater(TextView timeDisplay, TextView networkSpeed, 
                     ChannelAdapter channelAdapter, EpgController epgController,
                     SettingsManager settingsManager, HandlerManager handlerManager) {
        this.timeDisplay = timeDisplay;
        this.networkSpeed = networkSpeed;
        this.channelAdapter = channelAdapter;
        this.epgController = epgController;
        this.settingsManager = settingsManager;
        this.handlerManager = handlerManager;
        
        // 初始化时间格式化器
        this.timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        
        // 初始化时间更新处理器
        this.timeRunnable = new TimeRunnable(this);
        
        // 确保EpgController有正确的ChannelAdapter引用
        if (epgController != null && channelAdapter != null) {
            epgController.updateChannelAdapter(channelAdapter);

            new Thread(() -> {
                epgController.loadEpgData(new EpgManager.OnEpgLoadListener() {
                    @Override
                    public void onEpgLoaded() {
                        Log.d("UiUpdater", "EPG数据加载成功");
                        // EPG数据加载完成
                    }

                    @Override
                    public void onEpgLoadFailed(String error) {
                        Log.e("UiUpdater", "EPG数据加载失败: " + error);
                        // EPG数据加载失败
                    }
                });
            }).start();
        }
    }

    public void updateDisplaySettings() {
        if (settingsManager == null || isUpdatingDisplaySettings) {
            return;
        }
        
        isUpdatingDisplaySettings = true;
        
        try {
            boolean showTime = settingsManager.isShowTime();
            boolean showSpeed = settingsManager.isShowSpeed();
            boolean showEpg = settingsManager.isShowEpg();
            boolean oldShowEpg = previousShowEpgSetting;
            previousShowEpgSetting = showEpg;
            
            if (timeDisplay != null) {
                // 检查当前可见性是否与目标相同，避免不必要的UI更新
                UiUtils.updateVisibilitySafely(timeDisplay, showTime);
            }
            
            if (networkSpeed != null) {
                // 检查当前可见性是否与目标相同，避免不必要的UI更新
                UiUtils.updateVisibilitySafely(networkSpeed, showSpeed);
            }
            
            // 当EPG设置发生变化时，或者首次初始化且EPG设置为启用时，更新EPG显示设置
            if (epgController != null) {
                if (oldShowEpg != showEpg) {
                    // 设置发生变化，按原逻辑处理
                    epgController.handleEpgDisplayChange(showEpg);
                } else if (showEpg && !previousShowEpgSetting) {
                    // 首次初始化且EPG设置为启用，加载EPG数据
                    epgController.handleEpgDisplayChange(true);
                }
            }
            
            // 更新频道列表中的台标显示
            if (channelAdapter != null) {
                // 通知频道适配器刷新台标显示状态
                channelAdapter.refresh();
            }
            
            // 控制时间更新
            if (showTime || showSpeed) {
                startTimeUpdater();
                if (showSpeed) {
                    startNetworkSpeedMonitoring();
                }
            } else {
                stopTimeUpdater();
                stopNetworkSpeedMonitoring();
            }
        } finally {
            isUpdatingDisplaySettings = false;
        }
    }

    public void updateTimeDisplay() {
        if (timeDisplay != null && timeDisplay.getVisibility() == View.VISIBLE) {
            String currentTime = timeFormat.format(new Date());
            UiUtils.updateTextSafely(timeDisplay, currentTime);
        }
    }

    public void startTimeUpdater() {
        stopTimeUpdater(); // 先停止之前的更新任务
        if (handlerManager != null && timeRunnable != null) {
            handlerManager.getTimeHandler().post(timeRunnable);
        }
    }

    public void stopTimeUpdater() {
        if (handlerManager != null && timeRunnable != null) {
            handlerManager.getTimeHandler().removeCallbacks(timeRunnable);
        }
    }

    public void startNetworkSpeedMonitoring() {
        // 初始化网络速度更新任务
        networkSpeedRunnable = new NetworkSpeedRunnable(this);
        
        // 启动网络速度更新
        if (handlerManager != null) {
            handlerManager.getMainHandler().post(networkSpeedRunnable);
        }
    }
    
    // 更新网络速度显示
    private void updateNetworkSpeed() {
        // 获取总接收字节数
        long currentRxBytes = android.net.TrafficStats.getTotalRxBytes();
        long currentTimeStamp = System.currentTimeMillis();
        
        // 计算网速
        if (lastTimeStamp != 0) {
            long timeDelta = currentTimeStamp - lastTimeStamp;
            long bytesDelta = currentRxBytes - lastTotalRxBytes;
            
            if (timeDelta > 0) {
                // 计算每秒字节数
                double speedBps = (double) bytesDelta / (timeDelta / 1000.0);
                
                // 转换为合适的单位
                String formattedSpeed;
                if (speedBps >= 1024 * 1024) {
                    // MB/s
                    formattedSpeed = String.format(Locale.getDefault(), "%.1fMB/s", speedBps / (1024 * 1024));
                } else if (speedBps >= 1024) {
                    // KB/s
                    formattedSpeed = String.format(Locale.getDefault(), "%.1fKB/s", speedBps / 1024);
                } else {
                    // B/s
                    formattedSpeed = String.format(Locale.getDefault(), "%.1fB/s", speedBps);
                }
                
                // 更新UI
                updateNetworkSpeedDisplay(formattedSpeed);
            }
        }
        
        // 更新上次记录的值
        lastTotalRxBytes = currentRxBytes;
        lastTimeStamp = currentTimeStamp;
    }
    
    // 专门用于更新网络速度显示的方法，避免与网络速度计算方法混淆
    private void updateNetworkSpeedDisplay(String formattedSpeed) {
        if (networkSpeed != null && networkSpeed.getVisibility() == View.VISIBLE) {
            String newText = formattedSpeed != null && !formattedSpeed.isEmpty() ? "↓" + formattedSpeed : "↓--";
            
            // 检查当前文本是否与新文本相同，避免不必要的UI更新
            UiUtils.updateTextSafely(networkSpeed, newText);
        }
    }
    
    /**
     * 停止网络速度监控
     */
    public void stopNetworkSpeedMonitoring() {
        if (handlerManager != null && networkSpeedRunnable != null) {
            handlerManager.getMainHandler().removeCallbacks(networkSpeedRunnable);
        }
    }

    // 时间更新任务
    private static class TimeRunnable implements Runnable {
        private final WeakReference<UiUpdater> updaterRef;
        
        TimeRunnable(UiUpdater updater) {
            this.updaterRef = new WeakReference<>(updater);
        }
        
        @Override
        public void run() {
            UiUpdater updater = updaterRef.get();
            if (updater != null && updater.handlerManager != null) {
                updater.updateTimeDisplay();
                // 每秒更新一次时间
                updater.handlerManager.getTimeHandler().postDelayed(this, Constants.NETWORK_SPEED_UPDATE_INTERVAL);
            }
        }
    }
    
    // 网络速度监控任务
    private static class NetworkSpeedRunnable implements Runnable {
        private final WeakReference<UiUpdater> updaterRef;
        
        NetworkSpeedRunnable(UiUpdater updater) {
            this.updaterRef = new WeakReference<>(updater);
        }
        
        @Override
        public void run() {
            UiUpdater updater = updaterRef.get();
            if (updater != null && updater.handlerManager != null) {
                updater.updateNetworkSpeed();
                // 每秒更新一次网络速度
                updater.handlerManager.getMainHandler().postDelayed(this, Constants.NETWORK_SPEED_UPDATE_INTERVAL);
            }
        }
    }

}
