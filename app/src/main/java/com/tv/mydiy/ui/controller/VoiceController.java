package com.tv.mydiy.ui.controller;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;

import com.peasun.aispeech.aiopen.AIOpenService;
import com.peasun.aispeech.aiopen.AIOpenUtils;
import com.tv.mydiy.util.LogUtil;

public class VoiceController {
    private static final String TAG = "VoiceController";

    private Context context;
    private VoiceListener listener;
    private BroadcastReceiver aiOpenReceiver;

    public interface VoiceListener {
        Context getContext();
    }

    public VoiceController() {
    }

    public void setListener(VoiceListener listener) {
        this.listener = listener;
        if (listener != null) {
            this.context = listener.getContext();
        }
    }

    public void initializeXiajieVoice() {
        try {
            if (context == null) {
                LogUtil.e(TAG, "Context is null, cannot initialize Xiajie Voice");
                return;
            }

            AIOpenUtils.registerLiveTvApp(context);

            LogUtil.d(TAG, "注册AI语音接收器");
            aiOpenReceiver = AIOpenUtils.registerLiveTvReciver(context);

            Intent intent = new Intent(context, AIOpenService.class);
            context.startService(intent);
            
            LogUtil.d(TAG, "夏杰语音支持初始化完成");
        } catch (Exception e) {
            LogUtil.e(TAG, "初始化夏杰语音支持时出错: " + e.getMessage(), e);
        }
    }

    public void release() {
        context = null;
        listener = null;
        aiOpenReceiver = null;
    }
}
