package com.tv.mydiy.ui.controller;

import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.channel.ChannelAdapter;
import com.tv.mydiy.channel.ChannelGroup;
import com.tv.mydiy.channel.GroupAdapter;
import com.tv.mydiy.menu.MainMenuManager;
import com.tv.mydiy.player.SimplePlayerController;
import com.tv.mydiy.ui.MainActivity;
import com.tv.mydiy.ui.viewmodel.PlayerViewModel;
import com.tv.mydiy.util.Constants;
import com.tv.mydiy.util.UiEventUtils;

import java.util.List;

public class KeyEventController {
    private static final String TAG = "KeyEventController";

    private MainActivity activity;
    private Handler longPressHandler;
    private Runnable longPressRunnable;
    private static final long LONG_PRESS_DELAY = Constants.MAIN_ACTIVITY_LONG_PRESS_DELAY;

    private KeyEventListener listener;
    private SimplePlayerController playerController;
    private PlayerViewModel playerViewModel;

    public interface KeyEventListener {
        boolean isAnyMenuVisible();
        void changeChannel(int direction);
        void changeSource(int direction);
        boolean handleMenuKey();
        boolean handleBack();
        void hideChannelInfoBar();
        void handleLongPressFavorite();
        void handleEnterOrCenter();
        boolean isSettingModuleSubSettingsVisible();
        boolean isDrawerOpen(int gravity);
        void closeDrawer(int gravity);
        void openDrawer(int gravity);
        void requestSettingsListFocus();
        void requestSettingsDetailListFocus();
        void requestPlayerContainerFocus();
        void scrollSettingsListToFirst();
        boolean isMainMenuManagerChannelMenuManagerNull();
        void hideChannelMenu();
        void hideSubSettings();
        void showSettingsMenu();
        
        MainMenuManager getMainMenuManager();
        RecyclerView getGroupList();
        List<ChannelGroup> getChannelGroups();
        int getCurrentGroupIndex();
        void setCurrentGroupIndex(int index);
        GroupAdapter getGroupAdapter();
        void showChannelList();
        void handleChannelListFocusTransfer(RecyclerView channelList);
        RecyclerView getChannelList();
        ChannelAdapter getChannelAdapter();
        void playChannel(Channel channel);
        void hideChannelListFromMainMenuManager();
        void setLeftSubDrawerVisibility(int visibility);
        void showChannelInfoBar(Channel channel);
        Channel getCurrentChannel();
        RecyclerView getEpgProgramList();
        void hideEpgDrawerFromMainMenuManager();
        void setLeftEpgDrawerVisibility(int visibility);
        boolean isLeftSubDrawerVisible();
        boolean isLeftEpgDrawerVisible();
        
        void updateChannelInfo(Channel channel);
        void saveLastPlayedChannel(int groupIndex, int channelIndex);
        int findGroupIndexForChannel(Channel channel);
        int findChannelIndexInGroup(int groupIndex, Channel channel);
        void recordFirstChannelChange();
        void checkAuthorizationIfNeeded();
        void runOnUiThread(Runnable runnable);
        
        void showEpgProgramList(Channel channel);
    }

    public KeyEventController() {
        this.longPressHandler = new Handler(Looper.getMainLooper());
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public void setListener(KeyEventListener listener) {
        this.listener = listener;
    }

    public void setPlayerController(SimplePlayerController playerController) {
        this.playerController = playerController;
    }

    public void setPlayerViewModel(PlayerViewModel playerViewModel) {
        this.playerViewModel = playerViewModel;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) && 
            event.getRepeatCount() == 0) {
            if (listener != null) {
                listener.hideChannelInfoBar();
            }
            longPressRunnable = () -> {
                if (listener != null) {
                    listener.handleLongPressFavorite();
                }
            };
            longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_DELAY);
            return true;
        }

        if (keyCode == 82 || keyCode == 187 || keyCode == 168) {
            if (listener != null) {
                return listener.handleMenuKey();
            }
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                return handleDpadUp();
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return handleDpadDown();
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return handleDpadLeft();
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return handleDpadRight();
            case KeyEvent.KEYCODE_BACK:
                if (listener != null) {
                    return listener.handleBack();
                }
                return false;
            default:
                return false;
        }
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
            boolean wasLongPress = longPressRunnable == null;
            if (longPressRunnable != null) {
                longPressHandler.removeCallbacks(longPressRunnable);
                longPressRunnable = null;
            }
            if (!wasLongPress && listener != null) {
                listener.handleEnterOrCenter();
            }
            return true;
        }
        return false;
    }

    private boolean handleDpadUp() {
        boolean isAnyMenuVisibleUp = false;
        if (listener != null) {
            isAnyMenuVisibleUp = listener.isAnyMenuVisible();
        }

        if (isAnyMenuVisibleUp) {
            return false;
        }

        if (listener != null) {
            listener.changeChannel(-1);
        }
        return true;
    }

    private boolean handleDpadDown() {
        boolean isAnyMenuVisibleDown = false;
        if (listener != null) {
            isAnyMenuVisibleDown = listener.isAnyMenuVisible();
        }

        if (isAnyMenuVisibleDown) {
            return false;
        }

        if (listener != null) {
            listener.changeChannel(1);
        }
        return true;
    }

    private boolean handleDpadLeft() {
        boolean isAnyMenuVisibleLeft = false;
        if (listener != null) {
            isAnyMenuVisibleLeft = listener.isAnyMenuVisible();
        }

        if (isAnyMenuVisibleLeft) {
            if (listener != null) {
                if (listener.isSettingModuleSubSettingsVisible()) {
                    listener.requestSettingsDetailListFocus();
                    return true;
                }
            }
            return false;
        }

        if (listener != null) {
            listener.changeSource(-1);
        }
        return true;
    }

    private boolean handleDpadRight() {
        boolean isAnyMenuVisibleRight = false;
        if (listener != null) {
            isAnyMenuVisibleRight = listener.isAnyMenuVisible();
        }

        if (isAnyMenuVisibleRight) {
            if (listener != null) {
                if (listener.isSettingModuleSubSettingsVisible()) {
                    listener.requestSettingsListFocus();
                    return true;
                }
                if (listener.isLeftSubDrawerVisible() && 
                    listener.getChannelList() != null && listener.getChannelAdapter() != null) {
                    View focusedView = listener.getChannelList().getFocusedChild();
                    if (focusedView != null) {
                        int focusedPosition = listener.getChannelList().getChildAdapterPosition(focusedView);
                        if (focusedPosition != RecyclerView.NO_POSITION) {
                            List<ChannelGroup> allChannelGroups = listener.getChannelAdapter().getChannelGroups();
                            if (allChannelGroups != null && !allChannelGroups.isEmpty()) {
                                int absolutePosition = 0;
                                for (int groupIndex = 0; groupIndex < allChannelGroups.size(); groupIndex++) {
                                    ChannelGroup group = allChannelGroups.get(groupIndex);
                                    if (group != null && group.isExpanded()) {
                                        absolutePosition++;
                                        
                                        if (group.getChannels() != null) {
                                            for (int channelIndex = 0; channelIndex < group.getChannels().size(); channelIndex++) {
                                                if (absolutePosition == focusedPosition) {
                                                    Channel selectedChannel = group.getChannel(channelIndex);
                                                    if (selectedChannel != null) {
                                                        listener.showEpgProgramList(selectedChannel);
                                                        return true;
                                                    }
                                                }
                                                absolutePosition++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        if (listener != null) {
            listener.changeSource(1);
        }
        return true;
    }

    public void handleEnterOrCenter() {
        if (listener == null) return;
        
        boolean isAnyMenuVisibleForConfirm;
        if (listener.getMainMenuManager() != null) {
            isAnyMenuVisibleForConfirm = listener.getMainMenuManager().isAnyMenuVisible();
        } else {
            isAnyMenuVisibleForConfirm = listener.isDrawerOpen(GravityCompat.START) ||
                listener.isLeftSubDrawerVisible() || listener.isLeftEpgDrawerVisible() ||
                listener.isDrawerOpen(GravityCompat.END) ||
                listener.isSettingModuleSubSettingsVisible();
        }
        
        if (isAnyMenuVisibleForConfirm) {
            if (listener.getMainMenuManager() != null && listener.getMainMenuManager().getChannelMenuManager() != null && listener.getMainMenuManager().getChannelMenuManager().isMenuVisible()) {
                View focusedView = listener.getGroupList().getFocusedChild();
                if (focusedView != null) {
                    int focusedPosition = listener.getGroupList().getChildAdapterPosition(focusedView);
                    if (focusedPosition != RecyclerView.NO_POSITION && focusedPosition < listener.getChannelGroups().size()) {
                        listener.setCurrentGroupIndex(focusedPosition);
                        listener.getGroupAdapter().setCurrentGroupIndex(listener.getCurrentGroupIndex());
                        listener.showChannelList();
                        listener.handleChannelListFocusTransfer(listener.getChannelList());
                    }
                } else {
                    if (listener.getGroupList() != null) {
                        listener.getGroupList().requestFocus();
                    }
                }
            } else if (listener.isLeftSubDrawerVisible() && 
                     listener.getChannelList() != null && listener.getChannelAdapter() != null) {
                View focusedView = listener.getChannelList().getFocusedChild();
                if (focusedView != null) {
                    int focusedPosition = listener.getChannelList().getChildAdapterPosition(focusedView);
                    if (focusedPosition != RecyclerView.NO_POSITION) {
                        List<ChannelGroup> allChannelGroups = listener.getChannelAdapter().getChannelGroups();
                        if (allChannelGroups != null && !allChannelGroups.isEmpty()) {
                            int absolutePosition = 0;
                            for (int groupIndex = 0; groupIndex < allChannelGroups.size(); groupIndex++) {
                                ChannelGroup group = allChannelGroups.get(groupIndex);
                                if (group != null && group.isExpanded()) {
                                    absolutePosition++;
                                    
                                    if (group.getChannels() != null) {
                                        for (int channelIndex = 0; channelIndex < group.getChannels().size(); channelIndex++) {
                                            if (absolutePosition == focusedPosition) {
                                                Channel selectedChannel = group.getChannel(channelIndex);
                                                if (selectedChannel != null) {
                                                    listener.playChannel(selectedChannel);
                                                    
                                                    if (listener.getMainMenuManager() != null && listener.getMainMenuManager().getChannelMenuManager() != null) {
                                                        listener.hideChannelListFromMainMenuManager();
                                                    } else {
                                                        listener.setLeftSubDrawerVisibility(View.GONE);
                                                    }
                                                    if (listener.isDrawerOpen(GravityCompat.START)) {
                                                        listener.closeDrawer(GravityCompat.START);
                                                        if (listener.getCurrentChannel() != null) {
                                                            listener.showChannelInfoBar(listener.getCurrentChannel());
                                                        }
                                                    }

                                                    listener.requestPlayerContainerFocus();
                                                    return;
                                                }
                                            }
                                            absolutePosition++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (listener.isLeftEpgDrawerVisible() && 
                     listener.getEpgProgramList() != null) {
                View focusedView = listener.getEpgProgramList().getFocusedChild();
                if (focusedView != null) {
                    int focusedPosition = listener.getEpgProgramList().getChildAdapterPosition(focusedView);
                    if (focusedPosition != RecyclerView.NO_POSITION) {
                        if (listener.getMainMenuManager() != null && listener.getMainMenuManager().getChannelMenuManager() != null) {
                            listener.hideEpgDrawerFromMainMenuManager();
                        } else {
                            listener.setLeftEpgDrawerVisibility(View.GONE);
                        }

                        listener.requestPlayerContainerFocus();
                    }
                }
            }
        } else {
            listener.hideChannelInfoBar();
            listener.openDrawer(GravityCompat.START);
            
            if (listener.getGroupList() != null) {
                listener.getGroupList().requestFocus();
                
                listener.getGroupList().post(() -> {
                    if (listener.getGroupList().getAdapter() != null && listener.getGroupList().getAdapter().getItemCount() > 0) {
                        listener.getGroupList().scrollToPosition(0);
                        UiEventUtils.setFocusToFirstWithLayoutChange(listener.getGroupList());
                    }
                });
            }
        }
    }

    public void changeSource(int direction) {
        if (listener == null) return;
        
        Channel currentChannel = listener.getCurrentChannel();
        
        if (currentChannel == null) {
            listener.runOnUiThread(() -> android.widget.Toast.makeText(activity, "无法获取当前播放的频道", android.widget.Toast.LENGTH_SHORT).show());
            return;
        }

        if (playerController != null) {
            listener.recordFirstChannelChange();
            listener.checkAuthorizationIfNeeded();

            if (direction > 0) {
                playerController.switchToNextSource(currentChannel);
                if (playerViewModel != null) {
                    int nextIndex = playerViewModel.getNextSourceIndex(currentChannel);
                    playerViewModel.setCurrentSourceIndex(nextIndex);
                }
            } else if (direction < 0) {
                playerController.switchToPreviousSource(currentChannel);
                if (playerViewModel != null) {
                    int prevIndex = playerViewModel.getPreviousSourceIndex(currentChannel);
                    playerViewModel.setCurrentSourceIndex(prevIndex);
                }
            }

            listener.updateChannelInfo(currentChannel);
            listener.showChannelInfoBar(currentChannel);

            if (listener.getChannelGroups() != null && !listener.getChannelGroups().isEmpty()) {
                int groupIndex = listener.findGroupIndexForChannel(currentChannel);
                if (groupIndex != -1) {
                    int channelIndex = listener.findChannelIndexInGroup(groupIndex, currentChannel);
                    if (channelIndex != -1) {
                        listener.saveLastPlayedChannel(groupIndex, channelIndex);
                    }
                }
            }
        }
    }

    public void release() {
        if (longPressHandler != null) {
            longPressHandler.removeCallbacksAndMessages(null);
        }
        activity = null;
        listener = null;
    }
}
