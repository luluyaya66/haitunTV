package com.tv.mydiy.util;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * UI 事件处理工具类，用于减少匿名内部类和 Lambda 表达式的使用
 * 提供常用的 UI 事件处理方法，提高代码复用性
 */
public class UiEventUtils {
    
    /**
     * 为 RecyclerView 设置焦点，如果子视图不可用则等待布局完成后设置焦点
     * @param recyclerView RecyclerView 实例
     * @param position 要设置焦点的项目位置
     */
    public static void setFocusWithLayoutChange(RecyclerView recyclerView, int position) {
        if (recyclerView == null) return;
        
        // 尝试获取焦点的子视图，如果不存在则等待布局完成
        View targetChild = recyclerView.getChildAt(position);
        if (targetChild != null) {
            targetChild.requestFocus();
        } else {
            // 等待布局完成后设置焦点
            recyclerView.addOnLayoutChangeListener(new RecyclerViewOnLayoutChangeListener(position, recyclerView));
        }
    }
    
    /**
     * 为 RecyclerView 设置焦点到第一个项目，如果子视图不可用则等待布局完成后设置焦点
     * @param recyclerView RecyclerView 实例
     */
    public static void setFocusToFirstWithLayoutChange(RecyclerView recyclerView) {
        setFocusWithLayoutChange(recyclerView, 0);
    }
    
    /**
     * RecyclerView 布局变化监听器内部类，用于在布局完成后设置焦点
     */
    private static class RecyclerViewOnLayoutChangeListener implements RecyclerView.OnLayoutChangeListener {
        private final int position;
        private final RecyclerView recyclerView;
        
        public RecyclerViewOnLayoutChangeListener(int position, RecyclerView recyclerView) {
            this.position = position;
            this.recyclerView = recyclerView;
        }
        
        @Override
        public void onLayoutChange(@NonNull View view, int left, int top, int right, int bottom, 
                                 int oldLeft, int oldTop, int oldRight, int oldBottom) {
            View targetChild = recyclerView.getChildAt(position);
            if (targetChild != null) {
                targetChild.requestFocus();
            } else {
                // 如果目标位置的子视图不可用，获取第一个可用子视图的焦点
                View firstChild = recyclerView.getChildAt(0);
                if (firstChild != null) {
                    firstChild.requestFocus();
                }
            }
            // 移除监听器以避免内存泄漏
            recyclerView.removeOnLayoutChangeListener(this);
        }
    }

    /**
     * 滚动到指定位置并设置焦点的便捷方法
     * @param recyclerView RecyclerView 实例
     * @param position 要滚动到的位置
     */
    public static void scrollToAndFocus(RecyclerView recyclerView, int position) {
        if (recyclerView == null) return;
        
        recyclerView.scrollToPosition(position);
        setFocusWithLayoutChange(recyclerView, position);
    }
    
    /**
     * 滚动到顶部并设置焦点的便捷方法
     * @param recyclerView RecyclerView 实例
     */
    public static void scrollToTopAndFocus(RecyclerView recyclerView) {
        scrollToAndFocus(recyclerView, 0);
    }
    
    /**
     * 创建一个 Runnable 任务，在 UI 线程上执行指定的操作
     * @param runnable 要执行的 Runnable 操作
     * @return 包装后的 Runnable
     */
    public static Runnable createPostRunnable(Runnable runnable) {
        return runnable;
    }
    
    /**
     * 在 UI 线程上执行指定的 Runnable 任务
     * @param view View 实例，用于调用 post 方法
     * @param runnable 要执行的 Runnable 操作
     */
    public static void postToUiThread(View view, Runnable runnable) {
        if (view == null || runnable == null) return;
        
        view.post(createPostRunnable(runnable));
    }
    
    /**
     * 在 UI 线程上执行指定的 Runnable 任务
     * @param handler Handler 实例，用于调用 post 方法
     * @param runnable 要执行的 Runnable 操作
     */
    public static void postToUiThread(android.os.Handler handler, Runnable runnable) {
        if (handler == null || runnable == null) return;
        
        handler.post(createPostRunnable(runnable));
    }
}