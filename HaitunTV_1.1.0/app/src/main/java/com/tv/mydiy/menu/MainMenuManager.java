package com.tv.mydiy.menu;

import android.app.Activity;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.tv.mydiy.R;

/**
 * 主菜单管理器，协调所有菜单的显示和隐藏
 */
public class MainMenuManager {
    private final Activity activity;
    private DrawerLayout drawerLayout;
    
    private final ChannelMenuManager channelMenuManager;
    private final SettingsMenuManager settingsMenuManager;
    
    public MainMenuManager(Activity activity) {
        this.activity = activity;
        this.channelMenuManager = new ChannelMenuManager(activity);
        this.settingsMenuManager = new SettingsMenuManager(activity);
    }
    
    public void initMenuViews() {
        drawerLayout = activity.findViewById(R.id.drawer_layout);
        
        channelMenuManager.initMenuViews();
        settingsMenuManager.initMenuViews();
        
        // 设置DrawerLayout的scrim颜色为透明，避免菜单打开时整个屏幕变暗
        if (drawerLayout != null) {
            drawerLayout.setScrimColor(android.graphics.Color.TRANSPARENT);
        }
    }
    
    public ChannelMenuManager getChannelMenuManager() {
        return channelMenuManager;
    }
    
    public SettingsMenuManager getSettingsMenuManager() {
        return settingsMenuManager;
    }
    
    /**
     * 检查是否有任何菜单可见
     */
    public boolean isAnyMenuVisible() {
        boolean isLeftDrawerOpen = drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START);
        boolean isRightDrawerOpen = drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END);
        boolean isChannelMenuVisible = channelMenuManager.isMenuVisible();
        boolean isSettingsMenuVisible = settingsMenuManager.isMenuVisible();
        boolean isSubSettingsVisible = settingsMenuManager.isSubSettingsVisible();
        
        return isLeftDrawerOpen || isRightDrawerOpen || 
               isChannelMenuVisible || isSettingsMenuVisible || isSubSettingsVisible;
    }

}