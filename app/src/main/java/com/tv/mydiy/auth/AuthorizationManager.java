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
    private static final long VALIDITY_PERIOD = 365L * 24 * 60 * 60 * 1000; // 365天的有效期

    private final Context context;
    private final Handler handler;
    // 60分钟检查一次

    public AuthorizationManager(Context context) {
        this.context = context;
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 延迟检查并显示授权对话框，遵循授权弹窗延迟初始化规范
     */
    public void checkAndShowAuthorizationDialog() {
        // 根据授权弹窗延迟初始化规范，使用handler.postDelayed()延后执行
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
        // 使用自定义样式创建对话框，实现蓝色半透明背景
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AuthDialogStyle);
        builder.setTitle("应用授权验证");
        
        // 检查是否是首次授权或授权已过期
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

        // 检查是否在有效期内
        long currentTime = System.currentTimeMillis();
        return (currentTime - authDate) < VALIDITY_PERIOD;
    }

    private void updateAuthorizationDate() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putLong(KEY_AUTH_DATE, System.currentTimeMillis()).apply();
    }

    // 提取密码计算逻辑为独立方法，保持原有逻辑不变
    private String calculateCorrectPassword() {
        try {
            // 固定基准日期 2007年11月5日 (UTC时间)
            Calendar baseDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            baseDate.set(2007, 10, 5, 0, 0, 0); // 注意：月份从0开始，所以11月是10
            baseDate.set(Calendar.MILLISECOND, 0);

            // 当前日期 (UTC时间)
            Calendar currentDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            // 计算日期差值（天数）
            long baseTime = baseDate.getTimeInMillis();
            long currentTime = currentDate.getTimeInMillis();
            long diffMillis = currentTime - baseTime;
            int dateDiff = (int) (diffMillis / (24 * 60 * 60 * 1000)); // 转换为天数

            // 使用SHA-256哈希算法
            String hashInput = String.valueOf(dateDiff);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(hashInput.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String hexDig = hexString.toString();

            // 取哈希值中间6位数字
            int middleIndex = hexDig.length() / 2;
            String middlePart = hexDig.substring(Math.max(0, middleIndex - 3),
                    Math.min(hexDig.length(), middleIndex + 3));

            // 确保是数字（提取数字字符）
            StringBuilder digitChars = new StringBuilder();
            for (char c : middlePart.toCharArray()) {
                if (Character.isDigit(c)) {
                    digitChars.append(c);
                }
            }

            // 如果数字不足6位，补充更多数字
            int attempts = 0;
            while (digitChars.length() < 6 && attempts < 10) {
                // 生成补充哈希
                String additionalInput = hashInput + digitChars.length() + attempts;
                byte[] additionalHashBytes = digest.digest(additionalInput.getBytes(java.nio.charset.StandardCharsets.UTF_8));

                StringBuilder additionalHex = new StringBuilder();
                for (byte b : additionalHashBytes) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        additionalHex.append('0');
                    }
                    additionalHex.append(hex);
                }

                // 从补充哈希中提取数字
                for (char c : additionalHex.toString().toCharArray()) {
                    if (Character.isDigit(c) && digitChars.length() < 6) {
                        digitChars.append(c);
                    }
                }

                attempts++;
            }

            // 获取最终密码（最多6位数字）
            return digitChars.length() > 6 ?
                    digitChars.substring(0, 6) :
                    digitChars.toString();
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

    /**
     * 销毁资源，移除所有待处理的回调
     */
    public void destroy() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}