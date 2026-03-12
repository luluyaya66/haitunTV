package com.tv.mydiy.epg;

import android.app.Activity;

import com.tv.mydiy.util.LogUtil;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.channel.ChannelAdapter;
import com.tv.mydiy.channel.ChannelGroup;
import com.tv.mydiy.settings.SettingsManager;
import com.tv.mydiy.ui.MainActivity;

import java.util.List;

import com.tv.mydiy.util.UiUtils;

public class EpgController {
    private static final String TAG = "EpgController";

    private final Activity activity;
    private final EpgManager epgManager;
    private final SettingsManager settingsManager;
    private ChannelAdapter channelAdapter;
    private final MainActivity mainActivity;
    
    // EPG节目列表显示完成回调接口
    public interface OnEpgDisplayCompleteListener {
        void onEpgDisplayComplete(Channel channel, RecyclerView epgProgramList, EpgProgramAdapter adapter);
    }
    
    public EpgManager getEpgManager() {
        return epgManager;
    }

    public EpgController(Activity activity, EpgManager epgManager, SettingsManager settingsManager, ChannelAdapter channelAdapter, MainActivity mainActivity) {
        this.activity = activity;
        this.epgManager = epgManager;
        this.settingsManager = settingsManager;
        this.channelAdapter = channelAdapter;
        this.mainActivity = mainActivity;
    }

    /**
     * 加载EPG数据
     */
    public void loadEpgData(EpgManager.OnEpgLoadListener listener) {
        if (epgManager != null) {
            // 总是加载EPG数据，不管显示设置如何，因为EPG数据加载和显示是分离的
            epgManager.loadEpgData(new EpgManager.OnEpgLoadListener() {
                @Override
                public void onEpgLoaded() {
                    // 立即对所有频道进行EPG匹配，不管显示设置如何
                    // 这样EPG数据就绪了，显示与否由设置控制
                    // 使用完整的频道数据进行匹配，而不仅仅是当前显示的频道
                    updateChannelsWithEpgForAllChannels();
                    
                    // 通知原始监听器
                    if (listener != null) {
                        listener.onEpgLoaded();
                    }
                }

                @Override
                public void onEpgLoadFailed(String error) {
                    // 通知原始监听器
                    if (listener != null) {
                        listener.onEpgLoadFailed(error);
                    }
                }
            });
        } else {
            // 如果epgManager为null，标记为已完成
            if (listener != null) {
                listener.onEpgLoaded();
            }
        }
    }

    /**
     * 处理EPG显示变化
     * @param showEpg 是否显示EPG
     */
    public void handleEpgDisplayChange(boolean showEpg) {
        // 检查EPG显示设置是否发生变化，避免在修改其他显示选项时误触发EPG数据清理
        boolean oldShowEpg = settingsManager.isShowEpg();
        if (oldShowEpg != showEpg) {
            // 更新设置
            settingsManager.setShowEpg(showEpg);
            
            // 根据新的设置决定是加载还是清除EPG数据
            if (showEpg) {
                // 如果启用了EPG显示，只需更新UI显示EPG信息
                // 更新频道列表中的EPG信息
                if (channelAdapter != null) {
                    updateChannelsWithEpg(channelAdapter);
                } else {
                    LogUtil.e(TAG, "EpgController中保存的channelAdapter为null，无法更新EPG信息");
                }
            } else {
                // 如果关闭了EPG显示，清除已加载的EPG数据
                clearEpgData();
            }
        } else {
            // 即使设置未变化，如果showEpg为true，也更新显示
            if (showEpg) {
                // 更新频道列表中的EPG信息
                if (channelAdapter != null) {
                    updateChannelsWithEpg(channelAdapter);
                } else {
                    LogUtil.e(TAG, "EpgController中保存的channelAdapter为null，无法更新EPG信息");
                }
            }
        }
    }
    
    /**
     * 清除EPG数据
     */
    public void clearEpgData() {
        if (epgManager != null) {
            // 清除EPG数据，以便在下次启用时重新加载
            epgManager.clearEpgData();
        }
    }
    
    /**
     * 更新ChannelAdapter引用
     * @param channelAdapter 新的ChannelAdapter实例
     */
    public void updateChannelAdapter(ChannelAdapter channelAdapter) {
        this.channelAdapter = channelAdapter;
    }

    /**
     * 更新频道EPG信息
     */
    public void updateChannelsWithEpg(ChannelAdapter channelAdapter) {

        // 检查channelAdapter是否为null
        if (channelAdapter == null) {
            return;
        }
        
        boolean dataChanged = false;

        List<ChannelGroup> channelGroups = channelAdapter.getChannelGroups();
        if (channelGroups == null) {
            return;
        }
        
        // 遍历所有频道组和频道，进行EPG匹配
        // 注意：即使EPG显示设置为关闭，我们仍然需要更新频道对象中的EPG信息
        // 这样当用户开启EPG显示时，信息已经准备就绪
        for (int groupIndex = 0; groupIndex < channelGroups.size(); groupIndex++) {
            if (UiUtils.isValidGroupIndex(channelGroups, groupIndex)) {
                ChannelGroup group = channelGroups.get(groupIndex);
                if (group == null || group.getChannels() == null) {
                    continue; // 跳过空组或空频道列表
                }
                for (Channel channel : group.getChannels()) {
                    if (channel == null) {
                        continue; // 跳过空频道
                    }

                    // 根据频道名称获取EPG信息，按照EPG与频道名称匹配优先级规则
                    EpgProgram currentProgram;

                    // 首先尝试使用频道名称进行精确匹配
                    currentProgram = epgManager.getCurrentProgram(channel.getName());

                    // 如果精确匹配失败，尝试使用tvg-id匹配
                    if (currentProgram == null && channel.getTvgId() != null && !channel.getTvgId().isEmpty()) {
                        currentProgram = epgManager.getCurrentProgram(channel.getTvgId());
                    }

                    // 如果精确匹配失败，尝试使用tvg-name匹配
                    if (currentProgram == null && channel.getTvgName() != null && !channel.getTvgName().isEmpty()) {
                        currentProgram = epgManager.getCurrentProgram(channel.getTvgName());
                    }
                    
                    // 如果以上匹配都失败，尝试标准化名称匹配，处理CCTV-1与CCTV1这类格式差异
                    if (currentProgram == null) {
                        // 尝试标准化频道名称匹配
                        String normalizedChannelName = channel.getName().replaceAll("-", "").trim();
                        currentProgram = epgManager.getCurrentProgram(normalizedChannelName);
                    }
                    
                    // 最后尝试标准化tvg-id匹配
                    if (currentProgram == null && channel.getTvgId() != null && !channel.getTvgId().isEmpty()) {
                        String normalizedTvgId = channel.getTvgId().replaceAll("-", "").trim();
                        currentProgram = epgManager.getCurrentProgram(normalizedTvgId);
                    }
                    
                    // 最后尝试标准化tvg-name匹配
                    if (currentProgram == null && channel.getTvgName() != null && !channel.getTvgName().isEmpty()) {
                        String normalizedTvgName = channel.getTvgName().replaceAll("-", "").trim();
                        currentProgram = epgManager.getCurrentProgram(normalizedTvgName);
                    }

                    if (currentProgram != null) {
                        channel.setCurrentProgram(currentProgram);
                        dataChanged = true;
                    }
                }
            }
        }

        // 只有在设置中启用了EPG显示才更新UI
        // 这样即使频道对象中包含了EPG信息，UI也不会显示，直到用户开启EPG显示
        if (settingsManager.isShowEpg()) {
            // 如果数据发生变化，通知适配器更新
            if (dataChanged) {
                // 确保在主线程中更新适配器
                activity.runOnUiThread(channelAdapter::refresh);
            }
        }
    }
    
    /**
     * 使用所有频道进行EPG匹配（从MainActivity获取完整数据）
     */
    public void updateChannelsWithEpgForAllChannels() {
        // 获取完整的频道数据并进行EPG匹配
        if (mainActivity != null) {
            // 使用MainActivity提供的完整频道数据进行匹配
            ChannelAdapter fullChannelAdapter = mainActivity.getFullChannelAdapter();
            if (fullChannelAdapter != null) {
                updateChannelsWithEpg(fullChannelAdapter);
            } else {
                if (this.channelAdapter != null) {
                    updateChannelsWithEpg(this.channelAdapter);
                }
            }
        } else {
            if (this.channelAdapter != null) {
                updateChannelsWithEpg(this.channelAdapter);
            }
        }
    }

    /**
     * 获取指定频道当前播放节目在节目列表中的位置
     * @param channel 频道对象
     * @param programs 节目列表
     * @return 当前播放节目在列表中的位置，如果未找到则返回-1
     */
    public int getCurrentProgramPosition(Channel channel, List<EpgProgram> programs) {
        if (channel == null || programs == null || programs.isEmpty()) {
            return -1;
        }
        
        // 通过EpgManager获取当前节目 - 这与二级菜单中显示的当前节目是同一逻辑
        EpgProgram currentProgram = epgManager.getCurrentProgram(channel.getName());
        
        if (currentProgram != null) {
            // 在节目列表中查找当前节目的位置
            String targetStartTime = currentProgram.getStartTime();
            String targetTitle = currentProgram.getTitle();
            String targetChannelId = currentProgram.getChannelId();
            
            for (int i = 0; i < programs.size(); i++) {
                EpgProgram program = programs.get(i);
                if (program != null) {
                    boolean isMatching = true;
                    
                    // 匹配开始时间
                    if (targetStartTime != null && program.getStartTime() != null) {
                        isMatching = targetStartTime.equals(program.getStartTime());
                    }
                    
                    // 匹配标题
                    if (isMatching && targetTitle != null && program.getTitle() != null) {
                        isMatching = targetTitle.equals(program.getTitle());
                    }
                    
                    // 匹配频道ID（可选，增加匹配准确性）
                    if (isMatching && targetChannelId != null && program.getChannelId() != null) {
                        isMatching = targetChannelId.equals(program.getChannelId());
                    }
                    
                    if (isMatching) {
                        return i;
                    }
                }
            }
        }
        
        return -1;
    }

    /**
     * 显示指定频道的EPG节目清单
     */
    public void showEpgProgramList(Channel channel, 
                                   RecyclerView epgProgramList,
                                   LinearLayoutManager layoutManager,
                                   EpgProgramAdapter epgProgramAdapter,
                                   android.widget.TextView epgDrawerTitle,
                                   android.widget.LinearLayout leftEpgDrawer,
                                   OnEpgDisplayCompleteListener listener) {
        // 检查EPG显示设置，如果用户设置不显示EPG，则直接返回
        if (settingsManager != null && !settingsManager.isShowEpg()) {
            // 通知调用者EPG显示完成（虽然没有实际显示）
            if (listener != null) {
                listener.onEpgDisplayComplete(channel, epgProgramList, epgProgramAdapter);
            }
            return;
        }
        
        // 在后台线程获取EPG节目数据，避免阻塞UI线程
        new Thread(() -> {
            // 获取频道的所有EPG节目
            List<EpgProgram> programs = epgManager.getAllProgramsForChannel(channel);
            LogUtil.d(TAG, "获取到 " + (programs != null ? programs.size() : 0) + " 个节目用于频道: " + channel.getName());
            
            // 在主线程中执行UI更新
            activity.runOnUiThread(() -> {
                // 设置标题
                if (epgDrawerTitle != null) {
                    epgDrawerTitle.setText(channel.getName() + " 节目清单");
                }
        
                // 设置布局管理器和适配器（如果尚未设置）
                if (epgProgramList != null) {
                    if (epgProgramList.getLayoutManager() == null) {
                        epgProgramList.setLayoutManager(layoutManager);
                    }
                    if (epgProgramList.getAdapter() == null && epgProgramAdapter != null) {
                        epgProgramList.setAdapter(epgProgramAdapter);
                    }
                }
        
                // 更新EPG节目适配器
                if (epgProgramAdapter != null) {
                    epgProgramAdapter.updateData(programs);
                }
        
                // 显示三级抽屉
                if (leftEpgDrawer != null) {
                    leftEpgDrawer.setVisibility(android.view.View.VISIBLE);
                }
                
                // 通知调用者EPG显示完成
                if (listener != null) {
                    listener.onEpgDisplayComplete(channel, epgProgramList, epgProgramAdapter);
                }
            });
        }).start();
    }
}