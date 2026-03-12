package com.tv.mydiy.settings;

import androidx.annotation.NonNull;

import java.util.Objects;

public class SettingItem {
    private final String title;
    private final SettingItemType type;
    private boolean checked;
    private boolean isRadioButton; // 标记是否为单选按钮
    
    public enum SettingItemType {
        // 主设置项类型
        LINE_SELECTION,
        ASPECT_RATIO,
        DECODER_TYPE,
        TIMEOUT,
        DISPLAY_SETTINGS,
        SOURCE_CONFIG,
        PREFERENCE_SETTINGS,
        RESET_DEFAULTS,
        APP_UPDATE,
        
        // 子设置项类型
        TYPE_SUB_ITEM
    }
    
    public SettingItem(String title, SettingItemType type) {
        this.title = title;
        this.type = type;
    }
    
    public SettingItem(String title, boolean checked) {
        this.title = title;
        this.checked = checked;
        this.type = SettingItemType.TYPE_SUB_ITEM;
    }
    
    // 用于创建单选按钮类型的设置项
    public SettingItem(String title, boolean checked, boolean isRadioButton) {
        this.title = title;
        this.checked = checked;
        this.type = SettingItemType.TYPE_SUB_ITEM;
        this.isRadioButton = isRadioButton;
    }
    
    // Getters and setters
    public String getTitle() { return title; }

    public SettingItemType getType() { return type; }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }
    
    public boolean isRadioButton() { return isRadioButton; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SettingItem that = (SettingItem) o;
        
        if (!Objects.equals(title, that.title)) return false;
        return type == that.type;
    }
    
    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "SettingItem{" +
                "title='" + title + '\'' +
                ", type=" + type +
                '}';
    }

}