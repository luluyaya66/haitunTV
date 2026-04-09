package com.tv.mydiy.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.tv.mydiy.R;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.TimeZone;

public class AuthorizationManager {
    private static final String TAG = "AuthorizationManager";
    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_AUTH_DATE = "auth_date";
    private static final long VALIDITY_PERIOD = 365L * 24 * 60 * 60 * 1000; 

    private final Context context;
    private final Handler handler;

    public AuthorizationManager(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void checkAndShowAuthorizationDialog() {
        handler.postDelayed(() -> {
            if (!isAuthorizationValid()) {
                // 在主线程中显示对话框
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(this::showAuthorizationDialog);
                }
            }
        }, 100); // 延迟100毫秒执行
    }

    private void showAuthorizationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AuthDialogStyle);
        builder.setTitle("应用授权验证");

        if (isFirstRun() || !isAuthorizationValid()) {
            String message;
            if (!isAuthorizationValid() && !isFirstRun()) {
                message = "授权有效期已到，需重新授权\n";
            } else {
                message = "请输入授权密码";
            }
            builder.setMessage(message);
        }

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("输入授权码", (dialog, which) -> {
            String password = input.getText().toString().trim();
            if (validatePassword(password)) {
                // 密码正确
                setFirstRunCompleted();
                updateAuthorizationDate();
                Toast.makeText(context, "授权成功", Toast.LENGTH_SHORT).show();
            } else {
                // 密码错误
                Toast.makeText(context, "授权失败，请重新输入", Toast.LENGTH_SHORT).show();
                // 重新显示对话框
                showAuthorizationDialog();
            }
        });

        builder.setNegativeButton("暂时不", (dialog, which) -> {
            dialog.cancel();
            // 用户拒绝授权，但1小时后会再次提醒
        });

        builder.setCancelable(false); // 要止取消对话框
        builder.show();
    }

    private boolean isFirstRun() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean("first_run", true);
    }

    private void setFirstRunCompleted() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean("first_run", false).apply();
    }

    public boolean isAuthorizationValid() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long authDate = prefs.getLong(KEY_AUTH_DATE, 0);

        // 如果没有授权日期，说明从未授权
        if (authDate == 0) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - authDate) < VALIDITY_PERIOD;
    }

    private void updateAuthorizationDate() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_AUTH_DATE, System.currentTimeMillis()).apply();
    }

    // 提取密码计算逻辑为独立方法，保持原有逻辑不变, 此处删除发布，可自行设计密码规则
    private String calculateCorrectPassword() {
        try {
            
                attempts++;
            }

        } catch (Exception e) {
            Log.e(TAG, "计算正确密码时出错", e);
            return "ERROR";
        }
    }

    private boolean validatePassword(String inputPassword) {
        try {
            String correctPassword = calculateCorrectPassword();
            // 验证输入密码
            return inputPassword.equals(correctPassword);
        } catch (Exception e) {
            Log.e(TAG, "密码验证出错", e);
            return false;
        }
    }

    public void destroy() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

}
