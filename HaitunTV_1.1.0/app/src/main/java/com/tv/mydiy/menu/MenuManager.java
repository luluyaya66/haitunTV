package com.tv.mydiy.menu;

import android.app.Activity;

/**
 * 菜单管理器基类
 */
public abstract class MenuManager {
    protected Activity activity;
    
    public MenuManager(Activity activity) {
        this.activity = activity;
    }
    
    /**
     * 初始化菜单视图
     */
    public abstract void initMenuViews();
    
    /**
     * 隐藏菜单
     */
    public abstract void hideMenu();
    
    /**
     * 检查菜单是否可见
     */
    public abstract boolean isMenuVisible();
}