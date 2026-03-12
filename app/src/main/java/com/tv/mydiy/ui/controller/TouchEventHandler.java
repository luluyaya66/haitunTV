package com.tv.mydiy.ui.controller;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

public class TouchEventHandler implements View.OnTouchListener {

    public interface TouchEventListener {
        void onSingleClick(float x, float screenWidth);
        void onDoubleClick(float x, float screenWidth);
        void onLongPress(float x, float screenWidth);
    }

    private TouchEventListener listener;
    private Handler longPressHandler;
    private Runnable longPressRunnable;
    private Runnable singleClickRunnable;

    private long lastTouchTime = 0;
    private float lastTouchX = 0;
    private float lastTouchY = 0;
    private boolean isLongPressTriggered = false;

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;
    private static final long LONG_PRESS_DELAY = 800;
    private static final float TOUCH_SLOP = 10f;

    public TouchEventHandler() {
        longPressHandler = new Handler(Looper.getMainLooper());
    }

    public void setListener(TouchEventListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        float screenWidth = v.getWidth();
        long currentTime = System.currentTimeMillis();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isLongPressTriggered = false;
                lastTouchX = x;
                lastTouchY = y;
                startLongPressCheck(x, screenWidth);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (Math.abs(x - lastTouchX) > TOUCH_SLOP || 
                    Math.abs(y - lastTouchY) > TOUCH_SLOP) {
                    cancelLongPressCheck();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isLongPressTriggered) {
                    cancelSingleClickCheck();
                    isLongPressTriggered = false;
                    return true;
                }

                cancelLongPressCheck();

                long timeDelta = currentTime - lastTouchTime;
                if (timeDelta < DOUBLE_CLICK_TIME_DELTA) {
                    cancelSingleClickCheck();
                    if (listener != null) {
                        listener.onDoubleClick(x, screenWidth);
                    }
                    lastTouchTime = 0;
                } else {
                    lastTouchTime = currentTime;
                    scheduleSingleClick(x, screenWidth);
                }
                return true;
        }

        return false;
    }

    private void startLongPressCheck(final float x, final float screenWidth) {
        cancelLongPressCheck();

        longPressRunnable = new Runnable() {
            @Override
            public void run() {
                isLongPressTriggered = true;
                if (listener != null) {
                    listener.onLongPress(x, screenWidth);
                }
            }
        };

        longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_DELAY);
    }

    private void cancelLongPressCheck() {
        if (longPressRunnable != null) {
            longPressHandler.removeCallbacks(longPressRunnable);
            longPressRunnable = null;
        }
    }

    private void scheduleSingleClick(final float x, final float screenWidth) {
        cancelSingleClickCheck();

        singleClickRunnable = new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onSingleClick(x, screenWidth);
                }
            }
        };

        longPressHandler.postDelayed(singleClickRunnable, DOUBLE_CLICK_TIME_DELTA);
    }

    private void cancelSingleClickCheck() {
        if (singleClickRunnable != null) {
            longPressHandler.removeCallbacks(singleClickRunnable);
            singleClickRunnable = null;
        }
    }

    public boolean isLeftSide(float x, float screenWidth) {
        return x < screenWidth / 2;
    }

    public boolean isRightSide(float x, float screenWidth) {
        return x >= screenWidth / 2;
    }

    public void release() {
        cancelLongPressCheck();
        cancelSingleClickCheck();
        longPressHandler = null;
        listener = null;
    }

    public void reset() {
        cancelLongPressCheck();
        cancelSingleClickCheck();
        lastTouchTime = 0;
        isLongPressTriggered = false;
    }
}
