package com.tv.mydiy.ui.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tv.mydiy.R;
import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.epg.EpgController;
import com.tv.mydiy.epg.EpgManager;
import com.tv.mydiy.epg.EpgProgram;

import java.util.List;

public class ChannelInfoBarController {

    public interface ChannelInfoBarListener {
        void onInfoBarHidden();
        void onLoadLogo(ImageView imageView, String logoUrl);
    }

    private LinearLayout channelInfoBar;
    private TextView channelInfo;
    private TextView currentProgramTitle;
    private TextView nextProgramTitle;
    private TextView sourceInfoText;
    private ImageView channelInfoLogo;

    private EpgController epgController;
    private ChannelInfoBarListener listener;
    private Handler handler;
    private Runnable hideRunnable;
    private Context context;
    private boolean isFirstPlay = true;

    private static final long DISPLAY_TIME = 3000;

    public ChannelInfoBarController(Context context) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
        hideRunnable = this::hide;
    }

    public void setEpgController(EpgController epgController) {
        this.epgController = epgController;
    }

    public void setChannelInfoBar(LinearLayout channelInfoBar) {
        this.channelInfoBar = channelInfoBar;
    }

    public void setChannelInfo(TextView channelInfo) {
        this.channelInfo = channelInfo;
    }

    public void setCurrentProgramTitle(TextView currentProgramTitle) {
        this.currentProgramTitle = currentProgramTitle;
    }

    public void setNextProgramTitle(TextView nextProgramTitle) {
        this.nextProgramTitle = nextProgramTitle;
    }

    public void setSourceInfoText(TextView sourceInfoText) {
        this.sourceInfoText = sourceInfoText;
    }

    public void setChannelInfoLogo(ImageView channelInfoLogo) {
        this.channelInfoLogo = channelInfoLogo;
    }

    public void setListener(ChannelInfoBarListener listener) {
        this.listener = listener;
    }

    public void setFirstPlay(boolean isFirstPlay) {
        this.isFirstPlay = isFirstPlay;
    }

    public void showChannelInfoBar(Channel channel) {
        if (isFirstPlay) {
            return;
        }

        if (channel == null || channelInfoBar == null) {
            return;
        }

        handler.post(() -> {
            cancelAutoHide();

            updateChannelInfoInternal(channel);
            updateSourceInfoInternal(channel);
            updateEpgProgramsInternal(channel);
            loadChannelLogoInternal(channel);

            channelInfoBar.setVisibility(View.VISIBLE);
            scheduleAutoHide();
        });
    }

    public void show(Channel channel) {
        showChannelInfoBar(channel);
    }

    public void hide() {
        if (channelInfoBar == null) {
            return;
        }

        handler.post(() -> {
            cancelAutoHide();
            channelInfoBar.setVisibility(View.GONE);

            if (listener != null) {
                listener.onInfoBarHidden();
            }
        });
    }

    public void updateChannelInfo(Channel channel) {
        if (channel == null) {
            return;
        }

        handler.post(() -> {
            updateChannelInfoInternal(channel);
        });
    }

    private void updateChannelInfoInternal(Channel channel) {
        if (channelInfo != null) {
            String info = channel.getName();
            if (channel.getSources() != null && channel.getSources().size() > 1) {
                info += " (" + (channel.getCurrentSourceIndex() + 1) + "/" + channel.getSources().size() + ")";
            }
            channelInfo.setText(info);
        }
    }

    private void updateSourceInfoInternal(Channel channel) {
        if (sourceInfoText != null) {
            String sourceInfo = "1/1";
            if (channel.getSources() != null && channel.getSources().size() > 1) {
                sourceInfo = (channel.getCurrentSourceIndex() + 1) + "/" + channel.getSources().size();
            }
            sourceInfoText.setText(sourceInfo);
        }
    }

    private void updateEpgProgramsInternal(Channel channel) {
        EpgProgram currentProgram = null;
        EpgProgram nextProgram = null;

        if (epgController != null) {
            List<EpgProgram> programs = null;
            if (epgController.getEpgManager() != null) {
                programs = epgController.getEpgManager().getAllProgramsForChannel(channel);
            }

            if (programs != null && !programs.isEmpty()) {
                long currentTime = System.currentTimeMillis();
                int currentIndex = -1;

                for (int i = 0; i < programs.size(); i++) {
                    EpgProgram program = programs.get(i);
                    long startTime = parseEpgTimeToMillis(program.getStartTime());
                    long endTime = parseEpgTimeToMillis(program.getEndTime());

                    if (startTime <= currentTime && currentTime <= endTime) {
                        currentIndex = i;
                        currentProgram = program;
                        break;
                    }
                }

                if (currentIndex == -1) {
                    long minNextTime = Long.MAX_VALUE;
                    for (int i = 0; i < programs.size(); i++) {
                        EpgProgram program = programs.get(i);
                        long startTime = parseEpgTimeToMillis(program.getStartTime());
                        if (startTime > currentTime && startTime < minNextTime) {
                            minNextTime = startTime;
                            currentIndex = i;
                            currentProgram = program;
                        }
                    }
                }

                if (currentIndex != -1 && currentIndex + 1 < programs.size()) {
                    nextProgram = programs.get(currentIndex + 1);
                }
            }
        }

        updateProgramInfoInternal(currentProgram, nextProgram);
    }

    private void updateProgramInfoInternal(EpgProgram currentProgram, EpgProgram nextProgram) {
        if (currentProgramTitle != null) {
            String currentTitle = currentProgram != null && currentProgram.getTitle() != null 
                ? "正在播放: " + currentProgram.getTitle() : "正在播放: 暂无节目信息";
            currentProgramTitle.setText(currentTitle);
        }

        if (nextProgramTitle != null) {
            String nextTitle = "";
            if (nextProgram != null) {
                String nextStartTime = EpgManager.formatEpgTimeToHM(nextProgram.getStartTime());
                String nextProgramName = nextProgram.getTitle() != null ? nextProgram.getTitle() : "暂无后续节目";
                if (!nextStartTime.isEmpty()) {
                    nextTitle = "下一节目:" + nextStartTime + " " + nextProgramName;
                } else {
                    nextTitle = "下一节目:" + nextProgramName;
                }
            } else {
                nextTitle = "下一节目:暂无后续节目";
            }
            nextProgramTitle.setText(nextTitle);
        }
    }

    private void loadChannelLogoInternal(Channel channel) {
        if (channelInfoLogo != null) {
            String logoName = "file:///android_asset/logos/" + channel.getName() + ".png";
            
            if (listener != null) {
                listener.onLoadLogo(channelInfoLogo, logoName);
            }
        }
    }

    private long parseEpgTimeToMillis(String epgTime) {
        if (epgTime == null || epgTime.isEmpty()) {
            return 0;
        }

        try {
            String trimmedTime = epgTime.trim();

            String timePart;
            String timezonePart = null;

            if (trimmedTime.contains(" ")) {
                String[] parts = trimmedTime.split("\\s+");
                timePart = parts[0];
                if (parts.length > 1) {
                    timezonePart = parts[1];
                }
            } else {
                timePart = trimmedTime;
            }

            if (timePart.length() >= 14) {
                String year = timePart.substring(0, 4);
                String month = timePart.substring(4, 6);
                String day = timePart.substring(6, 8);
                String hour = timePart.substring(8, 10);
                String minute = timePart.substring(10, 12);
                String second = timePart.substring(12, 14);

                java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.set(java.util.Calendar.YEAR, Integer.parseInt(year));
                calendar.set(java.util.Calendar.MONTH, Integer.parseInt(month) - 1);
                calendar.set(java.util.Calendar.DAY_OF_MONTH, Integer.parseInt(day));
                calendar.set(java.util.Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
                calendar.set(java.util.Calendar.MINUTE, Integer.parseInt(minute));
                calendar.set(java.util.Calendar.SECOND, Integer.parseInt(second));
                calendar.set(java.util.Calendar.MILLISECOND, 0);

                if (timezonePart != null && timezonePart.length() >= 5) {
                    try {
                        char sign = timezonePart.charAt(0);
                        int tzHour = Integer.parseInt(timezonePart.substring(1, 3));
                        int tzMinute = Integer.parseInt(timezonePart.substring(3, 5));
                        int tzOffsetMillis = (tzHour * 60 + tzMinute) * 60 * 1000;
                        if (sign == '+') {
                            calendar.add(java.util.Calendar.MILLISECOND, -tzOffsetMillis);
                        } else {
                            calendar.add(java.util.Calendar.MILLISECOND, tzOffsetMillis);
                        }
                    } catch (Exception e) {
                    }
                }

                return calendar.getTimeInMillis();
            }
        } catch (Exception e) {
        }

        return 0;
    }

    public void updateProgramInfo(String currentTitle, String nextTitle) {
        handler.post(() -> {
            if (currentProgramTitle != null) {
                currentProgramTitle.setText(currentTitle != null ? currentTitle : "正在播放: 暂无节目信息");
            }
            if (nextProgramTitle != null) {
                nextProgramTitle.setText(nextTitle != null ? nextTitle : "下一节目:暂无后续节目");
            }
        });
    }

    private void scheduleAutoHide() {
        handler.postDelayed(hideRunnable, DISPLAY_TIME);
    }

    private void cancelAutoHide() {
        handler.removeCallbacks(hideRunnable);
    }

    public void release() {
        cancelAutoHide();
        handler = null;
        hideRunnable = null;
    }

    public void reset() {
        cancelAutoHide();
        if (channelInfoBar != null) {
            channelInfoBar.setVisibility(View.GONE);
        }
    }
}
