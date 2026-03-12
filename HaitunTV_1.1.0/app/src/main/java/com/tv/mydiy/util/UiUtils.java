package com.tv.mydiy.util;

import android.view.View;
import android.widget.TextView;

import com.tv.mydiy.channel.ChannelGroup;

import java.util.List;

/**
 * UI 工具类，用于处理常见的 UI 更新操作，减少重复代码
 */
public class UiUtils {
    
    /**
     * 安全地更新 TextView 的文本内容，只有在内容不同时才进行更新
     *
     * @param textView 要更新的 TextView
     * @param newText  新的文本内容
     */
    public static void updateTextSafely(TextView textView, String newText) {
        if (textView != null && newText != null) {
            String currentText = textView.getText().toString();
            if (!currentText.equals(newText)) {
                textView.setText(newText);
            }
        }
    }
    
    /**
     * 安全地更新 View 的可见性，只有在状态不同时才进行更新
     *
     * @param view       要更新的 View
     * @param visibility 新的可见性状态 (View.VISIBLE, View.GONE, View.INVISIBLE)
     */
    public static void updateVisibilitySafely(View view, int visibility) {
        if (view != null) {
            if (view.getVisibility() != visibility) {
                view.setVisibility(visibility);
            }
        }
    }
    
    /**
     * 安全地更新 View 的可见性，根据布尔值决定是否显示
     *
     * @param view 要更新的 View
     * @param show 如果为 true 则设置为 VISIBLE，否则设置为 GONE
     */
    public static void updateVisibilitySafely(View view, boolean show) {
        updateVisibilitySafely(view, show ? View.VISIBLE : View.GONE);
    }
    
    /**
     * 检查索引是否在列表的有效范围内
     * @param list 要检查的列表
     * @param index 要检查的索引
     * @return 如果索引有效则返回 true，否则返回 false
     */
    public static boolean isValidIndex(List<?> list, int index) {
        return list != null && index >= 0 && index < list.size();
    }
    
    /**
     * 检查组索引是否在频道组列表的有效范围内
     * @param channelGroups 频道组列表
     * @param groupIndex 要检查的组索引
     * @return 如果索引有效则返回 true，否则返回 false
     */
    public static boolean isValidGroupIndex(List<ChannelGroup> channelGroups, int groupIndex) {
        return channelGroups != null && groupIndex >= 0 && groupIndex < channelGroups.size();
    }
    
    /**
     * 检查频道索引是否在频道列表的有效范围内
     * @param channelGroup 频道组
     * @param channelIndex 要检查的频道索引
     * @return 如果索引有效则返回 true，否则返回 false
     */
    public static boolean isValidChannelIndex(ChannelGroup channelGroup, int channelIndex) {
        return channelGroup != null && channelGroup.getChannels() != null && 
               channelIndex >= 0 && channelIndex < channelGroup.getChannels().size();
    }
}