package com.tv.mydiy.menu;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tv.mydiy.R;
import com.tv.mydiy.channel.ChannelAdapter;
import com.tv.mydiy.channel.ChannelGroup;
import com.tv.mydiy.channel.GroupAdapter;
import com.tv.mydiy.util.UiUtils;

import java.util.List;

/**
 * 频道菜单管理器
 */
public class ChannelMenuManager extends MenuManager {
    private RecyclerView groupList;
    private RecyclerView channelList;
    private LinearLayout leftSubDrawer;
    private LinearLayout leftEpgDrawer;
    
    private androidx.drawerlayout.widget.DrawerLayout drawerLayout;

    private List<ChannelGroup> channelGroups;
    
    public ChannelMenuManager(Activity activity) {
        super(activity);
    }
    
    @Override
    public void initMenuViews() {
        groupList = activity.findViewById(R.id.group_list);
        channelList = activity.findViewById(R.id.channel_list);
        leftSubDrawer = activity.findViewById(R.id.left_sub_drawer);
        leftEpgDrawer = activity.findViewById(R.id.left_epg_drawer);
        drawerLayout = activity.findViewById(R.id.drawer_layout);
    }
    
    public void setChannelGroups(List<ChannelGroup> channelGroups) {
        this.channelGroups = channelGroups;
        initAdapters();
    }
    
    private void initAdapters() {
        if (channelGroups != null) {
            // 创建一个空的组点击监听器，因为 ChannelMenuManager 本身不处理组点击事件
            GroupAdapter.OnGroupClickListener emptyGroupClickListener = (group, groupIndex) -> {
                // 空实现，实际的组点击处理在 MainActivity 中
            };
            GroupAdapter groupAdapter = new GroupAdapter(channelGroups, emptyGroupClickListener);
            if (groupList != null) {
                groupList.setLayoutManager(new LinearLayoutManager(activity));
                groupList.setAdapter(groupAdapter);
            }
        }
    }
    
    public void setChannelAdapter(ChannelAdapter channelAdapter) {
        if (channelList != null && channelAdapter != null) {
            channelList.setLayoutManager(new LinearLayoutManager(activity));
            channelList.setAdapter(channelAdapter);
        }
    }
    
    public void showChannelList() {
        if (leftSubDrawer != null) {
            UiUtils.updateVisibilitySafely(leftSubDrawer, View.VISIBLE);
        }
    }
    
    public void hideChannelList() {
        if (leftSubDrawer != null) {
            UiUtils.updateVisibilitySafely(leftSubDrawer, View.GONE);
        }
    }

    public void hideEpgDrawer() {
        if (leftEpgDrawer != null) {
            UiUtils.updateVisibilitySafely(leftEpgDrawer, View.GONE);
        }
    }
    
    @Override
    public void hideMenu() {
        hideChannelList();
        hideEpgDrawer();
        
        // 如果主菜单（一级菜单）是打开的，也要关闭它
        if (drawerLayout != null && drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
        }
    }
    
    @Override
    public boolean isMenuVisible() {
        // 检查主菜单（一级菜单）是否打开，或者二级菜单是否可见
        return (drawerLayout != null && drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) ||
               (leftSubDrawer != null && leftSubDrawer.getVisibility() == View.VISIBLE);
    }
}