package com.tv.mydiy.channel;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tv.mydiy.R;
import com.tv.mydiy.util.UiUtils;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {
    private volatile List<ChannelGroup> channelGroups;
    private final OnGroupClickListener listener;
    private volatile int currentGroupIndex = -1; // 当前播放频道所在的组索引

    public interface OnGroupClickListener {
        void onGroupClick(ChannelGroup group, int groupIndex);
    }

    public GroupAdapter(List<ChannelGroup> channelGroups, OnGroupClickListener listener) {
        this.channelGroups = channelGroups;
        this.listener = listener;
    }
    
    // 设置当前播放频道所在的组索引
    public void setCurrentGroupIndex(int currentGroupIndex) {
        this.currentGroupIndex = currentGroupIndex;
        notifyDataSetChanged();
    }
    
    // 获取当前选中的组索引
    public int getCurrentGroupIndex() {
        return this.currentGroupIndex;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list_item, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        synchronized (this) {
            if (UiUtils.isValidIndex(channelGroups, position)) {
                ChannelGroup group = channelGroups.get(position);
                if (group != null) {
                    // 检查当前文本是否与新文本相同，避免不必要的UI更新
                    UiUtils.updateTextSafely(holder.textView, group.getName());
                    
                    // 确定目标颜色
                    int targetColor;
                    if (holder.itemView.isFocused()) {
                        targetColor = 0xFFFFFFFF; // 获得焦点时使用白色
                    } else if (position == currentGroupIndex) {
                        targetColor = 0xFF33B5E5; // 当前播放频道所在组以蓝色显示
                    } else {
                        targetColor = 0xFFFFFFFF; // 其他组使用白色
                    }
                    
                    // 检查当前颜色是否与目标颜色相同，避免不必要的UI更新
                    if (holder.textView.getCurrentTextColor() != targetColor) {
                        holder.textView.setTextColor(targetColor);
                    }
                    
                    // 设置焦点状态的背景
                    holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
                        if (hasFocus) {
                            // 获得焦点时，使用选中状态的背景
                            v.setBackgroundResource(R.drawable.menu_item_focused);
                            
                            // 获得焦点时文字使用白色，以确保在蓝色背景下可见
                            int focusColor = 0xFFFFFFFF; // 白色
                            if (holder.textView.getCurrentTextColor() != focusColor) {
                                holder.textView.setTextColor(focusColor);
                            }
                        } else {
                            // 失去焦点时，如果是当前播放频道组，保持蓝色；否则使用白色
                            v.setBackgroundResource(R.drawable.menu_item_focused);
                            
                            int nonFocusColor = (position == currentGroupIndex) ? 0xFF33B5E5 : 0xFFFFFFFF; // 蓝色或白色
                            if (holder.textView.getCurrentTextColor() != nonFocusColor) {
                                holder.textView.setTextColor(nonFocusColor);
                            }
                        }
                    });
                    
                    holder.itemView.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onGroupClick(group, position);
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        synchronized (this) {
            return channelGroups != null ? channelGroups.size() : 0;
        }
    }
    
    // 更新数据
    public void updateData(List<ChannelGroup> newChannelGroups) {
        if (newChannelGroups != null) {
            synchronized (this) {
                this.channelGroups = newChannelGroups;
            }
            notifyDataSetChanged();
        }
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        GroupViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            // 确保视图可以获取焦点
            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);
        }
    }
}