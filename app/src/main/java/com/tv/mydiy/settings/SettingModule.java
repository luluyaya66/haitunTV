package com.tv.mydiy.settings;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.ui.MainActivity;
import com.tv.mydiy.R;
import com.tv.mydiy.util.UiEventUtils;
import com.tv.mydiy.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class SettingModule {
    private static final String TAG = "SettingModule";

    private final MainActivity mainActivity;
    private final Activity activity;
    private final SettingsManager settingsManager;
    private RecyclerView settingsDetailList;
    private ViewGroup rightDrawer;
    private ViewGroup rightSubDrawer;
    private SettingsAdapter settingsAdapter;
    private SettingsAdapter settingsDetailAdapter;
    private List<SettingItem> settingItems = new ArrayList<>();
    
    private com.tv.mydiy.update.UpdateManager updateManager;

    public SettingModule(MainActivity mainActivity, SettingsManager settingsManager) {
        this.mainActivity = mainActivity;
        this.activity = mainActivity;
        this.settingsManager = settingsManager;
        initSettingItems();
    }

    public void setViews(RecyclerView settingsList, RecyclerView settingsDetailList, ViewGroup rightSubDrawer) {
        this.settingsDetailList = settingsDetailList;
        this.rightSubDrawer = rightSubDrawer;
        
        // 初始化设置适配器
        if (settingsAdapter == null) {
            settingsAdapter = new SettingsAdapter(settingItems, this::handleSettingItemClick);
        }
        
        if (settingsList != null) {
            if (settingsList.getLayoutManager() == null) {
                settingsList.setLayoutManager(new LinearLayoutManager(activity));
            }
            if (settingsList.getAdapter() == null) {
                settingsList.setAdapter(settingsAdapter);
            }
        }
        
        // 初始化二级设置适配器
        if (settingsDetailAdapter == null) {
            settingsDetailAdapter = new SettingsAdapter(new ArrayList<>(), this::handleSubSettingItemClickWrapper);
        }
        
        if (settingsDetailList != null) {
            if (settingsDetailList.getLayoutManager() == null) {
                settingsDetailList.setLayoutManager(new LinearLayoutManager(activity));
            }
            if (settingsDetailList.getAdapter() == null) {
                settingsDetailList.setAdapter(settingsDetailAdapter);
            }
        }
    }

    private void initSettingItems() {
        settingItems = new ArrayList<>();
        settingItems.add(createMainSettingItem("线路选择", SettingItem.SettingItemType.LINE_SELECTION));
        settingItems.add(createMainSettingItem("解码设置", SettingItem.SettingItemType.DECODER_TYPE));
        settingItems.add(createMainSettingItem("显示设置", SettingItem.SettingItemType.DISPLAY_SETTINGS));
        settingItems.add(createMainSettingItem("偏好设置", SettingItem.SettingItemType.PREFERENCE_SETTINGS));
        settingItems.add(createMainSettingItem("应用更新", SettingItem.SettingItemType.APP_UPDATE));
    }
    
    /**
     * 创建主设置项的工厂方法
     */
    private SettingItem createMainSettingItem(String title, SettingItem.SettingItemType type) {
        return new SettingItem(title, type);
    }

    public void handleSettingItemClick(SettingItem item) {
        if (item == null || activity == null) return;

        Log.d(TAG, "handleSettingItemClick: " + item.getTitle() + ", type: " + item.getType());

        // 检查是否有子设置项
        if (hasSubSettings(item)) {
            // 显示二级菜单
            showSubSettings(item);
        } else {
            // 处理没有子设置项的选项
            switch (item.getType()) {
                case SOURCE_CONFIG:
                    showSourceConfigDialog();
                    break;
                case RESET_DEFAULTS:
                    showResetConfirmation();
                    break;
                case APP_UPDATE:
                    checkForAppUpdate();
                    break;
                default:
                    Log.w(TAG, "Unknown setting item type: " + item.getType());
                    break;
            }
        }
    }

    /**
     * 检查设置项是否有子设置
     */
    public boolean hasSubSettings(SettingItem item) {
        if (item == null) return false;

        // 定义有子设置的类型集合
        switch (item.getType()) {
            case LINE_SELECTION:
            case ASPECT_RATIO:
            case DECODER_TYPE:
            case TIMEOUT:
            case DISPLAY_SETTINGS:
            case PREFERENCE_SETTINGS:
                return true;
            default:
                return false;
        }
    }

    // 保存当前选中的父项
    private SettingItem currentParentItem;
    
    /**
     * 显示子设置菜单（无动画版本）
     */
    public void showSubSettings(SettingItem parentItem) {
        Log.d(TAG, "showSubSettings called with parentItem: " + parentItem.getTitle() + ", type: " + parentItem.getType());
        
        // 保存当前选中的父项
        this.currentParentItem = parentItem;
        
        // 生成子设置项列表
        List<SettingItem> subItems = generateSubItems(parentItem);
        
        // 更新二级菜单数据
        if (settingsDetailAdapter != null) {
            settingsDetailAdapter.updateData(subItems);
        }

        // 显示二级菜单
        if (rightSubDrawer != null) {
            rightSubDrawer.setVisibility(View.VISIBLE);
            // 确保二级菜单显示在正确层级
            rightSubDrawer.bringToFront();
            
            // 请求重新布局以确保位置正确
            rightSubDrawer.requestLayout();
        }
        
        // 保持一级菜单可见，实现并列显示效果
        if (rightDrawer != null) {
            UiUtils.updateVisibilitySafely(rightDrawer, View.VISIBLE);
        }
        
        // 确保在显示二级菜单时，它被正确地定位
        if (rightSubDrawer != null) {
            // 二级菜单已经在正确位置，只需确保可见性
            rightSubDrawer.setTranslationX(0); // 重置translation
            // 更新布局参数，将二级菜单显示在占位视图位置
            if (rightSubDrawer.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams params = 
                    (FrameLayout.LayoutParams) rightSubDrawer.getLayoutParams();
                int firstMenuWidth = rightSubDrawer.getResources().getDimensionPixelSize(R.dimen.menu_first_level_width); // 一级菜单宽度
                params.setMargins(0, 0, firstMenuWidth, 0); // 位于左侧占位区域
                rightSubDrawer.setLayoutParams(params);
            }
        }
        
        // 将焦点设置到二级设置列表的第一个项目
        if (settingsDetailList != null) {
            // 使用post确保在布局完成后设置焦点
            UiEventUtils.postToUiThread(settingsDetailList, () -> {
                // 确保列表有数据后再设置焦点
                if (settingsDetailList.getAdapter() != null && 
                    settingsDetailList.getAdapter().getItemCount() > 0) {
                    // 滚动到顶部并设置第一个项目为焦点
                    settingsDetailList.scrollToPosition(0);
                    
                    // 获取第一个子视图并设置焦点
                    View firstChild = settingsDetailList.getChildAt(0);
                    if (firstChild != null) {
                        firstChild.requestFocus();
                    } else {
                        // 如果子视图还未创建，等待布局完成后再设置焦点
                        settingsDetailList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                View newFirstChild = settingsDetailList.getChildAt(0);
                                if (newFirstChild != null) {
                                    newFirstChild.requestFocus();
                                }
                                // 移除监听器以避免内存泄漏
                                settingsDetailList.removeOnLayoutChangeListener(this);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 包装方法，用于处理来自适配器的子设置项点击事件
     */
    private void handleSubSettingItemClickWrapper(SettingItem subItem) {
        if (currentParentItem != null) {
            handleSubSettingItemClick(currentParentItem, subItem);
        }
    }
    
    /**
     * 处理子设置项点击事件
     */
    public void handleSubSettingItemClick(SettingItem parentItem, SettingItem subItem) {
        Log.d(TAG, "handleSubSettingItemClick called with parentType: " + parentItem.getType() + ", subItem: " + subItem.getTitle());
        
        switch (parentItem.getType()) {
            case DISPLAY_SETTINGS:
                switch (subItem.getTitle()) {
                    case "显示时间":
                        handleGenericToggle(settingsManager.isShowTime(), 
                                          () -> settingsManager.setShowTime(!settingsManager.isShowTime()), 
                                          subItem);
                        // 更新UI显示
                        if (mainActivity != null) {
                            mainActivity.updateDisplaySettings();
                        }
                        break;
                    case "显示网速":
                        handleGenericToggle(settingsManager.isShowSpeed(), 
                                          () -> settingsManager.setShowSpeed(!settingsManager.isShowSpeed()), 
                                          subItem);
                        // 更新UI显示
                        if (mainActivity != null) {
                            mainActivity.updateDisplaySettings();
                        }
                        break;
                    case "显示节目单":
                        handleGenericToggle(settingsManager.isShowEpg(), 
                                          () -> settingsManager.setShowEpg(!settingsManager.isShowEpg()), 
                                          subItem);
                        // 更新UI显示
                        if (mainActivity != null) {
                            mainActivity.updateDisplaySettings();
                        }
                        break;
                    case "显示台标":
                        handleGenericToggle(settingsManager.isShowLogo(), 
                                          () -> settingsManager.setShowLogo(!settingsManager.isShowLogo()), 
                                          subItem);
                        break;
                }
                break;
                
            case PREFERENCE_SETTINGS:
                switch (subItem.getTitle()) {
                    case "换台反转":
                        handleGenericToggle(settingsManager.isReverseChannelSwitch(), 
                                          () -> settingsManager.setReverseChannelSwitch(!settingsManager.isReverseChannelSwitch()), 
                                          subItem);
                        break;
                    case "开机自启":
                        handleGenericToggle(settingsManager.isAutoStart(), 
                                          () -> settingsManager.setAutoStart(!settingsManager.isAutoStart()), 
                                          subItem);
                        break;
                    case "跨选分类":
                        handleGenericToggle(settingsManager.isCrossGroupSwitch(), 
                                          () -> settingsManager.setCrossGroupSwitch(!settingsManager.isCrossGroupSwitch()), 
                                          subItem);
                        break;
                }
                break;
                
            case LINE_SELECTION:
                // 处理线路选择
                handleLineSelection(subItem);
                // 更新线路选择UI
                if (settingsDetailAdapter != null) {
                    settingsDetailAdapter.notifyDataSetChanged();
                }
                break;
                
            case DECODER_TYPE:
                // 处理解码方式设置
                handleDecoderTypeSelection(subItem);
                break;
                
            case TIMEOUT:
                // 处理超时换源设置
                handleTimeoutSelection(subItem);
                break;
        }
    }

    /**
     * 生成子设置项列表
     */
    private List<SettingItem> generateSubItems(SettingItem parentItem) {
        List<SettingItem> subItems = new ArrayList<>();
        
        switch (parentItem.getType()) {
            case DISPLAY_SETTINGS:
                subItems.add(new SettingItem("显示时间", settingsManager.isShowTime()));
                subItems.add(new SettingItem("显示网速", settingsManager.isShowSpeed()));
                subItems.add(new SettingItem("显示节目单", settingsManager.isShowEpg()));
                subItems.add(new SettingItem("显示台标", settingsManager.isShowLogo()));
                break;
                
            case PREFERENCE_SETTINGS:
                subItems.add(new SettingItem("换台反转", settingsManager.isReverseChannelSwitch()));
                subItems.add(new SettingItem("开机自启", settingsManager.isAutoStart()));
                subItems.add(new SettingItem("跨选分类", settingsManager.isCrossGroupSwitch()));
                break;
                
            case LINE_SELECTION:
                // 添加线路选择项（从当前播放频道获取）
                Channel currentChannel = mainActivity.getCurrentChannel();
                if (currentChannel != null && currentChannel.getSources() != null) {
                    for (int i = 0; i < currentChannel.getSources().size(); i++) {
                        String sourceName = "线路 " + (i + 1) + "/" + currentChannel.getSources().size();
                        boolean isSelected = (i == currentChannel.getCurrentSourceIndex());
                        subItems.add(new SettingItem(sourceName, isSelected, true));
                    }
                } else {
                    // 默认添加几个线路选项
                    subItems.add(new SettingItem("线路 1/5", true, true));
                    subItems.add(new SettingItem("线路 2/5", false, true));
                    subItems.add(new SettingItem("线路 3/5", false, true));
                    subItems.add(new SettingItem("线路 4/5", false, true));
                    subItems.add(new SettingItem("线路 5/5", false, true));
                }
                break;
                
            case DECODER_TYPE:
                // 添加解码方式选项
                subItems.add(new SettingItem("硬件解码", "hardware".equals(settingsManager.getCurrentDecoderType()), true));
                subItems.add(new SettingItem("软件解码", "software".equals(settingsManager.getCurrentDecoderType()), true));
                break;
                
            case TIMEOUT:
                // 添加超时换源选项
                String currentTimeout = settingsManager.getTimeoutValue();
                subItems.add(new SettingItem("5秒", "5".equals(currentTimeout), true));
                subItems.add(new SettingItem("10秒", "10".equals(currentTimeout), true));
                subItems.add(new SettingItem("15秒", "15".equals(currentTimeout), true));
                subItems.add(new SettingItem("20秒", "20".equals(currentTimeout), true));
                subItems.add(new SettingItem("25秒", "25".equals(currentTimeout), true));
                subItems.add(new SettingItem("30秒", "30".equals(currentTimeout), true));
                break;
        }
        
        return subItems;
    }

    public SettingsAdapter getSettingsAdapter() {
        return settingsAdapter;
    }
    
    public SettingsAdapter getSettingsDetailAdapter() {
        return settingsDetailAdapter;
    }
    
    public boolean isSubSettingsVisible() {
        return rightSubDrawer != null && rightSubDrawer.getVisibility() == View.VISIBLE;
    }
    
    /**
     * 隐藏子设置菜单
     */
    public void hideSubSettings() {
        Log.d(TAG, "hideSubSettings called");
        
        if (rightSubDrawer != null) {
            rightSubDrawer.setVisibility(View.GONE);
            
            // 重置位置，将二级菜单移回隐藏位置
            rightSubDrawer.setTranslationX(0); // 重置translation
            // 同时确保使用正确的margin隐藏二级菜单
            if (rightSubDrawer.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams params = 
                    (FrameLayout.LayoutParams) rightSubDrawer.getLayoutParams();
                int firstMenuWidth = rightSubDrawer.getResources().getDimensionPixelSize(R.dimen.menu_first_level_width); // 一级菜单宽度
                int secondMenuWidth = rightSubDrawer.getResources().getDimensionPixelSize(R.dimen.menu_second_level_width); // 二级菜单宽度
                params.setMargins(0, 0, firstMenuWidth + secondMenuWidth, 0); // 隐藏在屏幕外
                rightSubDrawer.setLayoutParams(params);
            }
        }
    }
    
    // 简化其他方法，只保留核心功能
    private void showSourceConfigDialog() {
        // 简化实现
        Toast.makeText(activity, "片源配置功能", Toast.LENGTH_SHORT).show();
    }
    
    private void showResetConfirmation() {
        // 简化实现
        new AlertDialog.Builder(activity)
                .setTitle("确认重置")
                .setMessage("确定要恢复所有设置为默认值吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    settingsManager.resetToDefaults();
                    Toast.makeText(activity, "设置已恢复为默认值", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    private void checkForAppUpdate() {
        // 实现检查更新功能
        updateManager = new com.tv.mydiy.update.UpdateManager(activity);
        
        updateManager.setUpdateCallback(new com.tv.mydiy.update.UpdateManager.UpdateCallback() {
            @Override
            public void onUpdateAvailable(String version, String downloadUrl) {
                // 有新版本可用，询问用户是否更新
                new AlertDialog.Builder(activity)
                    .setTitle("发现新版本")
                    .setMessage("新版本: " + version + "\n是否立即更新？")
                    .setPositiveButton("立即更新", (dialog, which) -> {
                        // 开始下载更新
                        updateManager.downloadUpdate(downloadUrl);
                        Toast.makeText(activity, "开始下载更新...", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("稍后更新", null)
                    .show();
            }

            @Override
            public void onUpdateNotAvailable() {
                Toast.makeText(activity, "当前已是最新版本", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCheckError(String error) {
                Toast.makeText(activity, "检查更新失败: " + error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadProgress(long progress, long total) {
                // 下载进度更新，可以通过通知显示
            }

            @Override
            public void onDownloadComplete() {
                Toast.makeText(activity, "下载完成，准备安装...", Toast.LENGTH_SHORT).show();
            }
        });
        
        updateManager.checkUpdate();
    }
    
    private void handleGenericToggle(boolean currentValue, Runnable toggleAction, SettingItem subItem) {
        toggleAction.run();
        subItem.setChecked(!currentValue);
        // 更新UI
        if (settingsDetailAdapter != null) {
            settingsDetailAdapter.notifyDataSetChanged();
        }
    }
    
    private void handleLineSelection(SettingItem subItem) {
        // 获取当前频道
        Channel currentChannel = mainActivity.getCurrentChannel();
        if (currentChannel != null) {
            // 从标题中提取线路索引（简单处理，实际应该有更好的方式）
            String title = subItem.getTitle();
            try {
                // 提取"线路 X/Y"中的X部分
                String indexStr = title.split("/")[0].replace("线路 ", "");
                int index = Integer.parseInt(indexStr) - 1;
                
                // 切换到指定线路
                currentChannel.setCurrentSourceIndex(index);
                
                // 重新播放当前频道
                mainActivity.playChannel(currentChannel);
                
                // 根据规范，不显示Toast提示
                
                // 更新线路选择状态
                if (settingsDetailAdapter != null) {
                    settingsDetailAdapter.updateSingleSelection(index);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "解析线路索引失败: " + title, e);
                // 根据规范，不显示Toast提示
            }
        }
    }

    private void handleDecoderTypeSelection(SettingItem subItem) {
        // 根据选择的解码方式更新设置
        if ("硬件解码".equals(subItem.getTitle())) {
            settingsManager.setCurrentDecoderType("hardware");
            Toast.makeText(activity, "已切换到硬件解码", Toast.LENGTH_SHORT).show();
        } else if ("软件解码".equals(subItem.getTitle())) {
            settingsManager.setCurrentDecoderType("software");
            Toast.makeText(activity, "已切换到软件解码", Toast.LENGTH_SHORT).show();
        }
        
        // 重新应用解码器设置
        if (mainActivity != null) {
            mainActivity.applyDecoderSettings();
        }
    }
    
    private void handleTimeoutSelection(SettingItem subItem) {
        // 简化实现
        Toast.makeText(activity, "选择了超时时间: " + subItem.getTitle(), Toast.LENGTH_SHORT).show();
    }
    
    public void unregisterUpdateReceiver() {
        if (updateManager != null) {
            updateManager.unregisterReceiver();
        }
    }
}
