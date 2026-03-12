package com.tv.mydiy.settings;

import android.content.Context;
import android.content.SharedPreferences;
import com.tv.mydiy.R;

public class SettingsManager {
    private final SharedPreferences prefs;
    
    // 从SettingsConfig导入常量
    public static final String KEY_SHOW_TIME = SettingsConfig.KEY_SHOW_TIME;
    public static final String KEY_SHOW_SPEED = SettingsConfig.KEY_SHOW_SPEED;
    public static final String KEY_SHOW_EPG = SettingsConfig.KEY_SHOW_EPG;
    public static final String KEY_CUSTOM_CHANNEL_URL = SettingsConfig.KEY_CUSTOM_CHANNEL_URL;
    public static final String KEY_LAST_PLAYED_CHANNEL_GROUP = SettingsConfig.KEY_LAST_PLAYED_CHANNEL_GROUP;
    public static final String KEY_LAST_PLAYED_CHANNEL_POSITION = SettingsConfig.KEY_LAST_PLAYED_CHANNEL_POSITION;
    public static final String KEY_USE_CUSTOM_URL = SettingsConfig.KEY_USE_CUSTOM_URL;
    public static final String KEY_REVERSE_CHANNEL_SWITCH = SettingsConfig.KEY_REVERSE_CHANNEL_SWITCH;
    public static final String KEY_AUTO_START = SettingsConfig.KEY_AUTO_START;
    public static final String KEY_SHOW_LOGO = SettingsConfig.KEY_SHOW_LOGO;
    public static final String KEY_DECODER_TYPE = SettingsConfig.KEY_DECODER_TYPE;
    public static final String KEY_TIMEOUT_SWITCH = SettingsConfig.KEY_TIMEOUT_SWITCH;
    public static final String KEY_CROSS_GROUP_SWITCH = SettingsConfig.KEY_CROSS_GROUP_SWITCH;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(SettingsConfig.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public SharedPreferences getSharedPreferences() {
        return prefs;
    }

    /**
     * 获取当前解码器类型
     */
    public String getCurrentDecoderType() {
        return prefs.getString(KEY_DECODER_TYPE, SettingsConfig.DEFAULT_DECODER_TYPE);
    }
    
    /**
     * 设置当前解码器类型
     */
    public void setCurrentDecoderType(String decoderType) {
        prefs.edit().putString(KEY_DECODER_TYPE, decoderType).apply();
    }

    /**
     * 获取超时值
     */
    public String getTimeoutValue() {
        return prefs.getString(KEY_TIMEOUT_SWITCH, SettingsConfig.DEFAULT_TIMEOUT_VALUE); // 默认15秒
    }
    
    public boolean isShowTime() {
        return prefs.getBoolean(KEY_SHOW_TIME, true); // 默认开启时间显示
    }
    
    public boolean isShowSpeed() {
        return prefs.getBoolean(KEY_SHOW_SPEED, true); // 默认开启网速显示
    }
    
    public boolean isShowEpg() {
        return prefs.getBoolean(KEY_SHOW_EPG, false); // 默认不开启EPG显示
    }
    
    public boolean isReverseChannelSwitch() {
        return prefs.getBoolean(KEY_REVERSE_CHANNEL_SWITCH, false);
    }
    
    public boolean isAutoStart() {
        return prefs.getBoolean(KEY_AUTO_START, false);
    }
    
    public boolean isCrossGroupSwitch() {
        return prefs.getBoolean(KEY_CROSS_GROUP_SWITCH, true);
    }
    
    public String getCurrentChannelUrl(Context context) {
        boolean useCustom = prefs.getBoolean(KEY_USE_CUSTOM_URL, false);
        if (useCustom) {
            return prefs.getString(KEY_CUSTOM_CHANNEL_URL, context.getString(R.string.default_channel_url));
        }
        return context.getString(R.string.default_channel_url);
    }

    /**
     * 是否显示台标
     */
    public boolean isShowLogo() {
        return prefs.getBoolean(KEY_SHOW_LOGO, true); // 默认显示台标
    }
    
    /**
     * 重置所有设置为默认值
     */
    public void resetToDefaults() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        
        // 使用SettingsConfig中的默认值设置
        java.util.Map<String, Object> defaultSettings = SettingsConfig.getDefaultSettings();
        for (java.util.Map.Entry<String, Object> entry : defaultSettings.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            }
        }
        
        // 重置片源设置相关
        String[] keysToClear = SettingsConfig.getKeysToClearOnReset();
        for (String key : keysToClear) {
            editor.remove(key);
        }
        
        editor.putInt(KEY_LAST_PLAYED_CHANNEL_GROUP, -1);
        editor.putInt(KEY_LAST_PLAYED_CHANNEL_POSITION, -1);
        editor.apply();
    }
    
    /**
     * 保存上次播放的频道信息
     */
    public void setLastPlayedChannel(int groupIndex, int channelPosition) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LAST_PLAYED_CHANNEL_GROUP, groupIndex);
        editor.putInt(KEY_LAST_PLAYED_CHANNEL_POSITION, channelPosition);
        editor.apply();
    }
    
    /**
     * 获取上次播放的频道组索引
     */
    public int getLastPlayedChannelGroup() {
        return prefs.getInt(KEY_LAST_PLAYED_CHANNEL_GROUP, -1);
    }
    
    /**
     * 获取上次播放的频道位置
     */
    public int getLastPlayedChannelPosition() {
        return prefs.getInt(KEY_LAST_PLAYED_CHANNEL_POSITION, -1);
    }
    
    // Setter methods
    public void setShowTime(boolean showTime) {
        prefs.edit().putBoolean(KEY_SHOW_TIME, showTime).apply();
    }
    
    public void setShowSpeed(boolean showSpeed) {
        prefs.edit().putBoolean(KEY_SHOW_SPEED, showSpeed).apply();
    }
    
    public void setShowEpg(boolean showEpg) {
        prefs.edit().putBoolean(KEY_SHOW_EPG, showEpg).apply();
    }
    
    public void setShowLogo(boolean showLogo) {
        prefs.edit().putBoolean(KEY_SHOW_LOGO, showLogo).apply();
    }
    
    public void setReverseChannelSwitch(boolean reverse) {
        prefs.edit().putBoolean(KEY_REVERSE_CHANNEL_SWITCH, reverse).apply();
    }
    
    public void setAutoStart(boolean autoStart) {
        prefs.edit().putBoolean(KEY_AUTO_START, autoStart).apply();
    }
    
    public void setCrossGroupSwitch(boolean crossGroupSwitch) {
        prefs.edit().putBoolean(KEY_CROSS_GROUP_SWITCH, crossGroupSwitch).apply();
    }

}