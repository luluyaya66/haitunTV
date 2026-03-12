package com.tv.mydiy.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.tv.mydiy.R;

import com.tv.mydiy.ui.MainActivity;

public class PlaybackService extends Service {
    private static final String TAG = "PlaybackService";
    private static final int NOTIFICATION_ID = 2;
    private static final String CHANNEL_ID = "playback_service_channel";
    public static final String ACTION_START_WITH_EXISTING_SETTINGS = "start_with_existing_settings";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "PlaybackService starting with intent: " + intent);
        
        // 启动前台服务
        startForeground(NOTIFICATION_ID, createNotification());
        
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            if (PlaybackService.ACTION_START_WITH_EXISTING_SETTINGS.equals(action)) {
                Log.i(TAG, "Starting with existing settings...");
                // 恢复之前保存的设置
                restorePreviousSettings();
            }
        }
        return START_STICKY;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "播放服务",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("后台播放服务通知");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("播放服务运行中")
            .setContentText("正在后台播放媒体内容")
            .setSmallIcon(R.drawable.ic_tv)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }
    
    private void restorePreviousSettings() {
        SharedPreferences prefs = getSharedPreferences("player_settings", MODE_PRIVATE);
        // 这里可以恢复之前保存的播放设置
        Log.d(TAG, "恢复播放设置");
        
        // 可以在这里添加实际的设置恢复逻辑
        // 例如：
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "PlaybackService destroyed");
        stopForeground(true);
    }
}