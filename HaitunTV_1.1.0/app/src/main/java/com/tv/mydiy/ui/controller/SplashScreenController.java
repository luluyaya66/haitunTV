package com.tv.mydiy.ui.controller;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class SplashScreenController {

    public interface SplashScreenListener {
        void onSplashScreenHidden();
    }

    private RelativeLayout splashScreen;
    private ProgressBar loadingProgress;
    private SplashScreenListener listener;
    private boolean isHidden = false;

    private static final long ANIMATION_DURATION = 500;

    public SplashScreenController() {
    }

    public void setSplashScreen(RelativeLayout splashScreen) {
        this.splashScreen = splashScreen;
    }

    public void setLoadingProgress(ProgressBar loadingProgress) {
        this.loadingProgress = loadingProgress;
    }

    public void setListener(SplashScreenListener listener) {
        this.listener = listener;
    }

    public void show() {
        if (splashScreen != null) {
            splashScreen.setVisibility(View.VISIBLE);
            splashScreen.setAlpha(1f);
            isHidden = false;
        }
    }

    public void hide() {
        if (isHidden || splashScreen == null) {
            return;
        }

        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(ANIMATION_DURATION);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                splashScreen.setVisibility(View.GONE);
                splashScreen.setAlpha(1f);
                isHidden = true;
                if (listener != null) {
                    listener.onSplashScreenHidden();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        splashScreen.startAnimation(fadeOut);
    }

    public void showLoadingProgress() {
        if (loadingProgress != null) {
            loadingProgress.setVisibility(View.VISIBLE);
        }
    }

    public void hideLoadingProgress() {
        if (loadingProgress != null) {
            loadingProgress.setVisibility(View.GONE);
        }
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void reset() {
        isHidden = false;
        if (splashScreen != null) {
            splashScreen.setVisibility(View.VISIBLE);
            splashScreen.setAlpha(1f);
        }
    }
}
