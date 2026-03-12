package com.tv.mydiy.menu;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

import com.tv.mydiy.R;
import com.tv.mydiy.util.UiUtils;

/**
 * 设置菜单管理器
 */
public class SettingsMenuManager extends MenuManager {
    private FrameLayout rightDrawer;
    private FrameLayout rightSubDrawer;

    public SettingsMenuManager(Activity activity) {
        super(activity);
    }
    
    @Override
    public void initMenuViews() {
        View view = activity.findViewById(R.id.right_drawer);
        if (view instanceof FrameLayout) {
            rightDrawer = (FrameLayout) view;
        }
        
        view = activity.findViewById(R.id.right_sub_drawer);
        if (view instanceof FrameLayout) {
            rightSubDrawer = (FrameLayout) view;
        }
    }

    @Override
    public void hideMenu() {
        if (rightDrawer != null) {
            UiUtils.updateVisibilitySafely(rightDrawer, View.GONE);
        }
        hideSubSettings();
    }

    public void hideSubSettings() {
        if (rightSubDrawer != null) {
            UiUtils.updateVisibilitySafely(rightSubDrawer, View.GONE);
        }
    }
    
    public boolean isSubSettingsVisible() {
        return rightSubDrawer != null && rightSubDrawer.getVisibility() == View.VISIBLE;
    }
    
    @Override
    public boolean isMenuVisible() {
        return rightDrawer != null && rightDrawer.getVisibility() == View.VISIBLE;
    }
    
    public void showMenu() {
        if (rightDrawer != null) {
            UiUtils.updateVisibilitySafely(rightDrawer, View.VISIBLE);
        }
    }
    
}