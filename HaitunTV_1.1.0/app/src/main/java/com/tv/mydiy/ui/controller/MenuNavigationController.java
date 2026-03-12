package com.tv.mydiy.ui.controller;

import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.channel.ChannelAdapter;
import com.tv.mydiy.channel.ChannelGroup;
import com.tv.mydiy.channel.GroupAdapter;
import com.tv.mydiy.epg.EpgController;
import com.tv.mydiy.epg.EpgProgramAdapter;
import com.tv.mydiy.menu.MainMenuManager;
import com.tv.mydiy.settings.SettingItem;
import com.tv.mydiy.settings.SettingModule;
import com.tv.mydiy.settings.SettingsAdapter;
import com.tv.mydiy.ui.MainActivity;
import com.tv.mydiy.util.UiEventUtils;

import java.util.ArrayList;
import java.util.List;

public class MenuNavigationController {
    private static final String TAG = "MenuNavigationController";

    private MainActivity activity;
    private MenuNavigationListener listener;

    public interface MenuNavigationListener {
        void playChannel(Channel channel);
        void showChannelInfoBar(Channel channel);
        void requestChannelListFocus();
        void requestGroupListFocus();
        void requestSettingsListFocus();
        void requestSettingsDetailListFocus();
        void requestEpgProgramListFocus();
        void requestPlayerContainerFocus();
        void hideLeftEpgDrawer();
        void hideLeftSubDrawer();
        void closeDrawer(int gravity);
        void openDrawer(int gravity);
        boolean isMainMenuManagerNull();
        boolean isMainMenuManagerChannelMenuManagerNull();
        void hideChannelMenuFromMainMenuManager();
        void hideChannelListFromMainMenuManager();
        void hideEpgDrawerFromMainMenuManager();
        boolean isSettingModuleSubSettingsVisible();
        void hideSubSettingsFromSettingModule();
        void showSubSettingsFromSettingModule(SettingItem item);
        boolean isDrawerOpen(int gravity);
        boolean isLeftEpgDrawerVisible();
        boolean isLeftSubDrawerVisible();
        
        List<ChannelGroup> getChannelGroups();
        int getCurrentGroupIndex();
        ChannelAdapter getChannelAdapter();
        MainMenuManager getMainMenuManager();
        EpgController getEpgController();
        RecyclerView getChannelList();
        void setLeftSubDrawerVisibility(int visibility);
        
        void showEpgProgramList(Channel channel);
    }

    public MenuNavigationController() {
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public void setListener(MenuNavigationListener listener) {
        this.listener = listener;
    }

    public boolean handleDpadUp(RecyclerView settingsDetailList, RecyclerView settingsList,
                                 RecyclerView epgProgramList, RecyclerView channelList,
                                 RecyclerView groupList, SettingModule settingModule,
                                 MainMenuManager mainMenuManager, LinearLayout leftSubDrawer,
                                 LinearLayout leftEpgDrawer) {
        if (listener == null) return false;

        if (listener.isSettingModuleSubSettingsVisible() && settingsDetailList != null) {
            if (!settingsDetailList.hasFocus()) {
                listener.requestSettingsDetailListFocus();
            }
            return false;
        } else if (listener.isDrawerOpen(androidx.core.view.GravityCompat.END) && settingsList != null) {
            if (!settingsList.hasFocus()) {
                listener.requestSettingsListFocus();
            }
            return false;
        } else if (listener.isLeftEpgDrawerVisible() && epgProgramList != null) {
            if (!epgProgramList.hasFocus()) {
                listener.requestEpgProgramListFocus();
            }
            return false;
        } else if (listener.isLeftSubDrawerVisible()) {
            if (groupList != null && groupList.hasFocus()) {
                return false;
            } else if (channelList != null && channelList.hasFocus()) {
                return false;
            } else {
                if (groupList != null) {
                    listener.requestGroupListFocus();
                }
                return false;
            }
        } else if (listener.isDrawerOpen(androidx.core.view.GravityCompat.START) && groupList != null) {
            if (!groupList.hasFocus()) {
                listener.requestGroupListFocus();
            }
            return false;
        }

        return true;
    }

    public boolean handleDpadDown(RecyclerView settingsDetailList, RecyclerView settingsList,
                                   RecyclerView epgProgramList, RecyclerView channelList,
                                   RecyclerView groupList, SettingModule settingModule,
                                   MainMenuManager mainMenuManager, LinearLayout leftSubDrawer,
                                   LinearLayout leftEpgDrawer) {
        if (listener == null) return false;

        if (listener.isSettingModuleSubSettingsVisible() && settingsDetailList != null) {
            if (!settingsDetailList.hasFocus()) {
                listener.requestSettingsDetailListFocus();
            }
            return false;
        } else if (listener.isDrawerOpen(androidx.core.view.GravityCompat.END) && settingsList != null) {
            if (!settingsList.hasFocus()) {
                listener.requestSettingsListFocus();
            }
            return false;
        } else if (listener.isLeftEpgDrawerVisible() && epgProgramList != null) {
            if (!epgProgramList.hasFocus()) {
                listener.requestEpgProgramListFocus();
            }
            return false;
        } else if (listener.isLeftSubDrawerVisible()) {
            if (groupList != null && groupList.hasFocus()) {
                return false;
            } else if (channelList != null && channelList.hasFocus()) {
                return false;
            } else {
                if (groupList != null) {
                    listener.requestGroupListFocus();
                }
                return false;
            }
        } else if (listener.isDrawerOpen(androidx.core.view.GravityCompat.START) && groupList != null) {
            if (!groupList.hasFocus()) {
                listener.requestGroupListFocus();
            }
            return false;
        }

        return true;
    }

    public boolean handleDpadLeft(RecyclerView channelList, RecyclerView groupList,
                                  MainMenuManager mainMenuManager, SettingModule settingModule,
                                  LinearLayout leftSubDrawer, LinearLayout leftEpgDrawer) {
        if (listener == null) return false;

        if (listener.isLeftEpgDrawerVisible()) {
            if (!listener.isMainMenuManagerChannelMenuManagerNull()) {
                listener.hideEpgDrawerFromMainMenuManager();
            } else {
                listener.hideLeftEpgDrawer();
            }
            listener.requestChannelListFocus();
            return true;
        } else if (listener.isLeftSubDrawerVisible()) {
            if (channelList != null && channelList.hasFocus()) {
                if (groupList != null) {
                    listener.requestGroupListFocus();
                    final int currentSelectedPosition = groupList.getAdapter() != null ? 
                        ((GroupAdapter) groupList.getAdapter()).getCurrentGroupIndex() : 0;
                    if (currentSelectedPosition >= 0) {
                        groupList.post(() -> {
                            if (groupList.getAdapter() != null && groupList.getAdapter().getItemCount() > 0) {
                                groupList.scrollToPosition(currentSelectedPosition);
                                assert groupList.getLayoutManager() != null;
                                View focusedChild = groupList.getChildAt(currentSelectedPosition -
                                    ((LinearLayoutManager) groupList.getLayoutManager()).findFirstVisibleItemPosition());
                                if (focusedChild != null) {
                                    focusedChild.requestFocus();
                                } else {
                                    groupList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                        @Override
                                        public void onLayoutChange(View v, int left, int top, int right, int bottom, 
                                                                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                            View newChild = groupList.getChildAt(currentSelectedPosition - 
                                                ((LinearLayoutManager) groupList.getLayoutManager()).findFirstVisibleItemPosition());
                                            if (newChild != null) {
                                                newChild.requestFocus();
                                            }
                                            groupList.removeOnLayoutChangeListener(this);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
                return true;
            } else if (groupList != null && groupList.hasFocus()) {
                if (!listener.isMainMenuManagerChannelMenuManagerNull()) {
                    listener.hideChannelListFromMainMenuManager();
                } else {
                    listener.hideLeftSubDrawer();
                }
                return true;
            }
        } else if (listener.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            if (listener.isLeftSubDrawerVisible()) {
                if (!listener.isMainMenuManagerChannelMenuManagerNull()) {
                    listener.hideChannelListFromMainMenuManager();
                } else {
                    listener.hideLeftSubDrawer();
                }
                listener.requestGroupListFocus();
            } else {
                if (!listener.isMainMenuManagerChannelMenuManagerNull()) {
                    listener.hideChannelMenuFromMainMenuManager();
                } else {
                    listener.closeDrawer(androidx.core.view.GravityCompat.START);
                }
                listener.requestPlayerContainerFocus();
            }
            return true;
        } else if (listener.isSettingModuleSubSettingsVisible()) {
            listener.hideSubSettingsFromSettingModule();
            listener.requestSettingsListFocus();
            return true;
        }

        return true;
    }

    public boolean handleDpadRight(RecyclerView channelList, ChannelAdapter channelAdapter,
                                    List<ChannelGroup> channelGroups, Channel currentChannel,
                                    RecyclerView settingsList, SettingsAdapter settingsAdapter,
                                    SettingModule settingModule, RecyclerView epgProgramList,
                                    EpgProgramAdapter epgProgramAdapter, RecyclerView groupList,
                                    GroupAdapter groupAdapter, MainMenuManager mainMenuManager,
                                    LinearLayout leftSubDrawer, LinearLayout leftEpgDrawer) {
        if (listener == null) return false;

        if (listener.isLeftEpgDrawerVisible()) {
            return true;
        } else if (listener.isLeftSubDrawerVisible()) {
            if (groupList != null && groupList.hasFocus()) {
                if (channelList != null) {
                    listener.requestChannelListFocus();
                    channelList.post(() -> {
                        if (channelAdapter != null && channelAdapter.getItemCount() > 0) {
                            UiEventUtils.scrollToTopAndFocus(channelList);
                        }
                    });
                }
                return true;
            } else if (channelList != null && channelList.hasFocus()) {
                View focusedView = channelList.getFocusedChild();
                if (focusedView != null) {
                    int focusedPosition = channelList.getChildAdapterPosition(focusedView);
                    if (focusedPosition != RecyclerView.NO_POSITION) {
                        List<ChannelGroup> allChannelGroups = channelAdapter.getChannelGroups();
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
                return true;
            }
        }

        return true;
    }

    public boolean handleChannelListFocusTransfer(RecyclerView channelList) {
        if (channelList != null && channelList.hasFocus()) {
            return true;
        } else {
            if (channelList != null) {
                channelList.post(channelList::requestFocus);
            }
            return true;
        }
    }

    public void showChannelList() {
        if (listener == null) return;
        
        if (listener.getChannelGroups() != null && listener.getCurrentGroupIndex() >= 0 && listener.getCurrentGroupIndex() < listener.getChannelGroups().size()) {
            ChannelGroup currentGroup = listener.getChannelGroups().get(listener.getCurrentGroupIndex());
            if (listener.getChannelAdapter() != null && currentGroup != null) {
                List<ChannelGroup> selectedGroupList = new ArrayList<>();
                selectedGroupList.add(currentGroup);
                listener.getChannelAdapter().updateData(selectedGroupList);
                listener.getChannelAdapter().setCurrentGroupIndex(0, -1);
            }
        }
        
        if (listener.getMainMenuManager() != null && listener.getMainMenuManager().getChannelMenuManager() != null) {
            listener.getMainMenuManager().getChannelMenuManager().showChannelList();
            
            if (listener.getEpgController() != null && listener.getChannelAdapter() != null) {
                listener.getEpgController().updateChannelsWithEpg(listener.getChannelAdapter());
            }
            
            if (listener.getChannelList() != null) {
                listener.getChannelList().post(() -> {
                    if (listener.getChannelList().getAdapter() != null && listener.getChannelList().getAdapter().getItemCount() > 0) {
                        UiEventUtils.scrollToTopAndFocus(listener.getChannelList());
                    }
                });
            }
        } else {
            if (listener.getChannelList() != null) {
                listener.setLeftSubDrawerVisibility(View.VISIBLE);
                
                if (listener.getEpgController() != null && listener.getChannelAdapter() != null) {
                    listener.getEpgController().updateChannelsWithEpg(listener.getChannelAdapter());
                }
                
                if (listener.getChannelList() != null) {
                    listener.getChannelList().post(() -> {
                        if (listener.getChannelList().getAdapter() != null && listener.getChannelList().getAdapter().getItemCount() > 0) {
                            UiEventUtils.scrollToTopAndFocus(listener.getChannelList());
                        }
                    });
                }
            }
        }
    }

    public void release() {
        activity = null;
        listener = null;
    }
}
