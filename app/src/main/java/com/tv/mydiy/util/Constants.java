package com.tv.mydiy.util;

/**
 * 应用程序常量类，用于统一管理所有硬编码的数值和字符串常量
 */
public class Constants {
    
    // ChannelAdapter 相关常量
    public static final int CHANNEL_ADAPTER_HASH_CODE_MULTIPLIER = 31;
    public static final int CHANNEL_ADAPTER_ID_CALCULATION_BASE = 1000;
    public static final int CHANNEL_ADAPTER_MAX_PRELOAD_COUNT = 50;
    
    // NetworkManager 相关常量
    public static final long NETWORK_CACHE_DURATION_MINUTES = 30L;
    public static final long NETWORK_CACHE_DURATION_MILLIS = NETWORK_CACHE_DURATION_MINUTES * 60 * 1000; // 30分钟的毫秒数
    
    // MainActivity 相关常量
    public static final long MAIN_ACTIVITY_LONG_PRESS_DELAY = 800L; // 长按延迟毫秒数
    public static final long MAIN_ACTIVITY_DOUBLE_CLICK_TIME_DELTA = 300L; // 双击时间间隔阈值(毫秒)
    public static final int MAIN_ACTIVITY_MAX_RELOAD_ATTEMPTS = 3; // 最大重载尝试次数
    public static final int MAIN_ACTIVITY_EXIT_DELAY = 3000; // 退出延迟时间(毫秒)
    public static final int MAIN_ACTIVITY_INFO_BAR_DISPLAY_TIME = 3000; // 信息栏显示时间(毫秒)
    
    // NetworkManager 超时相关常量
    public static final int NETWORK_CONNECT_TIMEOUT_SECONDS = 10; // 连接超时秒数
    public static final int NETWORK_READ_TIMEOUT_SECONDS = 5;    // 读取超时秒数
    public static final int NETWORK_WRITE_TIMEOUT_SECONDS = 5;   // 写入超时秒数
    
    // 连接池相关常量
    public static final int NETWORK_CONNECTION_POOL_MAX_IDLE = 5;
    public static final int NETWORK_DISPATCHER_MAX_REQUESTS = 20;
    public static final int NETWORK_DISPATCHER_MAX_REQUESTS_PER_HOST = 10;
    
    // 缓冲区相关常量
    public static final int BUFFER_SIZE = 1024;
    
    // 网络速度监控相关常量
    public static final int NETWORK_SPEED_UPDATE_INTERVAL = 1000; // 网络速度更新间隔(毫秒)

    // UI相关常量
    public static final int UI_ANIMATION_DURATION_SHORT = 300; // 短动画时长(毫秒)
    public static final int UI_ANIMATION_DURATION_LONG = 500;  // 长动画时长(毫秒)
    
    // 加载进度相关常量
    public static final int LOADING_PROGRESS_TIMEOUT = 3000; // 加载进度条超时时间(毫秒)
    public static final int LOADING_PROGRESS_LONG_TIMEOUT = 10000; // 加载进度条长超时时间(毫秒)
    
    // 字符编码相关常量
    public static final String DEFAULT_CHARSET = "UTF-8";
    
    // 网络请求相关常量
    public static final String USER_AGENT = "Mozilla/5.0 (Linux; Android TV; Android 4.4) AppleWebKit/537.36";
    public static final String ACCEPT_ENCODING = "gzip";
    
    // Android 4网络优化相关常量
    public static final int ANDROID_4_CONNECT_TIMEOUT_SECONDS = 15;
    public static final int ANDROID_4_READ_TIMEOUT_SECONDS = 30;
    public static final int ANDROID_4_WRITE_TIMEOUT_SECONDS = 15;
    public static final int ANDROID_4_RETRY_COUNT = 3;
    public static final long ANDROID_4_RETRY_DELAY_MS = 1000;
    
    // 其他常量
    public static final int DEFAULT_MIN_VALUE = -1;
    public static final int DEFAULT_ZERO_VALUE = 0;
    public static final int DEFAULT_ONE_VALUE = 1;
}