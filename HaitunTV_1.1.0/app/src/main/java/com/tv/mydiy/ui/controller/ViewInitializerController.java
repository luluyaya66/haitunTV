package com.tv.mydiy.ui.controller;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tv.mydiy.R;
import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.channel.ChannelAdapter;
import com.tv.mydiy.channel.ChannelGroup;
import com.tv.mydiy.channel.GroupAdapter;
import com.tv.mydiy.epg.EpgController;
import com.tv.mydiy.epg.EpgManager;
import com.tv.mydiy.menu.MainMenuManager;
import com.tv.mydiy.settings.SettingsManager;
import com.tv.mydiy.settings.SettingModule;
import com.tv.mydiy.ui.UiUpdater;
import com.tv.mydiy.util.HandlerManager;

import java.util.ArrayList;
import java.util.List;

public class ViewInitializerController {

    public interface ViewInitializerListener {
        void onChannelClick(Channel channel, int groupIndex, int channelIndex);
        void onEpgInfoClick(Channel channel);
        void onGroupClick(ChannelGroup group, int groupIndex);
        void onDataChanged();
    }

    private Activity activity;
    private ViewInitializerListener listener;

    private DrawerLayout drawerLayout;
    private RecyclerView channelList;
    private RecyclerView groupList;
    private TextView channelInfo;
    private RelativeLayout splashScreen;
    private ProgressBar loadingProgress;
    private LinearLayout channelInfoBar;
    private TextView currentProgramTitle;
    private TextView nextProgramTitle;
    private TextView sourceInfoText;
    private ImageView channelInfoLogo;
    private TextView timeDisplay;
    private TextView networkSpeed;
    private View leftSubDrawer;
    private View leftEpgDrawer;
    private RecyclerView epgProgramList;
    private TextView epgDrawerTitle;

    private List<ChannelGroup> channelGroups;
    private List<ChannelGroup> channelGroupsForMenu;
    private ChannelAdapter channelAdapter;
    private GroupAdapter groupAdapter;
    private EpgController epgController;
    private EpgManager epgManager;
    private SettingsManager settingsManager;
    private MainMenuManager mainMenuManager;
    private SettingModule settingModule;
    private UiUpdater uiUpdater;
    private HandlerManager handlerManager;

    public ViewInitializerController(Activity activity) {
        this.activity = activity;
        this.channelGroups = new ArrayList<>();
        this.channelGroupsForMenu = new ArrayList<>();
    }

    public void setListener(ViewInitializerListener listener) {
        this.listener = listener;
    }

    public void setSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    public void setMainMenuManager(MainMenuManager mainMenuManager) {
        this.mainMenuManager = mainMenuManager;
    }

    public void setSettingModule(SettingModule settingModule) {
        this.settingModule = settingModule;
    }

    public void setHandlerManager(HandlerManager handlerManager) {
        this.handlerManager = handlerManager;
    }

    public DrawerLayout getDrawerLayout() {
        return drawerLayout;
    }

    public RecyclerView getChannelList() {
        return channelList;
    }

    public RecyclerView getGroupList() {
        return groupList;
    }

    public TextView getChannelInfo() {
        return channelInfo;
    }

    public RelativeLayout getSplashScreen() {
        return splashScreen;
    }

    public ProgressBar getLoadingProgress() {
        return loadingProgress;
    }

    public LinearLayout getChannelInfoBar() {
        return channelInfoBar;
    }

    public TextView getCurrentProgramTitle() {
        return currentProgramTitle;
    }

    public TextView getNextProgramTitle() {
        return nextProgramTitle;
    }

    public TextView getSourceInfoText() {
        return sourceInfoText;
    }

    public ImageView getChannelInfoLogo() {
        return channelInfoLogo;
    }

    public TextView getTimeDisplay() {
        return timeDisplay;
    }

    public TextView getNetworkSpeed() {
        return networkSpeed;
    }

    public View getLeftSubDrawer() {
        return leftSubDrawer;
    }

    public View getLeftEpgDrawer() {
        return leftEpgDrawer;
    }

    public RecyclerView getEpgProgramList() {
        return epgProgramList;
    }

    public TextView getEpgDrawerTitle() {
        return epgDrawerTitle;
    }

    public List<ChannelGroup> getChannelGroups() {
        return channelGroups;
    }

    public List<ChannelGroup> getChannelGroupsForMenu() {
        return channelGroupsForMenu;
    }

    public ChannelAdapter getChannelAdapter() {
        return channelAdapter;
    }

    public GroupAdapter getGroupAdapter() {
        return groupAdapter;
    }

    public EpgController getEpgController() {
        return epgController;
    }

    public EpgManager getEpgManager() {
        return epgManager;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public MainMenuManager getMainMenuManager() {
        return mainMenuManager;
    }

    public SettingModule getSettingModule() {
        return settingModule;
    }

    public UiUpdater getUiUpdater() {
        return uiUpdater;
    }

    public HandlerManager getHandlerManager() {
        return handlerManager;
    }

    public void initDisplayViews() {
        channelInfo = findViewById(R.id.channel_info);
        splashScreen = findViewById(R.id.splash_screen);
        loadingProgress = findViewById(R.id.loading_progress);
        channelInfoBar = findViewById(R.id.channel_info_bar);
        currentProgramTitle = findViewById(R.id.current_program_title);
        nextProgramTitle = findViewById(R.id.next_program_title);
        sourceInfoText = findViewById(R.id.source_info_text);
        channelInfoLogo = findViewById(R.id.channel_info_logo);
        timeDisplay = findViewById(R.id.time_display);
        networkSpeed = findViewById(R.id.network_speed);
    }

    public void initMenuViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        groupList = findViewById(R.id.group_list);
        channelList = findViewById(R.id.channel_list);
        leftSubDrawer = findViewById(R.id.left_sub_drawer);
        leftEpgDrawer = findViewById(R.id.left_epg_drawer);
        epgProgramList = findViewById(R.id.epg_program_list);
        epgDrawerTitle = findViewById(R.id.epg_drawer_title);

        if (drawerLayout != null) {
            drawerLayout.setScrimColor(android.graphics.Color.TRANSPARENT);
        }

        View leftDrawerContainer = findViewById(R.id.left_drawer_container);
        if (leftDrawerContainer != null) {
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) leftDrawerContainer.getLayoutParams();
            params.height = DrawerLayout.LayoutParams.MATCH_PARENT;
            leftDrawerContainer.setLayoutParams(params);
        }
    }

    public void setupDrawerFocus() {
        View rightSubDrawer = findViewById(R.id.right_sub_drawer);
        if (rightSubDrawer != null) {
            rightSubDrawer.setFocusable(true);
            rightSubDrawer.setClickable(true);
        }

        LinearLayout leftDrawer = findViewById(R.id.left_drawer);
        if (leftDrawer != null) {
            leftDrawer.setFocusable(true);
            leftDrawer.setClickable(true);
        }
    }

    public void initChannelAdapters() {
        if (channelGroups != null) {
            channelAdapter = new ChannelAdapter(channelGroups, new ChannelAdapter.OnChannelClickListener() {
                @Override
                public void onChannelClick(Channel channel, int groupIndex, int channelIndex) {
                    if (listener != null) {
                        listener.onChannelClick(channel, groupIndex, channelIndex);
                    }
                }

                @Override
                public void onEpgInfoClick(Channel channel) {
                    if (listener != null) {
                        listener.onEpgInfoClick(channel);
                    }
                }
            }, settingsManager, activity.getApplicationContext());

            if (listener != null) {
                channelAdapter.setOnDataChangedListener(listener::onDataChanged);
            }
        }
    }

    public void initEpgController() {
        if (channelAdapter != null) {
            epgManager = new EpgManager(activity);
            epgController = new EpgController(activity, epgManager, settingsManager, channelAdapter, null);
        }
    }

    public void initUiUpdater() {
        if (timeDisplay != null && networkSpeed != null && channelAdapter != null && epgController != null) {
            uiUpdater = new UiUpdater(timeDisplay, networkSpeed, channelAdapter, epgController, settingsManager, handlerManager);
        }
    }

    public void initGroupAdapter() {
        if (channelGroupsForMenu != null) {
            groupAdapter = new GroupAdapter(channelGroupsForMenu, (group, groupIndex) -> {
                if (listener != null) {
                    listener.onGroupClick(group, groupIndex);
                }
            });
        }
    }

    public void setupRecyclerViews() {
        if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
            mainMenuManager.getChannelMenuManager().setChannelGroups(channelGroupsForMenu);
            mainMenuManager.getChannelMenuManager().setChannelAdapter(channelAdapter);
        }

        if (channelList != null && channelAdapter != null) {
            channelList.setLayoutManager(new LinearLayoutManager(activity));
            channelList.setItemViewCacheSize(20);
            channelList.setDrawingCacheEnabled(true);
            channelList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
            channelList.setAdapter(channelAdapter);
        }

        if (groupList != null && groupAdapter != null) {
            LinearLayoutManager groupLayoutManager = new LinearLayoutManager(activity);
            groupList.setLayoutManager(groupLayoutManager);
            groupList.setAdapter(groupAdapter);
            groupList.setFocusable(true);
            groupList.setDescendantFocusability(RecyclerView.FOCUS_AFTER_DESCENDANTS);
        }
    }

    public void expandFirstChannelGroup() {
        if (channelGroups != null && !channelGroups.isEmpty()) {
            channelGroups.get(0).setExpanded(true);
        }
    }

    private <T extends View> T findViewById(int id) {
        return activity.findViewById(id);
    }
}
