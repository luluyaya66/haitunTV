package com.tv.mydiy.ui.controller;

import android.view.Gravity;
import android.view.View;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;

public class DrawerMenuController {

    public interface DrawerMenuListener {
        void onLeftDrawerOpened();
        void onLeftDrawerClosed();
        void onRightDrawerOpened();
        void onRightDrawerClosed();
    }

    private DrawerLayout drawerLayout;
    private DrawerMenuListener listener;
    private View leftDrawer;
    private View rightDrawer;

    public DrawerMenuController() {
    }

    public void setDrawerLayout(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;
        setupDrawerListener();
    }

    public void setLeftDrawer(View leftDrawer) {
        this.leftDrawer = leftDrawer;
    }

    public void setRightDrawer(View rightDrawer) {
        this.rightDrawer = rightDrawer;
    }

    public void setListener(DrawerMenuListener listener) {
        this.listener = listener;
    }

    private void setupDrawerListener() {
        if (drawerLayout == null) {
            return;
        }

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (listener == null) {
                    return;
                }
                if (drawerView == leftDrawer) {
                    listener.onLeftDrawerOpened();
                } else if (drawerView == rightDrawer) {
                    listener.onRightDrawerOpened();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (listener == null) {
                    return;
                }
                if (drawerView == leftDrawer) {
                    listener.onLeftDrawerClosed();
                } else if (drawerView == rightDrawer) {
                    listener.onRightDrawerClosed();
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    public void openLeftDrawer() {
        if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public void closeLeftDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void toggleLeftDrawer() {
        if (drawerLayout == null) {
            return;
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeLeftDrawer();
        } else {
            openLeftDrawer();
        }
    }

    public void openRightDrawer() {
        if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.openDrawer(GravityCompat.END);
        }
    }

    public void closeRightDrawer() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
    }

    public void toggleRightDrawer() {
        if (drawerLayout == null) {
            return;
        }
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            closeRightDrawer();
        } else {
            openRightDrawer();
        }
    }

    public void closeAllDrawers() {
        closeLeftDrawer();
        closeRightDrawer();
    }

    public boolean isLeftDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public boolean isRightDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END);
    }

    public boolean isAnyDrawerOpen() {
        return isLeftDrawerOpen() || isRightDrawerOpen();
    }

    public void setScrimColor(int color) {
        if (drawerLayout != null) {
            drawerLayout.setScrimColor(color);
        }
    }

    public void setDrawerLockMode(int lockMode, int edgeGravity) {
        if (drawerLayout != null) {
            drawerLayout.setDrawerLockMode(lockMode, edgeGravity);
        }
    }

    public void release() {
        drawerLayout = null;
        leftDrawer = null;
        rightDrawer = null;
        listener = null;
    }
}
