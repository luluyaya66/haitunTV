package com.tv.mydiy.channel;

import com.tv.mydiy.epg.EpgProgram;
import com.tv.mydiy.settings.SettingsManager;
import com.tv.mydiy.settings.FavoriteManager;
import com.tv.mydiy.util.UiEventUtils;
import com.tv.mydiy.util.Constants;
import com.tv.mydiy.util.UiUtils;
import com.tv.mydiy.util.LogUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Objects;
import com.tv.mydiy.R;

import androidx.recyclerview.widget.DiffUtil; // 添加DiffUtil导入

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_GROUP = 0;
    private static final int TYPE_CHANNEL = 1;
    
    // 启用稳定ID，帮助RecyclerView更准确地识别项目
    {
        setHasStableIds(true);
    }

    private volatile List<ChannelGroup> channelGroups;
    
    @Override
    public long getItemId(int position) {
        if (channelGroups == null || position < 0 || position >= getItemCount()) {
            return super.getItemId(position);
        }
        
        synchronized (this) {
            int count = 0;
            for (int i = 0; i < channelGroups.size(); i++) {
                ChannelGroup group = channelGroups.get(i);
                if (group == null) continue;
                
                // 组标题
                if (count == position) {
                    // 为组标题生成稳定ID
                    return group.getName() != null ? group.getName().hashCode() : i;
                }
                count++;
                
                // 频道项（仅当组展开时）
                if (group.isExpanded()) {
                    List<Channel> channels = group.getChannels();
                    if (channels != null) {
                        for (int j = 0; j < channels.size(); j++) {
                            if (count == position) {
                                Channel channel = channels.get(j);
                                // 为频道项生成稳定ID
                                return channel != null && channel.getName() != null ? 
                                    ((long) group.getName().hashCode() * Constants.CHANNEL_ADAPTER_HASH_CODE_MULTIPLIER + channel.getName().hashCode()) :
                                    ((long) i * Constants.CHANNEL_ADAPTER_ID_CALCULATION_BASE + j);
                            }
                            count++;
                        }
                    }
                }
            }
        }
        return super.getItemId(position);
    }

    private final OnChannelClickListener listener;
    private final SettingsManager settingsManager; // 添加SettingsManager
    private final FavoriteManager favoriteManager; // 添加FavoriteManager
    private Channel currentPlayingChannel = null; // 当前播放的频道对象
    private int currentGroupIndex = -1; // 当前播放频道的组索引
    private int currentChannelIndex = -1; // 当前播放频道的频道索引
    private OnDataChangedListener onDataChangedListener; // 数据变化监听器

    public interface OnChannelClickListener {
        void onChannelClick(Channel channel, int groupIndex, int channelIndex);
        void onEpgInfoClick(Channel channel); // 添加EPG信息点击事件
    }
    
    // 添加GroupClickListener接口定义
    public interface GroupClickListener {
        void onGroupClick(ChannelGroup group, int groupIndex);
    }
    
    // 添加OnDataChangedListener接口定义
    public interface OnDataChangedListener {
        void onDataChanged();
    }

    public ChannelAdapter(List<ChannelGroup> channelGroups, OnChannelClickListener listener, SettingsManager settingsManager, Context context) {
        // 使用副本而不是直接引用传入的列表
        this.channelGroups = channelGroups != null ? new java.util.ArrayList<>(channelGroups) : new java.util.ArrayList<>();
        this.listener = listener;
        this.settingsManager = settingsManager; // 保存SettingsManager实例
        this.favoriteManager = new FavoriteManager(context); // 保存FavoriteManager实例
        
        // 默认展开所有分组
        synchronized (this) {
            for (ChannelGroup group : this.channelGroups) {
                if (group != null) {
                    group.setExpanded(true);
                }
            }
        }
    }
    
    // 设置当前播放频道对象
    public void setCurrentPlayingChannel(Channel channel) {
        this.currentPlayingChannel = channel;
        notifyDataSetChanged();
        
        // 通知数据变化监听器
        if (onDataChangedListener != null) {
            onDataChangedListener.onDataChanged();
        }
    }
    
    // 设置当前播放频道的组和频道索引
    public void setCurrentGroupIndex(int groupIndex, int channelIndex) {
        this.currentGroupIndex = groupIndex;
        this.currentChannelIndex = channelIndex;
        notifyDataSetChanged();
        
        // 通知数据变化监听器
        if (onDataChangedListener != null) {
            onDataChangedListener.onDataChanged();
        }
    }

    // 更新数据
    public void updateData(List<ChannelGroup> newChannelGroups) {
        if (newChannelGroups != null) {
            // 使用DiffUtil来安全地更新数据，避免RecyclerView崩溃
            List<ChannelGroup> oldList = new java.util.ArrayList<>(channelGroups != null ? channelGroups : new java.util.ArrayList<>());
            List<ChannelGroup> newList = new java.util.ArrayList<>(newChannelGroups);
            
            ChannelDiffCallback diffCallback = new ChannelDiffCallback(oldList, newList);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
            
            synchronized (this) {
                this.channelGroups = newList;
            }
            
            diffResult.dispatchUpdatesTo(this);
            
            // 通知数据变化监听器
            if (onDataChangedListener != null) {
                onDataChangedListener.onDataChanged();
            }
        }
    }

    // DiffUtil回调类，用于计算列表差异
    private static class ChannelDiffCallback extends DiffUtil.Callback {
        private final List<ChannelGroup> oldList;
        private final List<ChannelGroup> newList;
        
        public ChannelDiffCallback(List<ChannelGroup> oldList, List<ChannelGroup> newList) {
            this.oldList = oldList != null ? oldList : new java.util.ArrayList<>();
            this.newList = newList != null ? newList : new java.util.ArrayList<>();
        }
        
        @Override
        public int getOldListSize() {
            return oldList.size();
        }
        
        @Override
        public int getNewListSize() {
            return newList.size();
        }
        
        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            // 添加空值检查
            if (oldItemPosition < 0 || newItemPosition < 0 || oldItemPosition >= oldList.size() || newItemPosition >= newList.size()) {
                return false;
            }
            
            // 比较两个位置的对象是否代表同一个项目
            ChannelGroup oldGroup = oldList.get(oldItemPosition);
            ChannelGroup newGroup = newList.get(newItemPosition);
            return oldGroup != null && newGroup != null && 
                   oldGroup.getName() != null && newGroup.getName() != null &&
                   oldGroup.getName().equals(newGroup.getName());
        }
        
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            // 添加空值检查
            if (oldItemPosition < 0 || newItemPosition < 0 || oldItemPosition >= oldList.size() || newItemPosition >= newList.size()) {
                return false;
            }
            
            // 比较两个位置的对象内容是否相同
            ChannelGroup oldGroup = oldList.get(oldItemPosition);
            ChannelGroup newGroup = newList.get(newItemPosition);
            
            if (oldGroup == null && newGroup == null) return true;
            if (oldGroup == null || newGroup == null) return false;
            
            // 比较分组名称和展开状态
            if (!Objects.equals(oldGroup.getName(), newGroup.getName())) return false;
            if (oldGroup.isExpanded() != newGroup.isExpanded()) return false;
            
            // 比较频道数量
            if (oldGroup.getChannelCount() != newGroup.getChannelCount()) return false;
            
            // 比较每个频道的信息
            List<Channel> oldChannels = oldGroup.getChannels();
            List<Channel> newChannels = newGroup.getChannels();
            
            if (oldChannels == null && newChannels == null) return true;
            if (oldChannels == null || newChannels == null) return false;
            if (oldChannels.size() != newChannels.size()) return false;
            
            // 使用同步块确保线程安全
            for (int i = 0; i < oldChannels.size(); i++) {
                Channel oldChannel = oldChannels.get(i);
                Channel newChannel = newChannels.get(i);
                
                if (oldChannel == null && newChannel == null) continue;
                if (oldChannel == null || newChannel == null) return false;
                
                // 比较频道的基本信息
                if (!Objects.equals(oldChannel.getName(), newChannel.getName())) return false;
                if (oldChannel.isFavorite() != newChannel.isFavorite()) return false;
                if (!Objects.equals(oldChannel.getCurrentSourceUrl(), newChannel.getCurrentSourceUrl())) return false;
                
                // 比较频道源数量
                if (oldChannel.getSourceCount() != newChannel.getSourceCount()) return false;
            }
            
            return true;
        }
        
        // 添加getChangePayload方法以支持局部更新
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            // 添加空值检查
            if (oldItemPosition < 0 || newItemPosition < 0 || oldItemPosition >= oldList.size() || newItemPosition >= newList.size()) {
                return null;
            }
            
            ChannelGroup oldGroup = oldList.get(oldItemPosition);
            ChannelGroup newGroup = newList.get(newItemPosition);
            
            if (oldGroup == null || newGroup == null) {
                return null;
            }
            
            // 如果只是展开状态改变，返回特定的payload
            if (oldGroup.isExpanded() != newGroup.isExpanded()) {
                return "expanded_state_changed";
            }
            
            // 如果是频道内容改变，返回通用payload
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        if (channelGroups == null || channelGroups.isEmpty()) {
            return TYPE_CHANNEL;
        }
        
        synchronized (this) {
            int count = 0;
            for (int i = 0; i < channelGroups.size(); i++) {
                ChannelGroup group = channelGroups.get(i);
                if (group == null) continue;
                
                // 组标题
                if (count == position) {
                    return TYPE_GROUP;
                }
                count++;
                
                // 频道项（仅当组展开时）
                if (group.isExpanded()) {
                    List<Channel> channels = group.getChannels();
                    if (channels != null) {
                        for (int j = 0; j < channels.size(); j++) {
                            if (count == position) {
                                return TYPE_CHANNEL;
                            }
                            count++;
                        }
                    }
                }
            }
        }
        return TYPE_CHANNEL;
    }

    @Override
    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (parent.getContext() == null) {
            throw new IllegalStateException("Parent context is null");
        }
        
        if (viewType == TYPE_GROUP) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_group, parent, false);
            return new GroupViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_channel, parent, false);
            return new ChannelViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            // 添加索引有效性检查，防止数据不一致导致的崩溃
            synchronized (this) {
                if (position < 0 || position >= getItemCount()) {
                    return;
                }
            }
            
            if (holder instanceof GroupViewHolder) {
                bindGroupViewHolder((GroupViewHolder) holder, position);
            } else if (holder instanceof ChannelViewHolder) {
                bindChannelViewHolder((ChannelViewHolder) holder, position);
            }
        } catch (Exception e) {
            // 捕获可能的异常，防止由于数据不一致导致的crash
            e.printStackTrace();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        // 添加索引有效性检查，防止数据不一致导致的崩溃
        synchronized (this) {
            if (position < 0 || position >= getItemCount()) {
                return;
            }
        }
        
        // 使用payload进行局部更新，提高性能
        if (!payloads.isEmpty()) {
            // 处理局部更新
            try {
                if (holder instanceof ChannelViewHolder) {
                    ChannelViewHolder channelHolder = (ChannelViewHolder) holder;
                    int[] indices = getChannelIndicesForPosition(position);
                    int groupIndex = indices[0];
                    int channelIndex = indices[1];
                    
                    // 确保索引有效后再继续处理
                    synchronized (this) {
                        if (groupIndex >= 0 && groupIndex < channelGroups.size()) {
                            ChannelGroup group = channelGroups.get(groupIndex);
                            if (group != null && channelIndex >= 0 && channelIndex < group.getChannels().size()) {
                                Channel channel = group.getChannels().get(channelIndex);
                                if (channel != null) {
                                    // 根据payload内容进行局部更新
                                    for (Object payload : payloads) {
                                        if (payload instanceof String) {
                                            String updateType = (String) payload;
                                            switch (updateType) {
                                                case "epg":
                                                    // 只更新EPG信息
                                                    updateEpgInfo(channelHolder, channel);
                                                    break;
                                                case "favorite":
                                                    // 只更新收藏状态
                                                    updateFavoriteIcon(channelHolder, channel);
                                                    break;
                                                case "playing":
                                                    // 只更新播放状态
                                                    updatePlayingStatus(channelHolder, groupIndex, channelIndex, channel);
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (holder instanceof GroupViewHolder) {
                    // 对于GroupViewHolder，处理展开状态的变化
                    for (Object payload : payloads) {
                        if ("expanded_state_changed".equals(payload)) {
                            bindGroupViewHolder((GroupViewHolder) holder, position);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // 出现异常时回退到完整绑定
                onBindViewHolder(holder, position);
                e.printStackTrace();
            }
        } else {
            // 在没有payload时，也要确保数据一致性
            synchronized (this) {
                if (position >= getItemCount()) {
                    return;
                }
            }
            onBindViewHolder(holder, position);
        }
    }
    
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        // 回收时不清理台标，避免滚动时丢失
        if (holder instanceof ChannelViewHolder) {
            // 只清理必要的资源，不清理台标
        } else if (holder instanceof GroupViewHolder) {
            ((GroupViewHolder) holder).clear();
        }
    }

    @Override
    public int getItemCount() {
        synchronized (this) {
            if (channelGroups == null || channelGroups.isEmpty()) {
                return 0;
            }
            
            int count = 0;
            for (ChannelGroup group : channelGroups) {
                if (group == null) continue;
                
                count++; // 组标题
                
                // 频道项（仅当组展开时）
                if (group.isExpanded()) {
                    List<Channel> channels = group.getChannels();
                    if (channels != null) {
                        count += channels.size();
                    }
                }
            }
            return count;
        }
    }
    
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        // 当ViewHolder附加到窗口时，可以进行一些优化操作
    }
    
    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        // 不清理台标，避免滚动时丢失
    }

    private void bindGroupViewHolder(GroupViewHolder holder, int position) {
        // 添加边界检查
        synchronized (this) {
            if (channelGroups == null || position < 0 || position >= getItemCount()) {
                return;
            }
        }
        
        int groupIndex = getGroupIndexForPosition(position);
        synchronized (this) {
            // 再次检查边界条件，防止在获取groupIndex的过程中数据发生变化
            if (groupIndex >= 0 && groupIndex < channelGroups.size()) {
                ChannelGroup group = channelGroups.get(groupIndex);
                if (group != null && holder.groupName != null) {
                    holder.groupName.setText(group.getName());
                    // 设置展开/折叠图标
                    if (holder.expandIcon != null) {
                        holder.expandIcon.setImageResource(group.isExpanded() ? R.drawable.ic_keyboard_arrow_down : R.drawable.ic_keyboard_arrow_right);
                    }
                    
                    holder.itemView.setOnClickListener(v -> {
                        // 不再切换展开/折叠状态，而是直接展开并显示频道列表
                        // 通知监听器显示当前组的频道列表
                        if (listener instanceof GroupClickListener) {
                            ((GroupClickListener) listener).onGroupClick(group, groupIndex);
                        } else if (listener != null) {
                            // 添加这个else if分支，确保至少有一种方式可以处理点击事件
                            listener.onChannelClick(null, groupIndex, -1);
                        }
                    });
                }
            }
        }
    }

    private void bindChannelViewHolder(ChannelViewHolder holder, int position) {
        // 添加边界检查
        synchronized (this) {
            if (channelGroups == null || position < 0 || position >= getItemCount()) {
                return;
            }
        }
        
        int[] indices = getChannelIndicesForPosition(position);
        int groupIndex = indices[0];
        int channelIndex = indices[1];
                 
        synchronized (this) {
            // 再次检查边界条件，防止在获取indices的过程中数据发生变化
            if (UiUtils.isValidGroupIndex(channelGroups, groupIndex)) {
                ChannelGroup group = channelGroups.get(groupIndex);
                if (UiUtils.isValidChannelIndex(group, channelIndex)) {
                    Channel channel = group.getChannels().get(channelIndex);
                    if (channel != null && holder.channelName != null) {
                        // 保存当前绑定的频道信息，以便在焦点变化时使用
                        holder.currentGroupIndex = groupIndex;
                        holder.currentChannelIndex = channelIndex;
                        holder.currentChannel = channel;
                        
                        // 检查当前文本是否与新文本相同，避免不必要的UI更新
                        UiUtils.updateTextSafely(holder.channelName, channel.getName());
                        
                        // 显示EPG信息（如果有）
                        updateEpgInfo(holder, channel);
                        
                        // 设置当前播放频道的样式
                        updatePlayingStatus(holder, groupIndex, channelIndex, channel);
                        
                        // 显示收藏图标（如果频道已被收藏）
                        updateFavoriteIcon(holder, channel);
                        
                        // 显示/隐藏台标
                        updateChannelLogoAsync(holder, channel);
                        
                        final int finalGroupIndex = groupIndex;
                        final int finalChannelIndex = channelIndex;
                        
                        // 设置长按监听器来处理收藏功能
                        holder.itemView.setOnLongClickListener(v -> {
                            // 获取操作前的收藏状态，用于确定操作类型
                            boolean wasFavorite = channel.isFavorite();
                            if (favoriteManager != null) {
                                wasFavorite = favoriteManager.isFavorite(channel);
                            }
                            
                            // 切换频道收藏状态 - 使用更安全的异步方法，避免在"我的收藏"组中出现并发问题
                            toggleFavoriteAsync(channel);
                            
                            // 显示收藏状态变化的提示（基于操作前的状态来确定操作类型）
                            String message = wasFavorite ? "已取消收藏: " + channel.getName() : "已收藏: " + channel.getName();
                            // 确保在UI线程中显示Toast
                            if (v.getContext() instanceof android.app.Activity) {
                                android.app.Activity activity = (android.app.Activity) v.getContext();
                                activity.runOnUiThread(() -> android.widget.Toast.makeText(activity, message, android.widget.Toast.LENGTH_SHORT).show());
                            } else {
                                android.widget.Toast.makeText(v.getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
                            }
                            
                            return true; // 表示已处理长按事件
                        });
                        
                        holder.itemView.setOnClickListener(v -> {
                            if (listener != null) {
                                listener.onChannelClick(channel, finalGroupIndex, finalChannelIndex);
                            }
                        });
                        
                        // 设置焦点变化监听器
                        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
                            // 当焦点变化时，重新更新播放状态以调整字体颜色
                            updatePlayingStatus(holder, holder.currentGroupIndex, holder.currentChannelIndex, holder.currentChannel);
                        });
                        
                        // 为频道名称也设置焦点状态监听，以处理长按事件
                        holder.channelName.setOnFocusChangeListener((v, hasFocus) -> {
                            if (!hasFocus) {
                                // 失去焦点时清除长按状态
                                holder.channelName.setPressed(false);
                            }
                        });
                    }
                }
            }
        }
    }
    
    // 更新EPG信息显示
    private void updateEpgInfo(ChannelViewHolder holder, Channel channel) {
        if (holder.epgInfo != null) {
            boolean shouldShowEpg = settingsManager != null && settingsManager.isShowEpg() && channel.getCurrentProgram() != null;
            
            if (shouldShowEpg) {
                EpgProgram program = channel.getCurrentProgram();
                String newEpgText = com.tv.mydiy.epg.EpgManager.formatProgramInfo(program);
                
                // 检查当前文本是否与新文本相同，避免不必要的UI更新
                UiUtils.updateTextSafely(holder.epgInfo, newEpgText);
                
                // 检查当前可见性是否与目标相同，避免不必要的UI更新
                UiUtils.updateVisibilitySafely(holder.epgInfo, View.VISIBLE);
                
                // 添加EPG信息点击事件
                holder.epgInfo.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onEpgInfoClick(channel);
                    }
                });
            } else {
                // 检查当前可见性是否与目标相同，避免不必要的UI更新
                UiUtils.updateVisibilitySafely(holder.epgInfo, View.GONE);
            }
        }
    }
    
    // 更新播放状态显示
    private void updatePlayingStatus(ChannelViewHolder holder, int groupIndex, int channelIndex, Channel channel) {
        // 设置当前播放频道的样式
        boolean isPlaying = isCurrentPlayingChannel(groupIndex, channelIndex, channel);
        boolean isFocused = holder.itemView.hasFocus();
        
        // 检查当前状态是否与目标状态相同，避免不必要的UI更新
        int currentColor = holder.channelName.getCurrentTextColor();
        // 如果是正在播放的频道且没有获得焦点，字体颜色为蓝色
        // 否则（正在播放但获得了焦点，或不是正在播放的频道），字体颜色为白色
        int targetColor = (isPlaying && !isFocused) ? 0xFF33B5E5 : 0xFFFFFFFF;
        
        if (currentColor != targetColor) {
            holder.channelName.setTextColor(targetColor); // 蓝色或白色
        }
    }
    
    // 更新收藏图标显示
    private void updateFavoriteIcon(ChannelViewHolder holder, Channel channel) {
        // 显示收藏图标（如果频道已被收藏）
        if (holder.favoriteIcon != null) {
            boolean isFavorite = channel.isFavorite() || (favoriteManager != null && favoriteManager.isFavorite(channel));
            
            // 检查当前状态是否与目标状态相同，避免不必要的UI更新
            if (holder.favoriteIcon.isSelected() != isFavorite) {
                holder.favoriteIcon.setSelected(isFavorite); // 设置选中状态，应用红色或灰色
            }
            
            // 收藏图标总是可见的，只是颜色不同
            UiUtils.updateVisibilitySafely(holder.favoriteIcon, View.VISIBLE);
        }
    }

    // 异步更新频道Logo显示
    private void updateChannelLogoAsync(ChannelViewHolder holder, Channel channel) {
        // 显示/隐藏台标
        boolean shouldShowLogo = settingsManager != null && settingsManager.isShowLogo();
        
        if (shouldShowLogo) {
            // 检查当前可见性是否与目标相同，避免不必要的UI更新
            UiUtils.updateVisibilitySafely(holder.channelLogo, View.VISIBLE);
            
            // 使用Glide加载台标图片
            String channelName = channel.getName();
            if (channelName != null && !channelName.isEmpty()) {
                // 构造台标文件名（频道名+.png）
                String logoFileName = "file:///android_asset/logos/" + channelName + ".png";
                
                // 使用LruCacheUtil检查是否有缓存的Drawable
                String cacheKey = "logo_" + channelName;
                Drawable cachedLogo = com.tv.mydiy.util.LruCacheUtil.getInstance().get(cacheKey, Drawable.class);
                
                if (cachedLogo != null) {
                    // 检查当前图片是否与缓存图片相同，避免不必要的UI更新
                    if (holder.channelLogo.getDrawable() != cachedLogo) {
                        holder.channelLogo.setImageDrawable(cachedLogo);
                    }
                } else {
                    // 异步加载图片并缓存，使用自定义的Target来处理缓存
                    Glide.with(holder.itemView.getContext())
                            .load(logoFileName)
                            .diskCacheStrategy(DiskCacheStrategy.NONE) // assets资源不需要磁盘缓存
                            .placeholder(R.mipmap.ic_launcher_round) // 加载中的占位图
                            .error(R.mipmap.ic_launcher_round) // 加载失败的图片
                            .into(new com.bumptech.glide.request.target.CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @androidx.annotation.Nullable com.bumptech.glide.request.transition.Transition<? super Drawable> transition) {
                                    // 缓存到LruCache
                                    com.tv.mydiy.util.LruCacheUtil.getInstance().put(cacheKey, resource);
                                    // 设置图片
                                    holder.channelLogo.setImageDrawable(resource);
                                }
                                
                                @Override
                                public void onLoadCleared(@androidx.annotation.Nullable Drawable placeholder) {
                                    // 不设置占位图，保持当前图片
                                }
                                
                                @Override
                                public void onLoadFailed(@androidx.annotation.Nullable Drawable errorDrawable) {
                                    if (errorDrawable != null) {
                                        holder.channelLogo.setImageDrawable(errorDrawable);
                                    }
                                }
                            });
                }
            } else {
                // 频道名为空时使用默认图标
                if (holder.channelLogo.getDrawable() == null || 
                    holder.channelLogo.getTag() == null || 
                    !holder.channelLogo.getTag().equals("default")) {
                    holder.channelLogo.setImageResource(R.mipmap.ic_launcher_round);
                    holder.channelLogo.setTag("default");
                }
            }
        } else {
            // 检查当前可见性是否与目标相同，避免不必要的UI更新
            UiUtils.updateVisibilitySafely(holder.channelLogo, View.GONE);
        }
    }
    
    // 判断是否为当前播放频道
    private boolean isCurrentPlayingChannel(int groupIndex, int channelIndex, Channel channel) {
        // 优先通过频道对象判断
        if (currentPlayingChannel != null && channel != null) {
            return currentPlayingChannel == channel;
        }
        
        // 如果没有频道对象，则通过索引判断
        return (groupIndex == this.currentGroupIndex && channelIndex == this.currentChannelIndex);
    }

    private int getGroupIndexForPosition(int position) {
        if (channelGroups == null || channelGroups.isEmpty()) {
            return -1;
        }
        
        // 添加同步块以确保线程安全
        synchronized (this) {
            int count = 0;
            for (int i = 0; i < channelGroups.size(); i++) {
                ChannelGroup group = channelGroups.get(i);
                if (group == null) continue;
                
                // 组标题
                if (position == count) {
                    return i;
                }
                count++;
                
                // 频道项（仅当组展开时）
                if (group.isExpanded()) {
                    List<Channel> channels = group.getChannels();
                    if (channels != null) {
                        count += channels.size();
                    }
                }
            }
        }
        return -1;
    }

    // 添加公共方法来获取指定位置的频道
    public Channel getChannelAtPosition(int position) {
        int[] indices = getChannelIndicesForPosition(position);
        int groupIndex = indices[0];
        int channelIndex = indices[1];
        
        if (groupIndex >= 0 && channelIndex >= 0) {
            synchronized (this) {
                if (channelGroups != null && 
                    groupIndex < channelGroups.size()) {
                    ChannelGroup group = channelGroups.get(groupIndex);
                    if (group != null && 
                        group.getChannels() != null && 
                        channelIndex < group.getChannels().size()) {
                        return group.getChannel(channelIndex);
                    }
                }
            }
        }
        return null;
    }
    
    // 将方法改为公共，以便外部访问
    public int[] getChannelIndicesForPosition(int position) {
        if (channelGroups == null || channelGroups.isEmpty()) {
            return new int[]{-1, -1};
        }
        
        // 添加同步块以确保线程安全
        synchronized (this) {
            int count = 0;
            for (int i = 0; i < channelGroups.size(); i++) {
                ChannelGroup group = channelGroups.get(i);
                if (group == null) continue;
                
                // 组标题
                if (position == count) {
                    return new int[]{-1, -1};
                }
                count++;
                
                // 频道项（仅当组展开时）
                if (group.isExpanded()) {
                    List<Channel> channels = group.getChannels();
                    if (channels != null) {
                        for (int j = 0; j < channels.size(); j++) {
                            if (position == count) {
                                return new int[]{i, j};
                            }
                            count++;
                        }
                    }
                }
            }
        }
        return new int[]{-1, -1};
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        ImageView expandIcon;

        GroupViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
            expandIcon = itemView.findViewById(R.id.expand_icon);
        }
        
        // 清理视图引用，防止内存泄漏
        void clear() {
            // 清理相关资源
        }
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {
        TextView channelName;
        TextView epgInfo;
        ImageView channelLogo;
        ImageView favoriteIcon;
        int currentGroupIndex;
        int currentChannelIndex;
        Channel currentChannel;

        ChannelViewHolder(View itemView) {
            super(itemView);
            channelName = itemView.findViewById(R.id.channel_name);
            epgInfo = itemView.findViewById(R.id.epg_info);
            channelLogo = itemView.findViewById(R.id.channel_logo);
            favoriteIcon = itemView.findViewById(R.id.favorite_indicator);
            currentGroupIndex = -1;
            currentChannelIndex = -1;
            currentChannel = null;
        }
        
        // 清理视图引用，防止内存泄漏
        void clear() {
            // 不清理台标，避免滚动时丢失
        }
    }

    // 获取频道组列表
    public List<ChannelGroup> getChannelGroups() {
        return channelGroups != null ? new java.util.ArrayList<>(channelGroups) : new java.util.ArrayList<>();
    }
    
    // 添加一个更简单的更新方法，适用于只需要刷新UI的情况
    public void refresh() {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
            notifyDataSetChanged();
        } else {
            android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            UiEventUtils.postToUiThread(handler, this::notifyDataSetChanged);
        }
    }

    /**
     * 更新频道收藏状态
     * @param channel 频道对象
     */
    public void updateChannelFavorite(Channel channel) {
        if (channelGroups == null || channel == null) return;
        
        // 确保在主线程中执行更新操作，避免数据不一致
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            UiEventUtils.postToUiThread(handler, () -> updateChannelFavorite(channel));
            return;
        }
        
        // 使用同步块确保在查找和更新过程中数据不会被其他线程修改
        synchronized (this) {
            // 遍历所有组，查找并更新所有同名频道的收藏状态（跨组同步机制）
            for (int groupIndex = 0; groupIndex < channelGroups.size(); groupIndex++) {
                if (UiUtils.isValidGroupIndex(channelGroups, groupIndex)) {
                    ChannelGroup group = channelGroups.get(groupIndex);
                    if (group != null && group.getChannels() != null) {
                        // 在当前组中查找所有与目标频道同名的频道
                        for (int channelIndex = 0; channelIndex < group.getChannels().size(); channelIndex++) {
                            Channel currentChannel = group.getChannels().get(channelIndex);
                            if (currentChannel != null && channel.getName().equals(currentChannel.getName())) {
                                // 找到了同名频道，计算在列表中的绝对位置
                                int absolutePosition = getAbsolutePosition(groupIndex, channelIndex);
                                if (absolutePosition >= 0 && absolutePosition < getItemCount()) {
                                    // 使用payload只更新收藏状态
                                    notifyItemChanged(absolutePosition, "favorite");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 根据组索引和频道索引计算在列表中的绝对位置
     * @param groupIndex 组索引
     * @param channelIndex 频道索引
     * @return 绝对位置
     */
    private int getAbsolutePosition(int groupIndex, int channelIndex) {
        if (!UiUtils.isValidGroupIndex(channelGroups, groupIndex)) {
            return -1;
        }
        
        int position = 0;
        // 计算前面所有组的项目数
        for (int i = 0; i < groupIndex; i++) {
            ChannelGroup group = channelGroups.get(i);
            if (group == null) continue;
            
            position++; // 组本身占一个位置
            
            // 如果组是展开的，则加上频道数量
            if (group.isExpanded()) {
                List<Channel> channels = group.getChannels();
                if (channels != null) {
                    position += channels.size();
                }
            }
        }
        
        // 加上当前组的位置
        ChannelGroup currentGroup = channelGroups.get(groupIndex);
        if (currentGroup == null) return -1;
        
        position++; // 当前组本身占一个位置
        
        // 如果当前组是展开的，则加上频道在组内的位置
        if (currentGroup.isExpanded()) {
            position += channelIndex;
        } else {
            // 如果组是折叠的，则无法确定频道的绝对位置
            return -1;
        }
        
        return position;
    }
    

    
    // 设置数据变化监听器
    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.onDataChangedListener = listener;
    }
    
    // 切换频道收藏状态
    public void toggleFavorite(Channel channel) {
        if (channel != null && favoriteManager != null) {
            boolean isFavorite = favoriteManager.toggleFavorite(channel);
            channel.setFavorite(isFavorite);
            
            // 更新该频道的收藏图标显示
            updateChannelFavorite(channel);
            
            LogUtil.d("ChannelAdapter", "切换收藏状态: " + channel.getName() + ", 状态: " + isFavorite);
            
            // 通知数据变化监听器，以便更新"我的收藏"组
            // 使用post到消息队列以避免与当前UI操作冲突
            if (onDataChangedListener != null) {
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                handler.post(() -> {
                    try {
                        onDataChangedListener.onDataChanged();
                    } catch (Exception e) {
                        LogUtil.e("ChannelAdapter", "更新收藏组时出错: ", e);
                    }
                });
            }
            
            return;
        }
        LogUtil.d("ChannelAdapter", "无法切换收藏状态: channel或favoriteManager为空");
    }
    
    // 从UI线程安全地切换频道收藏状态
    public void toggleFavoriteOnUiThread(Channel channel) {
        if (channel != null) {
            // 确保在主线程中执行收藏操作，避免RecyclerView并发修改异常
            if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
                toggleFavorite(channel);
            } else {
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                UiEventUtils.postToUiThread(handler, () -> toggleFavorite(channel));
            }
        }
    }
    
    // 异步安全地切换频道收藏状态，延迟执行以避免UI冲突
    public void toggleFavoriteAsync(Channel channel) {
        if (channel != null) {
            // 延迟执行以避免与当前UI操作冲突
            android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            handler.postDelayed(() -> toggleFavoriteOnUiThread(channel), 100); // 延迟100毫秒
        }
    }
    
    // 获取当前播放频道的组索引和频道索引
    public int[] getCurrentPlayingIndices() {
        return new int[]{currentGroupIndex, currentChannelIndex};
    }
}