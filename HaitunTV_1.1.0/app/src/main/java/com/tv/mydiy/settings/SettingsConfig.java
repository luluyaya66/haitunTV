package com.tv.mydiy.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * 设置配置管理器，集中管理所有设置项的默认值和配置信息
 */
public class SettingsConfig {
    
    // 解码器类型默认值
    public static final String DEFAULT_DECODER_TYPE = "hardware";
    
    // 画面比例默认值
    public static final String DEFAULT_ASPECT_RATIO = "fit";
    
    // 超时换源默认值
    public static final String DEFAULT_TIMEOUT_VALUE = "10";
    
    // 播放器类型默认值
    public static final String DEFAULT_PLAYER_TYPE = "ijk";
    
    // 重连间隔默认值
    public static final String DEFAULT_RECONNECT_INTERVAL = "5";
    
    // 断流重试次数默认值
    public static final String DEFAULT_RECONNECT_ENTRIES = "5";
    
    // SharedPreferences文件名
    public static final String SHARED_PREFS_NAME = "player_settings";
    
    // 各种设置项的键名
    public static final String KEY_SHOW_TIME = "show_time";
    public static final String KEY_SHOW_SPEED = "show_speed";
    public static final String KEY_SHOW_EPG = "show_epg";
    public static final String KEY_CUSTOM_CHANNEL_URL = "custom_channel_url";
    public static final String KEY_LAST_PLAYED_CHANNEL_GROUP = "last_played_channel_group";
    public static final String KEY_LAST_PLAYED_CHANNEL_POSITION = "last_played_channel_position";
    public static final String KEY_USE_CUSTOM_URL = "use_custom_url";
    public static final String KEY_REVERSE_CHANNEL_SWITCH = "reverse_channel_switch";
    public static final String KEY_AUTO_START = "auto_start";
    public static final String KEY_RECONNECT_ENTRIES = "reconnect_entries";
    public static final String KEY_SHOW_LOGO = "show_logo";
    public static final String KEY_PLAYER_TYPE = "player_type";
    public static final String KEY_DECODER_TYPE = "decoder_type";
    public static final String KEY_ASPECT_RATIO = "aspect_ratio";
    public static final String KEY_TIMEOUT_SWITCH = "timeout_switch";
    public static final String KEY_CROSS_GROUP_SWITCH = "cross_group_switch";
    public static final String KEY_AUTO_RECONNECT = "auto_reconnect";
    public static final String KEY_RECONNECT_INTERVAL = "reconnect_interval";
    public static final String KEY_REMEMBER_SOURCE = "remember_source";
    public static final String KEY_SUPPORT_TIMESHIFT = "support_timeshift";
    
    // 默认频道列表URL键名 - 引用strings.xml中的定义
    public static final String DEFAULT_CHANNEL_URL_KEY = "default_channel_url";
    
    /**
     * 获取所有设置项的默认值映射
     * @return 包含所有设置项默认值的映射
     */
    public static Map<String, Object> getDefaultSettings() {
        Map<String, Object> defaultSettings = new HashMap<>();
        
        // 显示设置默认值
        defaultSettings.put(KEY_SHOW_TIME, true);
        defaultSettings.put(KEY_SHOW_SPEED, true);
        defaultSettings.put(KEY_SHOW_EPG, true); // 默认开启EPG显示
        defaultSettings.put(KEY_SHOW_LOGO, true);
        
        // 功能设置默认值
        defaultSettings.put(KEY_REVERSE_CHANNEL_SWITCH, false);
        defaultSettings.put(KEY_AUTO_START, false);
        defaultSettings.put(KEY_CROSS_GROUP_SWITCH, true);
        defaultSettings.put(KEY_AUTO_RECONNECT, true);
        defaultSettings.put(KEY_REMEMBER_SOURCE, true);
        defaultSettings.put(KEY_SUPPORT_TIMESHIFT, false);
        
        // 数值设置默认值
        defaultSettings.put(KEY_LAST_PLAYED_CHANNEL_GROUP, -1);
        defaultSettings.put(KEY_LAST_PLAYED_CHANNEL_POSITION, -1);
        
        // 字符串设置默认值
        defaultSettings.put(KEY_DECODER_TYPE, DEFAULT_DECODER_TYPE);
        defaultSettings.put(KEY_ASPECT_RATIO, DEFAULT_ASPECT_RATIO);
        defaultSettings.put(KEY_TIMEOUT_SWITCH, DEFAULT_TIMEOUT_VALUE);
        defaultSettings.put(KEY_PLAYER_TYPE, DEFAULT_PLAYER_TYPE);
        defaultSettings.put(KEY_RECONNECT_INTERVAL, DEFAULT_RECONNECT_INTERVAL);
        defaultSettings.put(KEY_RECONNECT_ENTRIES, DEFAULT_RECONNECT_ENTRIES);
        
        return defaultSettings;
    }
    
    /**
     * 获取重置时需要清除的设置项键名列表
     * @return 需要清除的设置项键名数组
     */
    public static String[] getKeysToClearOnReset() {
        return new String[] {
            KEY_CUSTOM_CHANNEL_URL,
            KEY_USE_CUSTOM_URL
        };
    }

}