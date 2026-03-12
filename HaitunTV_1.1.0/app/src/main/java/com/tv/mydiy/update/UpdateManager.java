package com.tv.mydiy.update;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;



import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 应用更新管理器
 */
public class UpdateManager {
    private static final String UPDATE_URL = "https://gitee.com/JR158/DIR/raw/master/haitun/haitun.txt";

    private String getCurrentAppVersion() {
        try {
            return activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0"; // 默认版本号
        }
    }
    
    private final Activity activity;
    private UpdateCallback callback;
    private long downloadId = -1;
    private DownloadManager downloadManager;
    private BroadcastReceiver receiver;
    
    public interface UpdateCallback {
        void onUpdateAvailable(String version, String downloadUrl);
        void onUpdateNotAvailable();
        void onCheckError(String error);
        void onDownloadProgress(long progress, long total);
        void onDownloadComplete();
    }
    
    public UpdateManager(Activity activity) {
        this.activity = activity;
    }
    
    /**
     * 检查更新
     */
    public void checkUpdate() {
        new Thread(() -> {
            try {
                URL url = new URL(UPDATE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String versionLine = reader.readLine(); // 第一行是版本号
                    String downloadUrl = reader.readLine(); // 第二行是下载地址
                    
                    reader.close();
                    
                    if (versionLine != null && downloadUrl != null) {
                        // 解析版本号，格式为 "ver n.n"
                        String remoteVersion = parseVersion(versionLine);
                        
                        if (remoteVersion != null) {
                            // 比较版本
                            String currentVersion = getCurrentAppVersion();
                            if (isRemoteVersionHigher(remoteVersion, currentVersion)) {
                                if (callback != null) {
                                    activity.runOnUiThread(() -> callback.onUpdateAvailable(remoteVersion, downloadUrl));
                                }
                            } else {
                                if (callback != null) {
                                    activity.runOnUiThread(() -> callback.onUpdateNotAvailable());
                                }
                            }
                        } else {
                            if (callback != null) {
                                activity.runOnUiThread(() -> callback.onCheckError("无法解析远程版本号"));
                            }
                        }
                    } else {
                        if (callback != null) {
                            activity.runOnUiThread(() -> callback.onCheckError("无法读取更新信息"));
                        }
                    }
                } else {
                    if (callback != null) {
                        activity.runOnUiThread(() -> callback.onCheckError("无法连接到更新服务器: " + responseCode));
                    }
                }
                
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) {
                    activity.runOnUiThread(() -> callback.onCheckError("检查更新时发生错误: " + e.getMessage()));
                }
            }
        }).start();
    }
    
    /**
     * 解析版本号
     * @param versionLine 格式为 "ver n.n" 的字符串
     * @return 版本号，如 "1.1"
     */
    private String parseVersion(String versionLine) {
        if (versionLine == null) return null;
        
        // 使用正则表达式匹配 "ver n.n" 格式
        Pattern pattern = Pattern.compile("ver\\s+([\\d.]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(versionLine.trim());
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }
    
    /**
     * 比较远程版本是否高于当前版本
     * @param remoteVersion 远程版本
     * @param currentVersion 当前版本
     * @return 如果远程版本更高返回true，否则返回false
     */
    private boolean isRemoteVersionHigher(String remoteVersion, String currentVersion) {
        try {
            String[] remoteParts = remoteVersion.split("\\.");
            String[] currentParts = currentVersion.split("\\.");
            
            int maxLength = Math.max(remoteParts.length, currentParts.length);
            
            for (int i = 0; i < maxLength; i++) {
                int remotePart = i < remoteParts.length ? Integer.parseInt(remoteParts[i]) : 0;
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;
                
                if (remotePart > currentPart) {
                    return true;
                } else if (remotePart < currentPart) {
                    return false;
                }
            }
            
            return false; // 版本相同
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 如果解析失败，假定版本不高于当前版本
        }
    }
    
    /**
     * 下载更新
     * @param downloadUrl 下载地址
     */
    public void downloadUpdate(String downloadUrl) {
        downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        
        Uri uri = Uri.parse(downloadUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        
        // 设置标题和描述
        request.setTitle("海豚电视更新");
        request.setDescription("正在下载新版本...");
        
        // 设置通知可见性
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        
        // 设置下载路径
        // 使用应用私有目录而不是外部公共目录，避免权限问题
        request.setDestinationInExternalFilesDir(activity, Environment.DIRECTORY_DOWNLOADS, "haitun.apk");
        
        // 设置允许的网络类型
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        
        // 开始下载
        downloadId = downloadManager.enqueue(request);
        
        // 注册广播接收器监听下载完成
        registerDownloadReceiver();
    }
    
    /**
     * 注册下载完成广播接收器
     */
    private void registerDownloadReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long receivedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (receivedId == downloadId) {
                        // 下载完成
                        installApk();
                    }
                }
            }
        };
        
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        ContextCompat.registerReceiver(activity, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }
    
    /**
     * 安装APK
     */
    private void installApk() {
        if (downloadManager == null) return;
        
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        
        if (cursor.moveToFirst()) {
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int status = cursor.getInt(statusIndex);
            
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                int uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                String uriString = cursor.getString(uriIndex);
                
                if (uriString != null) {
                    // 直接使用应用私有目录的APK文件
                    File apkFile = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "haitun.apk");
                    
                    if (apkFile.exists()) {
                        // 适配Android 7.0以上的文件访问
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri apkUri = FileProvider.getUriForFile(
                                    activity,
                                    activity.getPackageName() + ".provider",
                                    apkFile
                            );
                            
                            Intent installIntent = new Intent(Intent.ACTION_VIEW);
                            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            
                            activity.startActivity(installIntent);
                        } else {
                            // Android 7.0以下直接使用文件路径
                            Uri apkUri = Uri.fromFile(apkFile);
                            
                            Intent installIntent = new Intent(Intent.ACTION_VIEW);
                            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            
                            activity.startActivity(installIntent);
                        }
                    }
                }
            }
        }
        
        cursor.close();
        
        if (callback != null) {
            activity.runOnUiThread(() -> callback.onDownloadComplete());
        }
    }
    
    /**
     * 设置回调
     */
    public void setUpdateCallback(UpdateCallback callback) {
        this.callback = callback;
    }

    /**
     * 取消注册广播接收器
     */
    public void unregisterReceiver() {
        if (receiver != null) {
            try {
                activity.unregisterReceiver(receiver);
                receiver = null;
            } catch (IllegalArgumentException e) {
                // 广播接收器可能未注册
            }
        }
    }
}