package com.tv.mydiy.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tv.mydiy.R;
import com.tv.mydiy.auth.AuthorizationManager;
import com.tv.mydiy.channel.Channel;
import com.tv.mydiy.channel.ChannelAdapter;
import com.tv.mydiy.channel.ChannelGroup;
import com.tv.mydiy.channel.GroupAdapter;
import com.tv.mydiy.epg.EpgController;
import com.tv.mydiy.epg.EpgManager;
import com.tv.mydiy.epg.EpgProgramAdapter;
import com.tv.mydiy.network.NetworkManager;
import com.tv.mydiy.player.IjkPlayerAdapter;
import com.tv.mydiy.player.SimplePlayerController;
import com.tv.mydiy.menu.MainMenuManager;
import com.tv.mydiy.settings.FavoriteManager;
import com.tv.mydiy.settings.SettingModule;
import com.tv.mydiy.settings.SettingsManager;
import com.tv.mydiy.settings.SettingItem;
import com.tv.mydiy.util.HandlerManager;
import com.tv.mydiy.util.UiEventUtils;
import com.tv.mydiy.util.LogUtil;
import com.tv.mydiy.util.Constants;
import com.tv.mydiy.ui.viewmodel.MainViewModel;
import com.tv.mydiy.ui.viewmodel.ChannelViewModel;
import com.tv.mydiy.ui.viewmodel.PlayerViewModel;
import com.tv.mydiy.ui.controller.SplashScreenController;
import com.tv.mydiy.ui.controller.ChannelInfoBarController;
import com.tv.mydiy.ui.controller.DrawerMenuController;
import com.tv.mydiy.ui.controller.TouchEventHandler;
import com.tv.mydiy.ui.controller.KeyEventController;
import com.tv.mydiy.ui.controller.FavoriteController;
import com.tv.mydiy.ui.controller.ChannelNavigationController;
import com.tv.mydiy.ui.controller.MenuNavigationController;
import com.tv.mydiy.ui.controller.VoiceController;
import com.tv.mydiy.ui.controller.ViewInitializerController;
import com.tv.mydiy.data.repository.ChannelRepository;

import java.util.ArrayList;
import java.util.List;

// 夏杰语音相关导入
import com.peasun.aispeech.aiopen.AIOpenReceiver;

public class MainActivity extends AppCompatActivity implements 
        ChannelAdapter.OnChannelClickListener,
        GroupAdapter.OnGroupClickListener {
    
    private static final String TAG = "MainActivity";

    private Channel currentChannel = null;
    private int currentGroupIndex = 0;
    private int currentChannelIndex = 0;

    // 长按相关变量已迁移到KeyEventController
    


    // Views
    private DrawerLayout drawerLayout;
    private RecyclerView channelList;
    private RecyclerView groupList;
    private TextView channelInfo;
    private RelativeLayout splashScreen;
    private ProgressBar loadingProgress;
    private LinearLayout channelInfoBar;
    private TextView currentProgramTitle;
    private TextView nextProgramTitle;
    private TextView sourceInfoText;
    private ImageView channelInfoLogo;
    private LinearLayout leftSubDrawer;
    private LinearLayout leftEpgDrawer;
    private RecyclerView epgProgramList;
    private TextView epgDrawerTitle;
    private LinearLayout rightSubDrawer;
    private FrameLayout playerContainer;
    private TextView timeDisplay;
    private TextView networkSpeed;
    private RecyclerView settingsList;
    private RecyclerView settingsDetailList;

    // UI更新管理器
    private UiUpdater uiUpdater;

    // Adapters
    private ChannelAdapter channelAdapter;
    private GroupAdapter groupAdapter;

    // Modules
    private SettingModule settingModule;
    private com.tv.mydiy.channel.ChannelManager channelManager;
    
    private List<ChannelGroup> channelGroups;
    private static final int MAX_RELOAD_ATTEMPTS = Constants.MAIN_ACTIVITY_MAX_RELOAD_ATTEMPTS;
    private int reloadAttempts = 0;
    private String defaultChannelUrl;
    private boolean isChannelListLoaded = false;
    private boolean isPlayerManagerInitialized = false;
    private boolean isSplashScreenHidden = false;
    private boolean isFirstPlay = true;
    
    // Managers
    private SettingsManager settingsManager;
    private IjkPlayerAdapter ijkPlayerAdapter;
    private SimplePlayerController playerController;
    private EpgController epgController;
    private FavoriteManager favoriteManager;
    private MainMenuManager mainMenuManager;
    private AuthorizationManager authorizationManager;
    private boolean hasLoggedInitializationComplete = false;
    private HandlerManager handlerManager;
    private boolean isBackPressedOnce = false;
    private final Runnable exitRunnable = () -> isBackPressedOnce = false;
    private static final int EXIT_DELAY = Constants.MAIN_ACTIVITY_EXIT_DELAY;
    private AIOpenReceiver aiOpenReceiver;
    private Runnable longPressRunnable;

    // ========== 新架构组件 - ViewModel层 ==========
    private MainViewModel mainViewModel;
    private ChannelViewModel channelViewModel;
    private PlayerViewModel playerViewModel;

    // ========== 新架构组件 - Repository层 ==========
    private ChannelRepository channelRepository;

    // ========== 新架构组件 - Controller层 ==========
    private SplashScreenController splashScreenController;
    private ChannelInfoBarController channelInfoBarController;
    private DrawerMenuController drawerMenuController;
    private TouchEventHandler touchEventHandler;
    private KeyEventController keyEventController;
    private FavoriteController favoriteController;
    private ChannelNavigationController channelNavigationController;
    private MenuNavigationController menuNavigationController;
    private VoiceController voiceController;
    private ViewInitializerController viewInitializerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 启用严格模式，帮助发现潜在问题
        // 移除对BuildConfig.DEBUG的依赖，改用常量判断是否启用调试模式
        // 在发布版本中设置为false，在调试版本中设置为true
        // 注意：在主线程中进行磁盘IO操作会违反StrictMode策略，所以我们暂时禁用它

        setContentView(R.layout.activity_main);
        
        // 初始化Handler管理器
        handlerManager = new HandlerManager(this);
        
        // 初始化设置管理器
        settingsManager = new SettingsManager(this);
        
        // 初始化收藏管理器
        favoriteManager = new FavoriteManager(this);
        
        // 初始化网络管理器
        NetworkManager networkManager = new NetworkManager();
        
        // 初始化频道管理器
        channelManager = new com.tv.mydiy.channel.ChannelManager(networkManager);
        
        // ========== 初始化新架构组件 ==========
        initArchitectureComponents();
        
        // 提前初始化关键视图组件（特别是启动画面）
        initViewsEarly();
        
        // 初始化播放控制器
        ijkPlayerAdapter = new IjkPlayerAdapter();
        playerController = new SimplePlayerController(this, ijkPlayerAdapter);
        isPlayerManagerInitialized = true; // 设置播放器管理器初始化完成标志
        

        
        // 初始化UI更新管理器，暂时使用null值，稍后在initViews中重新初始化
        uiUpdater = new UiUpdater(timeDisplay, networkSpeed, null, null, settingsManager, handlerManager);
        
        // 初始化设置模块
        settingModule = new SettingModule(this, settingsManager);
        
        // 初始化授权管理器
        authorizationManager = new AuthorizationManager(this);
        
        // 初始化视图组件
        initViews();
        
        // 应用设置
        applyAllSettings();
        
        // 初始化完成后加载频道列表
        loadChannelList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次 Activity 恢复时，设置焦点到播放器容器
        if (playerContainer != null) {
            playerContainer.post(() -> playerContainer.requestFocus());
        }
    }

    /**
     * 初始化新架构组件 (ViewModel和Controller)
     */
    private void initArchitectureComponents() {
        // 初始化Repository
        channelRepository = new ChannelRepository(channelManager);

        // 初始化ViewModels
        mainViewModel = new MainViewModel();
        channelViewModel = new ChannelViewModel();
        playerViewModel = new PlayerViewModel();

        // 初始化Controllers
        splashScreenController = new SplashScreenController();
        channelInfoBarController = new ChannelInfoBarController(this);
        drawerMenuController = new DrawerMenuController();
        touchEventHandler = new TouchEventHandler();
        keyEventController = new KeyEventController();
        favoriteController = new FavoriteController();
        channelNavigationController = new ChannelNavigationController();
        menuNavigationController = new MenuNavigationController();
        voiceController = new VoiceController();
        viewInitializerController = new ViewInitializerController(this);
    }



    /**
     * 提前初始化关键视图组件（特别是启动画面）
     */
    private void initViewsEarly() {
        // 初始化启动画面（必须在setContent之后立即执行）
        splashScreen = findViewById(R.id.splash_screen);
        loadingProgress = findViewById(R.id.loading_progress);

        // ========== 设置新架构组件的视图引用 ==========
        if (splashScreenController != null) {
            splashScreenController.setSplashScreen(splashScreen);
            splashScreenController.setLoadingProgress(loadingProgress);
        }
    }
    
    // 初始化视图组件
    private void initViews() {
        
        // 初始化默认频道列表URL
        defaultChannelUrl = getString(R.string.default_channel_url);
        
        // 初始化菜单管理器
        mainMenuManager = new MainMenuManager(this);
        
        // 初始化各模块视图
        initPlayerViews();
        initDisplayViews();
        initMenuViews();
        
        // 初始化菜单管理器视图
        if (mainMenuManager != null) {
            mainMenuManager.initMenuViews();
        }
        
        // 应用保存的解码器设置
        applyAllSettings(); // 使用统一的方法应用所有设置
        
        // 确保首次播放标志重置

        // 检查是否所有初始化已完成
        checkAllInitializationComplete();
        
        // 初始化夏杰语音支持
        if (voiceController != null) {
            voiceController.initializeXiajieVoice();
        }
        
    }
    
    /**
     * 初始化播放器相关视图组件
     */
    private void initPlayerViews() {
        playerContainer = findViewById(R.id.player_container);
        // 确保播放容器可以获取焦点
        if (playerContainer != null) {
            playerContainer.setFocusable(true);
            playerContainer.setFocusableInTouchMode(true);
            playerContainer.setBackgroundResource(R.drawable.player_container_background);
        }
        
        // 设置播放器视图
        setupPlayerView();
        

    }
    
    /**
     * 初始化显示相关视图组件
     */
    private void initDisplayViews() {
        channelInfo = findViewById(R.id.channel_info);
        splashScreen = findViewById(R.id.splash_screen);
        loadingProgress = findViewById(R.id.loading_progress);
        channelInfoBar = findViewById(R.id.channel_info_bar);
        currentProgramTitle = findViewById(R.id.current_program_title);
        nextProgramTitle = findViewById(R.id.next_program_title);
        sourceInfoText = findViewById(R.id.source_info_text);
        channelInfoLogo = findViewById(R.id.channel_info_logo);
        // 时间显示组件
        timeDisplay = findViewById(R.id.time_display);
        // 网速显示组件
        networkSpeed = findViewById(R.id.network_speed);
    }
    
    /**
     * 初始化菜单相关视图组件
     */
    private void initMenuViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        groupList = findViewById(R.id.group_list);
        channelList = findViewById(R.id.channel_list);
        leftSubDrawer = findViewById(R.id.left_sub_drawer);
        leftEpgDrawer = findViewById(R.id.left_epg_drawer);
        epgProgramList = findViewById(R.id.epg_program_list);
        epgDrawerTitle = findViewById(R.id.epg_drawer_title);
        
        // 设置DrawerLayout的scrim颜色为透明，避免菜单打开时整个屏幕变暗
        if (drawerLayout != null) {
            drawerLayout.setScrimColor(android.graphics.Color.TRANSPARENT);
            // 禁用scrim动画，防止出现瞬时透明蒙层效果
        }
        
        // 确保左侧抽屉容器填充整个屏幕高度
        View leftDrawerContainer = findViewById(R.id.left_drawer_container);
        if (leftDrawerContainer != null) {
            // 强制设置高度为MATCH_PARENT
            DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) leftDrawerContainer.getLayoutParams();
            params.height = DrawerLayout.LayoutParams.MATCH_PARENT;
            leftDrawerContainer.setLayoutParams(params);
        }
        
        // 初始化设置管理器
        if (settingsManager == null) {
            settingsManager = new SettingsManager(this);
        }
        
        channelGroups = new ArrayList<>();
        List<ChannelGroup> channelGroupsForMenu = new ArrayList<>();
        

        
        // 初始化设置菜单
        initSettingsMenu();
        
        // 设置SettingModule的视图引用
        if (settingModule != null && settingsList != null && settingsDetailList != null && rightSubDrawer != null) {
            settingModule.setViews(settingsList, settingsDetailList, rightSubDrawer);
        }
        
        // 初始化频道适配器
        if (channelGroups != null) {
            channelAdapter = new ChannelAdapter(channelGroups, new ChannelAdapter.OnChannelClickListener() {
                @Override
                public void onChannelClick(Channel channel, int groupIndex, int channelIndex) {
                    currentChannelIndex = channelIndex;
                    currentGroupIndex = groupIndex;
                    playChannel(channel);
                    // 关闭抽屉
                    if (drawerLayout != null) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }

                @Override
                public void onEpgInfoClick(Channel channel) {
                    // 显示该频道的完整EPG节目清单
                    showEpgProgramList(channel);
                }
            }, settingsManager, MainActivity.this.getApplicationContext()); // 传递SettingsManager实例和Context
            
            // 设置数据变化监听器
            // 数据变化时更新频道组列表
            channelAdapter.setOnDataChangedListener(this::updateGroupList);
            
            // 初始化EPG控制器 - 确保使用包含所有频道组的channelAdapter
            EpgManager epgManager = new EpgManager(this);
            epgController = new EpgController(this, epgManager, settingsManager, channelAdapter, this);
            
            // 重新初始化UI更新管理器，使用正确的channelAdapter和epgController
            uiUpdater = new UiUpdater(timeDisplay, networkSpeed, channelAdapter, epgController, settingsManager, handlerManager);
        }
        
        // 默认展开频道组
        if (channelGroups != null && !channelGroups.isEmpty()) {
            channelGroups.get(0).setExpanded(true);
        }

        // 初始化分组适配器
        groupAdapter = new GroupAdapter(channelGroupsForMenu, (group, groupIndex) -> {
            // 隐藏其他可能打开的抽屉
            if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                mainMenuManager.getChannelMenuManager().hideEpgDrawer();
            } else {
                hideLeftEpgDrawer(); // Fallback to original method
            }

            // 更新当前选中的频道组索引
            currentGroupIndex = groupIndex;

            // 更新频道列表为所选分组的内容并处理焦点转移
            // 确保在主线程中更新数据
            runOnUiThread(() -> {
                // 添加同步检查，确保数据一致性
                synchronized (MainActivity.this) {
                    if (channelGroups != null && groupIndex >= 0 && groupIndex < channelGroups.size()) {
                        // 更新当前选中的频道组索引，以高亮显示选中的组
                        if (groupAdapter != null) {
                            groupAdapter.setCurrentGroupIndex(groupIndex);
                        }

                        // 同时更新EPG控制器中的ChannelAdapter引用，确保EPG匹配使用完整数据
                        if (epgController != null && channelAdapter != null) {
                            epgController.updateChannelAdapter(channelAdapter);
                        }

                        // 使用 menuNavigationController 来显示频道列表并处理焦点转移
                        if (menuNavigationController != null) {
                            menuNavigationController.showChannelList();
                        }
                    }
                }
            });
        });
        
        // 设置菜单管理器的适配器
        if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
            mainMenuManager.getChannelMenuManager().setChannelGroups(channelGroupsForMenu);
            mainMenuManager.getChannelMenuManager().setChannelAdapter(channelAdapter);
        }

        if (channelList != null && channelAdapter != null) {
            channelList.setLayoutManager(new LinearLayoutManager(this));
            // 为RecyclerView设置更有效的缓存策略
            channelList.setItemViewCacheSize(20); // 增加ViewHolder缓存大小
            channelList.setDrawingCacheEnabled(true);
            channelList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
            channelList.setAdapter(channelAdapter);
            
            // 启用键盘导航
            channelList.setFocusable(true);
            channelList.setDescendantFocusability(RecyclerView.FOCUS_AFTER_DESCENDANTS);
        }
        
        if (groupList != null && groupAdapter != null) {
            LinearLayoutManager groupLayoutManager = new LinearLayoutManager(this);
            groupList.setLayoutManager(groupLayoutManager);
            groupList.setAdapter(groupAdapter);
            
            // 启用键盘导航
            groupList.setFocusable(true);
            groupList.setDescendantFocusability(RecyclerView.FOCUS_AFTER_DESCENDANTS);
        }
        
        // 确保右侧设置列表可以接收键盘导航
        if (settingsList != null) {
            settingsList.setFocusable(true);
            settingsList.setDescendantFocusability(RecyclerView.FOCUS_AFTER_DESCENDANTS);
        }
        
        if (settingsDetailList != null) {
            settingsDetailList.setFocusable(true);
            settingsDetailList.setDescendantFocusability(RecyclerView.FOCUS_AFTER_DESCENDANTS);
        }
        
        // 确保右侧二级抽屉可以接收焦点和点击事件
        if (rightSubDrawer != null) { // 添加空值检查
            rightSubDrawer.setFocusable(true);
            rightSubDrawer.setClickable(true);
        }
        
        // 确保左侧抽屉可以接收焦点和点击事件
        LinearLayout leftDrawer = findViewById(R.id.left_drawer);
        if (leftDrawer != null) {
            leftDrawer.setFocusable(true);
            leftDrawer.setClickable(true);
        }

        // ========== 设置其他新架构组件的视图引用 ==========
        setupArchitectureViewReferences();
        
        // ========== 设置新Controller的依赖关系 ==========
        setupNewControllers();
        
        // ========== 设置ViewModel的LiveData观察者 ==========
        setupViewModelObservers();
    }

    /**
     * 设置新架构组件的视图引用
     */
    private void setupArchitectureViewReferences() {
        // 设置 ChannelInfoBarController
        if (channelInfoBarController != null) {
            channelInfoBarController.setChannelInfoBar(channelInfoBar);
            channelInfoBarController.setChannelInfo(channelInfo);
            channelInfoBarController.setCurrentProgramTitle(currentProgramTitle);
            channelInfoBarController.setNextProgramTitle(nextProgramTitle);
            channelInfoBarController.setSourceInfoText(sourceInfoText);
            channelInfoBarController.setChannelInfoLogo(channelInfoLogo);
        }

        // 设置 DrawerMenuController
        if (drawerMenuController != null) {
            drawerMenuController.setDrawerLayout(drawerLayout);
            View leftDrawerView = findViewById(R.id.left_drawer);
            View rightDrawerView = findViewById(R.id.right_drawer);
            drawerMenuController.setLeftDrawer(leftDrawerView);
            drawerMenuController.setRightDrawer(rightDrawerView);
            
            // 设置抽屉监听器
            drawerMenuController.setListener(new DrawerMenuController.DrawerMenuListener() {
                @Override
                public void onLeftDrawerOpened() {
                    if (groupList != null) {
                        groupList.requestFocus();
                        groupList.post(() -> {
                            if (groupList.getAdapter() != null && groupList.getAdapter().getItemCount() > 0) {
                                groupList.scrollToPosition(0);
                                UiEventUtils.setFocusToFirstWithLayoutChange(groupList);
                            }
                        });
                    }
                }

                @Override
                public void onLeftDrawerClosed() {
                }

                @Override
                public void onRightDrawerOpened() {
                    if (settingsList != null) {
                        settingsList.post(() -> {
                            settingsList.requestFocus();
                            if (settingsList.getAdapter() != null && 
                                settingsList.getAdapter().getItemCount() > 0) {
                                settingsList.scrollToPosition(0);
                                UiEventUtils.setFocusToFirstWithLayoutChange(settingsList);
                            }
                        });
                        settingsList.postDelayed(() -> {
                            settingsList.requestFocus();
                        }, 300);
                        settingsList.postDelayed(() -> {
                            settingsList.requestFocus();
                        }, 600);
                    }
                }

                @Override
                public void onRightDrawerClosed() {
                }
            });
        }

        // ========== 完全迁移：设置 TouchEventHandler 并实现完整逻辑 ==========
        if (touchEventHandler != null) {
            // 为TouchEventHandler设置完整的事件监听器
            touchEventHandler.setListener(new TouchEventHandler.TouchEventListener() {
                @Override
                public void onSingleClick(float x, float screenWidth) {
                    // ========== 新架构的单击处理（完全迁移） ==========
                    if (drawerLayout != null) {
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                            // 关闭菜单时重新显示频道信息条
                            if (currentChannel != null) {
                                showChannelInfoBar(currentChannel);
                            }
                        } else if (settingModule != null && settingModule.isSubSettingsVisible()) {
                            settingModule.hideSubSettings();
                        } else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                            drawerLayout.closeDrawer(GravityCompat.END);
                        } else if (leftSubDrawer != null && leftSubDrawer.getVisibility() == View.VISIBLE) {
                            leftSubDrawer.setVisibility(View.GONE);
                        } else if (leftEpgDrawer != null && leftEpgDrawer.getVisibility() == View.VISIBLE) {
                            leftEpgDrawer.setVisibility(View.GONE);
                        } else if (!drawerLayout.isDrawerOpen(GravityCompat.END)) {
                            // 打开菜单前隐藏频道信息条（避免被误认为是蒙层）
                            hideChannelInfoBar();
                            drawerLayout.openDrawer(GravityCompat.START);
                            if (groupList != null) {
                                groupList.requestFocus();
                                groupList.post(() -> {
                                    if (groupList.getAdapter() != null && groupList.getAdapter().getItemCount() > 0) {
                                        groupList.scrollToPosition(0);
                                        UiEventUtils.setFocusToFirstWithLayoutChange(groupList);
                                    }
                                });
                            }
                        }
                        
                        if (playerContainer != null) {
                            playerContainer.requestFocus();
                        }
                    }
                }

                @Override
                public void onDoubleClick(float x, float screenWidth) {
                    // ========== 新架构的双击处理（完全迁移） ==========
                    if (x >= screenWidth / 2) {
                        if (drawerLayout != null) {
                            drawerLayout.openDrawer(GravityCompat.END);
                            if (settingsList != null) {
                                settingsList.post(() -> {
                                    settingsList.requestFocus();
                                    if (settingsList.getAdapter() != null && 
                                        settingsList.getAdapter().getItemCount() > 0) {
                                        settingsList.scrollToPosition(0);
                                        UiEventUtils.setFocusToFirstWithLayoutChange(settingsList);
                                    }
                                });
                                settingsList.postDelayed(() -> {
                                    settingsList.requestFocus();
                                }, 300);
                            }
                        }
                    } else {
                        if (drawerLayout != null) {
                            drawerLayout.openDrawer(GravityCompat.START);
                            if (groupList != null) {
                                groupList.requestFocus();
                                groupList.post(() -> {
                                    if (groupList.getAdapter() != null && groupList.getAdapter().getItemCount() > 0) {
                                        groupList.scrollToPosition(0);
                                        UiEventUtils.setFocusToFirstWithLayoutChange(groupList);
                                    }
                                });
                            }
                        }
                    }
                }

                @Override
                public void onLongPress(float x, float screenWidth) {
                    // ========== 新架构的长按处理（预留，未来可实现） ==========
                    // 长按逻辑可以在这里实现
                }
            });
            
            // ========== 启用 TouchEventHandler 到 playerContainer ==========
            if (playerContainer != null) {
                playerContainer.setOnTouchListener(touchEventHandler);
            }
        }
    }

    /**
     * 设置新Controller的依赖关系
     */
    private void setupNewControllers() {
        // 设置 KeyEventController
        if (keyEventController != null) {
            keyEventController.setActivity(this);
            keyEventController.setPlayerController(playerController);
            keyEventController.setPlayerViewModel(playerViewModel);
            keyEventController.setListener(new KeyEventController.KeyEventListener() {
                @Override
                public boolean isAnyMenuVisible() {
                    return isAnyMenuOpen();
                }

                @Override
                public void changeChannel(int direction) {
                    MainActivity.this.changeChannel(direction);
                }

                @Override
                public void changeSource(int direction) {
                    if (keyEventController != null) {
                        keyEventController.changeSource(direction);
                    }
                }

                @Override
                public boolean handleMenuKey() {
                    return MainActivity.this.handleMenuKey();
                }

                @Override
                public boolean handleBack() {
                    return MainActivity.this.handleBack();
                }

                @Override
                public void hideChannelInfoBar() {
                    MainActivity.this.hideChannelInfoBar();
                }

                @Override
                public void handleLongPressFavorite() {
                    if (favoriteController != null) {
                        favoriteController.handleLongPressFavorite(channelList, channelAdapter);
                    }
                }

                @Override
                public void handleEnterOrCenter() {
                    if (keyEventController != null) {
                        keyEventController.handleEnterOrCenter();
                    }
                }

                @Override
                public boolean isSettingModuleSubSettingsVisible() {
                    return settingModule != null && settingModule.isSubSettingsVisible();
                }

                @Override
                public boolean isDrawerOpen(int gravity) {
                    return drawerLayout != null && drawerLayout.isDrawerOpen(gravity);
                }

                @Override
                public void closeDrawer(int gravity) {
                    if (drawerLayout != null) {
                        drawerLayout.closeDrawer(gravity);
                    }
                }

                @Override
                public void openDrawer(int gravity) {
                    if (drawerLayout != null) {
                        drawerLayout.openDrawer(gravity);
                    }
                }

                @Override
                public void requestSettingsListFocus() {
                    if (settingsList != null) {
                        settingsList.requestFocus();
                    }
                }

                @Override
                public void requestSettingsDetailListFocus() {
                    if (settingsDetailList != null) {
                        settingsDetailList.requestFocus();
                    }
                }

                @Override
                public void requestPlayerContainerFocus() {
                    if (playerContainer != null) {
                        playerContainer.requestFocus();
                    }
                }

                @Override
                public void scrollSettingsListToFirst() {
                    if (settingsList != null) {
                        settingsList.post(() -> {
                            if (settingsList.getAdapter() != null && 
                                settingsList.getAdapter().getItemCount() > 0) {
                                settingsList.scrollToPosition(0);
                                UiEventUtils.setFocusToFirstWithLayoutChange(settingsList);
                            }
                        });
                    }
                }

                @Override
                public boolean isMainMenuManagerChannelMenuManagerNull() {
                    return mainMenuManager == null || mainMenuManager.getChannelMenuManager() == null;
                }

                @Override
                public void hideChannelMenu() {
                    if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                        mainMenuManager.getChannelMenuManager().hideMenu();
                    }
                }

                @Override
                public void hideSubSettings() {
                    if (settingModule != null) {
                        settingModule.hideSubSettings();
                    }
                }

                @Override
                public void showSettingsMenu() {
                    hideChannelInfoBar();
                    openDrawer(GravityCompat.END);
                    scrollSettingsListToFirst();
                }

                @Override
                public MainMenuManager getMainMenuManager() {
                    return mainMenuManager;
                }

                @Override
                public RecyclerView getGroupList() {
                    return groupList;
                }

                @Override
                public List<ChannelGroup> getChannelGroups() {
                    return channelGroups;
                }

                @Override
                public int getCurrentGroupIndex() {
                    return currentGroupIndex;
                }

                @Override
                public void setCurrentGroupIndex(int index) {
                    currentGroupIndex = index;
                }

                @Override
                public GroupAdapter getGroupAdapter() {
                    return groupAdapter;
                }

                @Override
                public void showChannelList() {
                    if (menuNavigationController != null) {
                        menuNavigationController.showChannelList();
                    }
                }

                @Override
                public void handleChannelListFocusTransfer(RecyclerView channelList) {
                    if (menuNavigationController != null) {
                        menuNavigationController.handleChannelListFocusTransfer(channelList);
                    }
                }

                @Override
                public RecyclerView getChannelList() {
                    return channelList;
                }

                @Override
                public ChannelAdapter getChannelAdapter() {
                    return channelAdapter;
                }

                @Override
                public void playChannel(Channel channel) {
                    MainActivity.this.playChannel(channel);
                }

                @Override
                public void hideChannelListFromMainMenuManager() {
                    if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                        mainMenuManager.getChannelMenuManager().hideChannelList();
                    }
                }

                @Override
                public void setLeftSubDrawerVisibility(int visibility) {
                    if (leftSubDrawer != null) {
                        leftSubDrawer.setVisibility(visibility);
                    }
                }

                @Override
                public void showChannelInfoBar(Channel channel) {
                    MainActivity.this.showChannelInfoBar(channel);
                }

                @Override
                public Channel getCurrentChannel() {
                    return currentChannel;
                }

                @Override
                public RecyclerView getEpgProgramList() {
                    return epgProgramList;
                }

                @Override
                public void hideEpgDrawerFromMainMenuManager() {
                    if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                        mainMenuManager.getChannelMenuManager().hideEpgDrawer();
                    }
                }

                @Override
                public void setLeftEpgDrawerVisibility(int visibility) {
                    if (leftEpgDrawer != null) {
                        leftEpgDrawer.setVisibility(visibility);
                    }
                }

                @Override
                public boolean isLeftSubDrawerVisible() {
                    return leftSubDrawer != null && leftSubDrawer.getVisibility() == View.VISIBLE;
                }

                @Override
                public boolean isLeftEpgDrawerVisible() {
                    return leftEpgDrawer != null && leftEpgDrawer.getVisibility() == View.VISIBLE;
                }

                @Override
                public void updateChannelInfo(Channel channel) {
                    MainActivity.this.updateChannelInfo(channel);
                }

                @Override
                public void saveLastPlayedChannel(int groupIndex, int channelIndex) {
                    MainActivity.this.saveLastPlayedChannel(groupIndex, channelIndex);
                }

                @Override
                public int findGroupIndexForChannel(Channel channel) {
                    return MainActivity.this.findGroupIndexForChannel(channel);
                }

                @Override
                public int findChannelIndexInGroup(int groupIndex, Channel channel) {
                    return MainActivity.this.findChannelIndexInGroup(groupIndex, channel);
                }

                @Override
                public void recordFirstChannelChange() {
                    MainActivity.this.recordFirstChannelChange();
                }

                @Override
                public void checkAuthorizationIfNeeded() {
                    MainActivity.this.checkAuthorizationIfNeeded();
                }

                @Override
                public void runOnUiThread(Runnable runnable) {
                    MainActivity.this.runOnUiThread(runnable);
                }

                @Override
                public void showEpgProgramList(Channel channel) {
                    MainActivity.this.showEpgProgramList(channel);
                }
            });
        }

        // 设置 FavoriteController
        if (favoriteController != null) {
            favoriteController.setFavoriteManager(favoriteManager);
            favoriteController.setListener(new FavoriteController.FavoriteUpdateListener() {
                @Override
                public void onFavoriteUpdated() {
                    if (groupAdapter != null && channelGroups != null) {
                        List<ChannelGroup> channelGroupsForMenu = new ArrayList<>();
                        for (ChannelGroup group : channelGroups) {
                            if (group != null) {
                                channelGroupsForMenu.add(group);
                            }
                        }
                        groupAdapter.updateData(channelGroupsForMenu);
                        if (groupList != null) {
                            groupList.requestFocus();
                        }
                        if (channelAdapter != null) {
                            channelAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void runOnUiThread(Runnable runnable) {
                    MainActivity.this.runOnUiThread(runnable);
                }
            });
        }

        // 设置 MenuNavigationController
        if (menuNavigationController != null) {
            menuNavigationController.setActivity(this);
            menuNavigationController.setListener(new MenuNavigationController.MenuNavigationListener() {
                @Override
                public void playChannel(Channel channel) {
                    MainActivity.this.playChannel(channel);
                }

                @Override
                public void showChannelInfoBar(Channel channel) {
                    MainActivity.this.showChannelInfoBar(channel);
                }

                @Override
                public void requestChannelListFocus() {
                    if (channelList != null) {
                        channelList.requestFocus();
                    }
                }

                @Override
                public void requestGroupListFocus() {
                    if (groupList != null) {
                        groupList.requestFocus();
                    }
                }

                @Override
                public void requestSettingsListFocus() {
                    if (settingsList != null) {
                        settingsList.requestFocus();
                    }
                }

                @Override
                public void requestSettingsDetailListFocus() {
                    if (settingsDetailList != null) {
                        settingsDetailList.requestFocus();
                    }
                }

                @Override
                public void requestEpgProgramListFocus() {
                    if (epgProgramList != null) {
                        epgProgramList.requestFocus();
                    }
                }

                @Override
                public void requestPlayerContainerFocus() {
                    if (playerContainer != null) {
                        playerContainer.requestFocus();
                    }
                }

                @Override
                public void hideLeftEpgDrawer() {
                    MainActivity.this.hideLeftEpgDrawer();
                }

                @Override
                public void hideLeftSubDrawer() {
                    MainActivity.this.hideLeftSubDrawer();
                }

                @Override
                public void closeDrawer(int gravity) {
                    if (drawerLayout != null) {
                        drawerLayout.closeDrawer(gravity);
                    }
                }

                @Override
                public void openDrawer(int gravity) {
                    if (drawerLayout != null) {
                        drawerLayout.openDrawer(gravity);
                    }
                }

                @Override
                public boolean isMainMenuManagerNull() {
                    return mainMenuManager == null;
                }

                @Override
                public boolean isMainMenuManagerChannelMenuManagerNull() {
                    return mainMenuManager == null || mainMenuManager.getChannelMenuManager() == null;
                }

                @Override
                public void hideChannelMenuFromMainMenuManager() {
                    if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                        mainMenuManager.getChannelMenuManager().hideMenu();
                    }
                }

                @Override
                public void hideChannelListFromMainMenuManager() {
                    if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                        mainMenuManager.getChannelMenuManager().hideChannelList();
                    }
                }

                @Override
                public void hideEpgDrawerFromMainMenuManager() {
                    if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                        mainMenuManager.getChannelMenuManager().hideEpgDrawer();
                    }
                }

                @Override
                public boolean isSettingModuleSubSettingsVisible() {
                    return settingModule != null && settingModule.isSubSettingsVisible();
                }

                @Override
                public void hideSubSettingsFromSettingModule() {
                    if (settingModule != null) {
                        settingModule.hideSubSettings();
                    }
                }

                @Override
                public void showSubSettingsFromSettingModule(SettingItem item) {
                    if (settingModule != null) {
                        settingModule.showSubSettings(item);
                    }
                }

                @Override
                public boolean isDrawerOpen(int gravity) {
                    return drawerLayout != null && drawerLayout.isDrawerOpen(gravity);
                }

                @Override
                public boolean isLeftEpgDrawerVisible() {
                    return leftEpgDrawer != null && leftEpgDrawer.getVisibility() == View.VISIBLE;
                }

                @Override
                public boolean isLeftSubDrawerVisible() {
                    return leftSubDrawer != null && leftSubDrawer.getVisibility() == View.VISIBLE;
                }

                @Override
                public List<ChannelGroup> getChannelGroups() {
                    return channelGroups;
                }

                @Override
                public int getCurrentGroupIndex() {
                    return currentGroupIndex;
                }

                @Override
                public ChannelAdapter getChannelAdapter() {
                    return channelAdapter;
                }

                @Override
                public MainMenuManager getMainMenuManager() {
                    return mainMenuManager;
                }

                @Override
                public EpgController getEpgController() {
                    return epgController;
                }

                @Override
                public RecyclerView getChannelList() {
                    return channelList;
                }

                @Override
                public void setLeftSubDrawerVisibility(int visibility) {
                    if (leftSubDrawer != null) {
                        leftSubDrawer.setVisibility(visibility);
                    }
                }

                @Override
                public void showEpgProgramList(Channel channel) {
                    MainActivity.this.showEpgProgramList(channel);
                }
            });
        }

        // 设置 ChannelNavigationController
        if (channelNavigationController != null) {
            channelNavigationController.setListener(direction -> MainActivity.this.changeChannel(direction));
        }

        // 设置 VoiceController
        if (voiceController != null) {
            voiceController.setListener(() -> MainActivity.this);
        }

        // 设置 ChannelInfoBarController
        if (channelInfoBarController != null) {
            channelInfoBarController.setEpgController(epgController);
            channelInfoBarController.setFirstPlay(isFirstPlay);
            channelInfoBarController.setListener(new ChannelInfoBarController.ChannelInfoBarListener() {
                @Override
                public void onInfoBarHidden() {
                }

                @Override
                public void onLoadLogo(ImageView imageView, String logoUrl) {
                    runOnUiThread(() -> Glide.with(MainActivity.this)
                            .load(logoUrl)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .error(R.mipmap.ic_launcher_round)
                            .into(imageView));
                }
            });
        }
    }
    
    /**
     * 设置ViewModel的LiveData观察者
     */
    private void setupViewModelObservers() {
        if (playerViewModel != null) {
            playerViewModel.getCurrentChannelLiveData().observe(this, channel -> {
                if (channel != null) {
                    updateChannelInfo(channel);
                }
            });
            
            playerViewModel.getIsPlayingLiveData().observe(this, isPlaying -> {
            });
            
            playerViewModel.getIsBufferingLiveData().observe(this, isBuffering -> {
            });
            
            playerViewModel.getPlayerErrorLiveData().observe(this, error -> {
                if (error != null && !error.isEmpty()) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        if (mainViewModel != null) {
            mainViewModel.getChannelGroupsLiveData().observe(this, groups -> {
                if (groups != null && groupAdapter != null) {
                    groupAdapter.updateData(groups);
                }
            });
            
            mainViewModel.getCurrentChannelLiveData().observe(this, channel -> {
                if (channel != null) {
                    currentChannel = channel;
                }
            });
            
            mainViewModel.getCurrentGroupIndexLiveData().observe(this, index -> currentGroupIndex = index);
        }

        if (channelRepository != null) {
            channelRepository.getChannelGroups().observe(this, groups -> {
                if (groups != null) {
                    channelGroups = groups;
                    if (mainViewModel != null) {
                        mainViewModel.setChannelGroups(groups);
                    }
                    updateGroupList();
                }
            });

            channelRepository.getIsLoading().observe(this, isLoading -> {
                if (isLoading) {
                    showLoadingProgress();
                } else {
                    hideLoadingProgress();
                }
            });

            channelRepository.getErrorMessage().observe(this, error -> {
                if (error != null && !error.isEmpty()) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    
    /**
     * 初始化设置菜单相关视图
     */
    private void initSettingsMenu() {
        // 确保在initViews中已经初始化了drawerLayout
        if (drawerLayout == null) {
            drawerLayout = findViewById(R.id.drawer_layout);
        }
        
        // 创建右侧抽屉容器
        if (drawerLayout != null) {
            // 检查是否已经存在右侧抽屉，避免重复创建
            View existingRightDrawer = findViewById(R.id.right_drawer);
            // 右侧主设置菜单
            FrameLayout rightDrawer;
            if (existingRightDrawer == null) {
                // 创建右侧主设置菜单容器，使用FrameLayout以便更好地控制子视图位置
                rightDrawer = new FrameLayout(this);
                rightDrawer.setId(R.id.right_drawer);
                int firstMenuWidth = getResources().getDimensionPixelSize(R.dimen.menu_first_level_width); // 一级菜单宽度
                int secondMenuWidth = getResources().getDimensionPixelSize(R.dimen.menu_second_level_width); // 二级菜单宽度
                DrawerLayout.LayoutParams rightDrawerParams = new DrawerLayout.LayoutParams(
                        firstMenuWidth + secondMenuWidth, // 宽度为一级+二级菜单宽度之和，为二级菜单预留空间
                        DrawerLayout.LayoutParams.MATCH_PARENT);
                rightDrawerParams.gravity = GravityCompat.END;
                rightDrawer.setLayoutParams(rightDrawerParams);
                rightDrawer.setBackgroundColor(ContextCompat.getColor(this, R.color.menu_background));
                rightDrawer.setFocusable(true);
                rightDrawer.setClickable(true);
                
                // 创建占位视图，为二级菜单预留空间
                View placeholderView = new View(this);
                FrameLayout.LayoutParams placeholderParams = new FrameLayout.LayoutParams(
                        secondMenuWidth, // 二级菜单宽度
                        FrameLayout.LayoutParams.MATCH_PARENT);
                placeholderParams.setMargins(0, 0, firstMenuWidth, 0); // 位于左侧
                placeholderView.setLayoutParams(placeholderParams);
                
                LinearLayout mainMenuContainer = createMainMenuContainer(firstMenuWidth, secondMenuWidth);
                
                // 添加标题栏
                TextView titleView = new TextView(this);
                titleView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        getResources().getDimensionPixelSize(R.dimen.menu_header_height)));
                titleView.setText(R.string.settings_title);
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 
                        getResources().getDimension(R.dimen.menu_header_text_size));
                titleView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                titleView.setGravity(Gravity.CENTER);
                titleView.setBackgroundColor(ContextCompat.getColor(this, R.color.menu_header_background));
                mainMenuContainer.addView(titleView);
                
                // 创建设置列表
                settingsList = new RecyclerView(this);
                settingsList.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0, 1.0f));
                settingsList.setLayoutManager(new LinearLayoutManager(this));
                settingsList.setFocusable(true);
                settingsList.setDescendantFocusability(RecyclerView.FOCUS_AFTER_DESCENDANTS);
                mainMenuContainer.addView(settingsList);
                
                // 将占位视图和主菜单容器添加到rightDrawer
                rightDrawer.addView(placeholderView);
                rightDrawer.addView(mainMenuContainer);
                
                // 将右侧抽屉添加到主布局
                drawerLayout.addView(rightDrawer);
            } else {
                // 检查现有视图是否为FrameLayout类型
                if (existingRightDrawer instanceof FrameLayout) {
                    rightDrawer = (FrameLayout) existingRightDrawer; // 安全类型转换
                    // 查找已存在的设置列表
                    for (int i = 0; i < rightDrawer.getChildCount(); i++) {
                        View child = rightDrawer.getChildAt(i);
                        if (child instanceof RecyclerView) {
                            settingsList = (RecyclerView) child;
                            break;
                        }
                        // 如果child是LinearLayout容器，检查其子视图
                        else if (child instanceof LinearLayout) {
                            LinearLayout container = (LinearLayout) child;
                            for (int j = 0; j < container.getChildCount(); j++) {
                                View innerChild = container.getChildAt(j);
                                if (innerChild instanceof RecyclerView) {
                                    settingsList = (RecyclerView) innerChild;
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    // 如果现有视图不是FrameLayout类型，创建新的FrameLayout
                    rightDrawer = new FrameLayout(this);
                    rightDrawer.setId(R.id.right_drawer);
                    int firstMenuWidth = getResources().getDimensionPixelSize(R.dimen.menu_first_level_width); // 一级菜单宽度
                    int secondMenuWidth = getResources().getDimensionPixelSize(R.dimen.menu_second_level_width); // 二级菜单宽度
                    DrawerLayout.LayoutParams rightDrawerParams = new DrawerLayout.LayoutParams(
                            firstMenuWidth + secondMenuWidth, // 宽度为一级+二级菜单宽度之和，为二级菜单预留空间
                            DrawerLayout.LayoutParams.MATCH_PARENT);
                    rightDrawerParams.gravity = GravityCompat.END;
                    rightDrawer.setLayoutParams(rightDrawerParams);
                    rightDrawer.setBackgroundColor(ContextCompat.getColor(this, R.color.menu_background));
                    rightDrawer.setFocusable(true);
                    rightDrawer.setClickable(true);
                    
                    // 创建占位视图，为二级菜单预留空间
                    View placeholderView = new View(this);
                    FrameLayout.LayoutParams placeholderParams = new FrameLayout.LayoutParams(
                            secondMenuWidth, // 二级菜单宽度
                            FrameLayout.LayoutParams.MATCH_PARENT);
                    placeholderParams.setMargins(0, 0, firstMenuWidth, 0); // 位于左侧
                    placeholderView.setLayoutParams(placeholderParams);
                    
                    LinearLayout mainMenuContainer = createMainMenuContainer(firstMenuWidth, secondMenuWidth);
                    
                    // 添加标题栏
                    TextView titleView = new TextView(this);
                    titleView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            getResources().getDimensionPixelSize(R.dimen.menu_header_height)));
                    titleView.setText(R.string.settings_title);
                    titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 
                            getResources().getDimension(R.dimen.menu_header_text_size));
                    titleView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                    titleView.setGravity(Gravity.CENTER);
                    titleView.setBackgroundColor(ContextCompat.getColor(this, R.color.menu_header_background));
                    mainMenuContainer.addView(titleView);
                    
                    // 创建设置列表
                    settingsList = new RecyclerView(this);
                    settingsList.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0, 1.0f));
                    settingsList.setLayoutManager(new LinearLayoutManager(this));
                    settingsList.setFocusable(true);
                    settingsList.setDescendantFocusability(RecyclerView.FOCUS_AFTER_DESCENDANTS);
                    mainMenuContainer.addView(settingsList);
                    
                    // 将占位视图和主菜单容器添加到rightDrawer
                    rightDrawer.addView(placeholderView);
                    rightDrawer.addView(mainMenuContainer);
                    
                    // 将右侧抽屉添加到主布局
                    drawerLayout.addView(rightDrawer);
                }
            }

            // 创建右侧子设置菜单作为主抽屉的子视图，使用LinearLayout实现覆盖效果
            rightSubDrawer = new LinearLayout(this);
            rightSubDrawer.setId(R.id.right_sub_drawer);
            rightSubDrawer.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            rightSubDrawer.setFocusable(true);
            rightSubDrawer.setClickable(true);
            rightSubDrawer.setVisibility(View.GONE);
            

            // 设置子菜单的大小，位置将在添加到DrawerLayout时设置
            int firstMenuWidth = getResources().getDimensionPixelSize(R.dimen.menu_first_level_width); // 一级菜单宽度
            int secondMenuWidth = getResources().getDimensionPixelSize(R.dimen.menu_second_level_width); // 二级菜单宽度
            
            // 设置初始布局参数
            rightSubDrawer.setLayoutParams(new ViewGroup.LayoutParams(secondMenuWidth, ViewGroup.LayoutParams.MATCH_PARENT));
            
            // 创建垂直布局容器来包含子菜单内容
            LinearLayout subMenuContainer = new LinearLayout(this);
            subMenuContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            subMenuContainer.setLayoutParams(containerParams);
            
            // 添加标题栏
            TextView subTitleView = new TextView(this);
            subTitleView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    getResources().getDimensionPixelSize(R.dimen.menu_header_height)));
            subTitleView.setText(R.string.settings_detail_title);
            subTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.menu_header_text_size));
            subTitleView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            subTitleView.setGravity(Gravity.CENTER);
            subTitleView.setBackgroundColor(ContextCompat.getColor(this, R.color.menu_header_background));
            subMenuContainer.addView(subTitleView);
            
            // 创建子设置列表
            settingsDetailList = new RecyclerView(this);
            settingsDetailList.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0, 1.0f));
            settingsDetailList.setLayoutManager(new LinearLayoutManager(this));
            settingsDetailList.setFocusable(true);
            settingsDetailList.setDescendantFocusability(RecyclerView.FOCUS_AFTER_DESCENDANTS);
            subMenuContainer.addView(settingsDetailList);

            rightSubDrawer.addView(subMenuContainer);
            
            FrameLayout.LayoutParams subDrawerFrameParams = new FrameLayout.LayoutParams(
                    secondMenuWidth,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            // 初始位于占位视图的位置（左侧）
            subDrawerFrameParams.setMargins(0, 0, firstMenuWidth, 0); // 初始位于左侧占位区域
            rightSubDrawer.setLayoutParams(subDrawerFrameParams);
            rightSubDrawer.setTranslationX(0); // 初始位置，使用margin定位

            rightDrawer.addView(rightSubDrawer);

        }
    }
    


    /**
     * 根据设置更新显示选项（时间、网速等）
     */
    public void updateDisplaySettings() {
        // ========== 使用新架构组件（状态保持一致） ==========
        if (settingsManager == null) {
            return;
        }
        
        // ========== 原有逻辑保持不变（UiUpdater已有专门管理器） ==========
        // 重新初始化UI更新管理器以确保使用最新的组件引用
        uiUpdater = new UiUpdater(timeDisplay, networkSpeed, channelAdapter, epgController, settingsManager, handlerManager);
        
        // 委托给UiUpdater处理
        uiUpdater.updateDisplaySettings();
    }

    private void changeChannel(int direction) {
        // ========== 完全迁移：使用ChannelViewModel ==========
        // ========== 注意：旧代码未完整注释，高复杂度模块 ==========
        // ========== 如果出现问题，可随时恢复原有的状态同步策略 ==========
        
        // ========== 新架构实现（使用ChannelViewModel） ==========
        
        // 检查频道列表是否已加载完成
        if (!isChannelListLoaded) {
            Toast.makeText(this, "频道列表加载中，请稍候...", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查是否启用了换台反转功能
        if (settingsManager != null && settingsManager.isReverseChannelSwitch()) {
            direction = -direction;
        }

        // 基于当前播放的频道重新确定索引
        boolean indicesUpdated = false;
        if (currentChannel != null) {
            int[] currentIndices = findChannelIndices(currentChannel);
            if (currentIndices[0] != -1 && currentIndices[1] != -1) {
                currentGroupIndex = currentIndices[0];
                currentChannelIndex = currentIndices[1];
                indicesUpdated = true;
            }
        }
        
        if (!indicesUpdated && channelAdapter != null) {
            int[] adapterIndices = channelAdapter.getCurrentPlayingIndices();
            if (adapterIndices != null && adapterIndices[0] != -1 && adapterIndices[1] != -1) {
                currentGroupIndex = adapterIndices[0];
                currentChannelIndex = adapterIndices[1];
            }
        }

        // 保存当前索引以防需要回滚
        int oldGroupIndex = currentGroupIndex;
        int oldChannelIndex = currentChannelIndex;

        // ========== 使用ChannelViewModel计算新索引 ==========
        boolean crossGroup = settingsManager == null || settingsManager.isCrossGroupSwitch();
        
        int[] newIndices;
        if (direction > 0) {
            newIndices = channelViewModel.getNextChannelIndices(channelGroups, currentGroupIndex, currentChannelIndex, crossGroup);
        } else {
            newIndices = channelViewModel.getPreviousChannelIndices(channelGroups, currentGroupIndex, currentChannelIndex, crossGroup);
        }
        
        currentGroupIndex = newIndices[0];
        currentChannelIndex = newIndices[1];
        
        // ========== 使用ChannelViewModel验证并获取频道 ==========
        if (channelViewModel.isValidGroupIndex(channelGroups, currentGroupIndex)) {
            ChannelGroup currentGroup = channelGroups.get(currentGroupIndex);
            if (channelViewModel.isValidChannelIndex(currentGroup, currentChannelIndex)) {
                Channel newChannel = channelViewModel.getChannel(channelGroups, currentGroupIndex, currentChannelIndex);
                if (newChannel != null) {
                    // 确保在换台时没有任何抽屉打开
                    if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
                        drawerLayout.closeDrawer(GravityCompat.END);
                    }
                    // 隐藏其他抽屉
                    if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                        mainMenuManager.getChannelMenuManager().hideChannelList();
                        mainMenuManager.getChannelMenuManager().hideEpgDrawer();
                    } else {
                        hideLeftSubDrawer();
                        hideLeftEpgDrawer();
                    }
                    
                    // 在换台前隐藏底部信息条、加载进度条和网速显示
                    hideChannelInfoBar();
                    hideLoadingProgress();
                    if (networkSpeed != null) {
                        networkSpeed.setVisibility(View.GONE);
                    }
                    

                    
                    playChannel(newChannel);

                    // 更新频道列表中的当前播放频道标识
                    if (channelAdapter != null) {
                        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
                            channelAdapter.setCurrentGroupIndex(currentGroupIndex, currentChannelIndex);
                        } else {
                            runOnUiThread(() -> channelAdapter.setCurrentGroupIndex(currentGroupIndex, currentChannelIndex));
                        }
                    }
                } else {
                    Toast.makeText(this, "无法切换到目标频道", Toast.LENGTH_SHORT).show();
                    currentGroupIndex = oldGroupIndex;
                    currentChannelIndex = oldChannelIndex;
                    if (uiUpdater != null) {
                        uiUpdater.updateDisplaySettings();
                    }
                }
            } else {
                Toast.makeText(this, "无法切换到目标频道", Toast.LENGTH_SHORT).show();
                currentGroupIndex = oldGroupIndex;
                currentChannelIndex = oldChannelIndex;
            }
        } else {
            Toast.makeText(this, "无法切换到目标频道组", Toast.LENGTH_SHORT).show();
            currentGroupIndex = oldGroupIndex;
            currentChannelIndex = oldChannelIndex;
        }
    }

    // 添加SurfaceView到playerContainer中
    private void setupPlayerView() {
        if (playerContainer != null && ijkPlayerAdapter != null) {
            // 创建SurfaceView用于视频播放
            SurfaceView surfaceView = new SurfaceView(this);
            surfaceView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ));
            
            // 添加SurfaceView到播放容器
            playerContainer.addView(surfaceView);
            
            // 设置SurfaceHolder回调
            SurfaceHolder holder = surfaceView.getHolder();
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    ijkPlayerAdapter.setDisplay(holder);
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                    // 在Surface销毁时，需要解除与播放器的绑定
                    ijkPlayerAdapter.setDisplay(null);
                }
            });
            
            // 注意：不要在这里设置OnInfoListener，因为它会覆盖SimplePlayerController中设置的监听器
            // 视频渲染开始事件的处理由SimplePlayerController负责
        }
    }
    

    
    // 添加检查并播放默认频道的方法
    public void checkAndPlayDefaultChannel() {
        // 重置日志记录标志，以便下次可以再次记录
        hasLoggedInitializationComplete = false;
        
              
        // 确保所有初始化任务已完成并且频道列表已加载完成
        // 注意：不再依赖EPG加载状态，因为EPG加载现在是可选的
        if (isChannelListLoaded && 
            channelGroups != null && !channelGroups.isEmpty() && playerController != null) {
            // 获取要播放的频道
            Channel channelToPlay = getChannelToPlay();
            
            if (channelToPlay != null) {
                playChannel(channelToPlay);
            }
            
            // 无论是否找到有效频道，都隐藏启动画面
            hideSplashScreen();
        } else if (isChannelListLoaded) {
            // 如果频道列表为空，也隐藏启动画面
            hideSplashScreen();
            
            // 如果频道组为空，尝试重新加载频道列表，但限制尝试次数避免无限循环
            if (reloadAttempts < MAX_RELOAD_ATTEMPTS) {
                reloadAttempts++;
                if (defaultChannelUrl != null && !defaultChannelUrl.isEmpty()) {
                    loadChannelList();
                }
            } else {
                // 达到最大尝试次数，停止尝试
                // 根据项目规范，即使无法获取当前频道也要尝试播放默认频道
                // 在这种情况下，我们至少要隐藏启动画面
                hideSplashScreen();
                
                // 显示错误信息给用户
                runOnUiThread(() -> Toast.makeText(this, "无法加载频道列表，请检查网络连接或频道源设置", Toast.LENGTH_LONG).show());
            }
        }
    }
    
    // 统一的隐藏启动画面方法
    private void hideSplashScreen() {
        // ========== 使用新架构组件 ==========
        if (splashScreenController != null) {
            splashScreenController.hide();
        }
        
        // 立即设置启动画面隐藏标志和首次播放标志为false
        isSplashScreenHidden = true;
        isFirstPlay = false;
        if (channelInfoBarController != null) {
            channelInfoBarController.setFirstPlay(false);
        }
        
        // 启动画面隐藏后，如果当前有播放频道，显示信息条
        if (currentChannel != null) {
            showChannelInfoBar(currentChannel);
        }
        
        // 重置初始化完成日志标志，以便下次可以再次记录
        hasLoggedInitializationComplete = false;
    }
    
    // 添加检查所有初始化是否完成的方法
    private void checkAllInitializationComplete() {
        
        // 检查所有初始化任务是否完成（EPG加载现在是可选的，不阻塞初始化过程）
        // 根据经验教训，确保所有必要组件都已正确初始化
        if (isChannelListLoaded && isPlayerManagerInitialized && playerController != null) {
            if (!hasLoggedInitializationComplete) {
                hasLoggedInitializationComplete = true;
            }
            checkAndPlayDefaultChannel();
            // 设置焦点到播放器容器
            if (playerContainer != null) {
                playerContainer.post(() -> playerContainer.requestFocus());
            }
        }
        
        // 如果频道列表已加载但启动画面仍未隐藏，则尝试隐藏
        if (isChannelListLoaded && !isSplashScreenHidden) {
            hideSplashScreen();
        }
    }

    // 保存最后播放的频道信息
    public void saveLastPlayedChannel(int groupIndex, int channelPosition) {
        // ========== 使用新架构组件（状态保存到MainViewModel） ==========
        if (mainViewModel != null) {
            mainViewModel.setCurrentGroupIndex(groupIndex);
            mainViewModel.setCurrentChannelIndex(channelPosition);
        }
        
        // ========== 旧代码作为后备 ==========
        if (settingsManager != null) {
            settingsManager.setLastPlayedChannel(groupIndex, channelPosition);
        }
    }

    /**
     * 显示指定频道的EPG节目清单
     * @param channel 频道对象
     */
    private void showEpgProgramList(Channel channel) {
        // 检查EPG显示设置，如果用户设置不显示EPG，则直接返回
        if (settingsManager != null && !settingsManager.isShowEpg()) {
            return;
        }
        
        if (epgController != null) {
            // 确保epgProgramList有适配器
            if (epgProgramList.getAdapter() == null) {
                epgProgramList.setAdapter(new EpgProgramAdapter(null));
            }
            epgController.showEpgProgramList(channel, epgProgramList, new LinearLayoutManager(this), 
                (EpgProgramAdapter) epgProgramList.getAdapter(), epgDrawerTitle, leftEpgDrawer,
                    (channel1, epgProgramList, adapter) -> {
                        // 在EPG数据显示完成后，定位到当前时段节目并设置焦点
                        runOnUiThread(() -> {
                            // 确保EPG节目列表获得焦点
                            if (epgProgramList != null) {
                                epgProgramList.requestFocus();

                                // 确保当前时间段的节目获得焦点
                                if (epgProgramList.getAdapter() != null && epgProgramList.getAdapter().getItemCount() > 0) {
                                    // 获取当前时间对应的节目索引
                                    int currentPosition = -1;
                                    // 使用EpgController中的方法来获取当前节目位置，确保与二级菜单中显示的当前节目完全一致
                                    if (epgController != null) {
                                        if (adapter != null) {
                                            currentPosition = epgController.getCurrentProgramPosition(channel1, adapter.getPrograms());
                                        }
                                    }

                                    // 创建final变量以供内部类使用，避免'Variable accessed from inner class'错误
                                    final int finalCurrentPosition = currentPosition;

                                    if (finalCurrentPosition >= 0) {
                                        // 滚动到当前节目位置并设置焦点
                                        epgProgramList.scrollToPosition(finalCurrentPosition);

                                        // 尝试获取焦点的子视图，如果不存在则等待布局完成
                                        UiEventUtils.setFocusWithLayoutChange(epgProgramList, finalCurrentPosition);
                                    } else {
                                        // 如果找不到当前节目，滚动到顶部并设置第一个位置为焦点
                                        epgProgramList.scrollToPosition(0);

                                        // 尝试获取焦点的子视图，如果不存在则等待布局完成
                                        UiEventUtils.setFocusToFirstWithLayoutChange(epgProgramList);
                                    }
                                }
                            }
                        });
                    });
            
            // 添加淡入动画效果
            if (leftEpgDrawer != null) {
                leftEpgDrawer.setAlpha(0f);
                leftEpgDrawer.setVisibility(View.VISIBLE);
                leftEpgDrawer.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .start();
            }
        }
    }
    
    
    // 添加统一应用所有设置的方法
    public void applyAllSettings() {
        // ========== 使用新架构组件 ==========
        // （设置模块逻辑主要在SettingsManager中，保持现状）
        
        // ========== 原有逻辑保持不变 ==========
        // 应用解码器设置
        applyDecoderSettings();
        
        // 应用画面比例设置
        applyAspectRatioSettings();
        
        // 应用显示设置
        updateDisplaySettings();
    }


    // 添加授权计时相关变量
    private long firstChannelChangeTime = -1; // 第一次换台的时间戳
    private static final long AUTH_CHECK_INTERVAL = 60 * 60 * 1000; // 60分钟检查一次

    /**
     * 获取当前播放的频道
     * @return 当前播放的频道
     */
    public Channel getCurrentChannel() {
        // ========== 使用新架构组件 ==========
        if (playerViewModel != null) {
            Channel channel = playerViewModel.getCurrentChannel();
            if (channel != null) {
                return channel;
            }
        }
        // ========== 旧代码作为后备 ==========
        return currentChannel;
    }

    /**
     * 播放指定频道
     * @param channel 要播放的频道
     */
    public void playChannel(Channel channel) {
        // ========== 使用新架构组件（状态同步） ==========
        if (playerViewModel != null && channel != null) {
            playerViewModel.setCurrentChannel(channel);
        }
        
        // ========== 原有逻辑保持不变 ==========
        if (channel == null) {
            return;
        }
        
        // 显示加载进度条
        showLoadingProgress();
        // 设置加载进度条超时隐藏任务
        setLoadingProgressTimeout();
        
        // 通知播放控制器播放频道
        if (playerController != null) {
            playerController.playChannel(channel);
        } else {
            hideLoadingProgress();
        }

        // 更新当前播放的频道引用
        currentChannel = channel;

        // 更新底部频道信息栏显示
        updateChannelInfo(channel);
        // 只有在启动画面已隐藏后才显示信息条，避免启动时过早显示
        if (!isFirstPlay || isSplashScreenHidden) {
            showChannelInfoBar(channel);
        }

        // 更新频道列表中的当前播放频道标识
        if (channelAdapter != null) {
            // 确保在主线程中更新
            if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
                channelAdapter.setCurrentPlayingChannel(channel);
            } else {
                runOnUiThread(() -> channelAdapter.setCurrentPlayingChannel(channel));
            }
        }

        // 更新频道组列表中当前播放频道所在组的高亮显示
        if (groupAdapter != null) {
            // 查找当前播放频道所在的组索引
            int groupIndex = findGroupIndexForChannel(channel);
            groupAdapter.setCurrentGroupIndex(groupIndex);
        }
        
        // 设置一个较短的超时机制，确保加载进度条不会一直显示
        if (loadingProgress != null) {
            cancelLoadingProgressTimeout();
            if (handlerManager != null) {
                handlerManager.getLoadingProgressHandler().postDelayed(() -> {
                    if (loadingProgress.getVisibility() == View.VISIBLE) {
                        loadingProgress.setVisibility(View.GONE);
                    }
                }, Constants.LOADING_PROGRESS_TIMEOUT); // 后自动隐藏，而不是10秒
            }
        }
        
        // 播放频道后，重新应用显示设置（包括网速显示）
        if (uiUpdater != null) {
            uiUpdater.updateDisplaySettings();
        }
    }

    public void updateChannelInfo(Channel channel) {
        if (channelInfoBarController != null) {
            channelInfoBarController.updateChannelInfo(channel);
        }
    }

    public void showChannelInfoBar(Channel channel) {
        if (channelInfoBarController != null) {
            channelInfoBarController.showChannelInfoBar(channel);
        }
    }

    private void hideChannelInfoBar() {
        // ========== 使用新架构组件 ==========
        if (channelInfoBarController != null) {
            channelInfoBarController.hide();
        }
        // 备用方案：直接隐藏视图
        if (channelInfoBar != null) {
            channelInfoBar.setVisibility(View.GONE);
        }
    }

    public void applyDecoderSettings() {
    }

    public void applyAspectRatioSettings() {
    }

    /**
     * 显示加载进度条
     */
    private void showLoadingProgress() {
        // ========== 使用新架构组件 ==========
        if (splashScreenController != null) {
            splashScreenController.showLoadingProgress();
        }
    }

    /**
     * 隐藏加载进度条
     */
    private void hideLoadingProgress() {
        // ========== 使用新架构组件 ==========
        if (splashScreenController != null) {
            splashScreenController.hideLoadingProgress();
        }
    }

    /**
     * 设置加载进度条超时隐藏任务
     */
    private void setLoadingProgressTimeout() {
        if (loadingProgress != null) {
            cancelLoadingProgressTimeout();
            if (handlerManager != null) {
                handlerManager.getLoadingProgressHandler().postDelayed(() -> {
                    if (loadingProgress.getVisibility() == View.VISIBLE) {
                        loadingProgress.setVisibility(View.GONE);
                    }
                }, Constants.LOADING_PROGRESS_LONG_TIMEOUT); // 后自动隐藏
            }
        }
    }

    /**
     * 取消加载进度条超时隐藏任务
     */
    private void cancelLoadingProgressTimeout() {
        if (handlerManager != null) {
            handlerManager.getLoadingProgressHandler().removeCallbacksAndMessages(null);
        }
    }

    /**
     * 更新频道组列表
     */
    private void updateGroupList() {
        if (groupAdapter != null && channelGroups != null) {
            Runnable updateRunnable = () -> {
                List<ChannelGroup> channelGroupsForMenu = new ArrayList<>();
                for (ChannelGroup group : channelGroups) {
                    if (group != null) {
                        channelGroupsForMenu.add(group);
                    }
                }
                updateFavoriteGroup();
                groupAdapter.updateData(channelGroupsForMenu);
                if (groupList != null) {
                    groupList.requestFocus();
                }
            };
            
            if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) {
                updateRunnable.run();
            } else {
                runOnUiThread(updateRunnable);
            }
        }
    }
    
    /**
     * 更新"我的收藏"组
     */
    private void updateFavoriteGroup() {
        if (favoriteController != null) {
            favoriteController.updateFavoriteGroup(channelGroups);
        }
    }

    /**
     * 显示左侧二级抽屉
     */
    private void showLeftSubDrawer() {
        if (leftSubDrawer != null) {
            leftSubDrawer.setVisibility(View.VISIBLE);
            
            // 将焦点设置到列表中的第一个频道
            if (channelList != null && channelAdapter != null) {
                // 使用post确保在布局完成后设置焦点
                UiEventUtils.postToUiThread(channelList, () -> {
                    // 遍历所有项目，找到第一个频道项并设置焦点
                    boolean focusSet = false;
                    
                    // 遍历适配器中的所有项目，找到第一个频道项
                    for (int position = 0; position < channelAdapter.getItemCount(); position++) {
                        // 使用getChannelIndicesForPosition来确定该位置是否是频道项
                        int[] indices = channelAdapter.getChannelIndicesForPosition(position);
                        int channelIndex = indices[1];
                        
                        // 如果channelIndex >= 0，说明这是一个频道项，而不是组标题
                        if (channelIndex >= 0) {
                            // 滚动到该频道项
                            channelList.scrollToPosition(position);
                            
                            // 使用Handler.postDelayed确保在布局完成后设置焦点
                            int finalPosition = position;
                            UiEventUtils.postToUiThread(new android.os.Handler(), () -> {
                                // 等待布局完成后再尝试设置焦点
                                RecyclerView.ViewHolder viewHolder = channelList.findViewHolderForAdapterPosition(finalPosition);
                                if (viewHolder != null) {
                                    viewHolder.itemView.requestFocus();
                                } else {
                                    // 如果ViewHolder不可用，等待布局完成
                                    channelList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                        @Override
                                        public void onLayoutChange(View v, int left, int top, int right, int bottom, 
                                                                    int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                            RecyclerView.ViewHolder finalViewHolder = channelList.findViewHolderForAdapterPosition(finalPosition);
                                            if (finalViewHolder != null) {
                                                finalViewHolder.itemView.requestFocus();
                                            }
                                            // 移除监听器以避免内存泄漏
                                            channelList.removeOnLayoutChangeListener(this);
                                        }
                                    });
                                }
                            });
                            
                            focusSet = true;
                            break; // 找到第一个频道后退出循环
                        }
                    }
                    
                    // 如果没有找到频道项（例如，组中没有频道），则设置焦点到组标题
                    if (!focusSet && channelAdapter.getItemCount() > 0) {
                        int groupHeaderPosition = 0;
                        channelList.scrollToPosition(groupHeaderPosition);
                        
                        UiEventUtils.postToUiThread(new android.os.Handler(), () -> {
                            RecyclerView.ViewHolder viewHolder = channelList.findViewHolderForAdapterPosition(groupHeaderPosition);
                            if (viewHolder != null) {
                                viewHolder.itemView.requestFocus();
                            } else {
                                // 等待布局完成后设置焦点
                                channelList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                    @Override
                                    public void onLayoutChange(View v, int left, int top, int right, int bottom, 
                                                                int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                        RecyclerView.ViewHolder finalViewHolder = channelList.findViewHolderForAdapterPosition(groupHeaderPosition);
                                        if (finalViewHolder != null) {
                                            finalViewHolder.itemView.requestFocus();
                                        }
                                        // 移除监听器以避免内存泄漏
                                        channelList.removeOnLayoutChangeListener(this);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    }

    /**
     * 隐藏左侧EPG抽屉
     */
    private void hideLeftEpgDrawer() {
        if (leftEpgDrawer != null && leftEpgDrawer.getVisibility() == View.VISIBLE) {
            leftEpgDrawer.setVisibility(View.GONE);
        }
    }

    /**
     * 隐藏左侧二级抽屉
     */
    private void hideLeftSubDrawer() {
        if (leftSubDrawer != null && leftSubDrawer.getVisibility() == View.VISIBLE) {
            leftSubDrawer.setVisibility(View.GONE);
        }
    }

    /**
     * 查找频道所在的组索引
     * @param channel 频道对象
     * @return 组索引，如果未找到则返回-1
     */
    private int findGroupIndexForChannel(Channel channel) {
        if (channelGroups != null && channel != null) {
            for (int i = 0; i < channelGroups.size(); i++) {
                ChannelGroup group = channelGroups.get(i);
                if (group != null && group.containsChannel(channel)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 查找频道在指定组内的索引
     * @param groupIndex 组索引
     * @param channel 频道对象
     * @return 频道在组内的索引，如果未找到则返回-1
     */
    private int findChannelIndexInGroup(int groupIndex, Channel channel) {
        if (channelGroups != null && groupIndex >= 0 && groupIndex < channelGroups.size()) {
            ChannelGroup group = channelGroups.get(groupIndex);
            if (group != null) {
                return group.getChannelIndex(channel);
            }
        }
        return -1;
    }

    /**
     * 查找频道在列表中的组索引和频道索引
     * @param channel 要查找的频道
     * @return 包含组索引和频道索引的数组，如果未找到则返回[-1, -1]
     */
    private int[] findChannelIndices(Channel channel) {
        if (channelGroups != null && channel != null) {
            for (int groupIndex = 0; groupIndex < channelGroups.size(); groupIndex++) {
                ChannelGroup group = channelGroups.get(groupIndex);
                if (group != null) {
                    int channelIndex = group.getChannelIndex(channel);
                    if (channelIndex != -1) {
                        return new int[]{groupIndex, channelIndex};
                    }
                }
            }
        }
        return new int[]{-1, -1};
    }
    
    /**
     * 获取要播放的频道
     * @return 要播放的频道，如果未找到则返回null
     */
    private Channel getChannelToPlay() {
        if (channelGroups == null || channelGroups.isEmpty()) {
            return null;
        }

        // 首先尝试获取上次播放的频道
        Channel lastPlayedChannel = null;
        if (settingsManager != null) {
            int lastGroupIndex = settingsManager.getLastPlayedChannelGroup();
            int lastChannelPosition = settingsManager.getLastPlayedChannelPosition();
            
            if (lastGroupIndex >= 0 && lastGroupIndex < channelGroups.size() &&
                lastChannelPosition >= 0) {
                ChannelGroup lastGroup = channelGroups.get(lastGroupIndex);
                if (lastGroup != null && lastChannelPosition < lastGroup.getChannelCount()) {
                    lastPlayedChannel = lastGroup.getChannel(lastChannelPosition);
                }
            }
        }
        
        // 如果没有找到上次播放的频道，返回第一个可用频道
        if (lastPlayedChannel == null) {
            for (ChannelGroup group : channelGroups) {
                if (group != null && group.getChannelCount() > 0) {
                    for (int i = 0; i < group.getChannelCount(); i++) {
                        Channel channel = group.getChannel(i);
                        if (channel != null && channel.getSources() != null && !channel.getSources().isEmpty()) {
                            return channel; // 返回第一个有可用源的频道
                        }
                    }
                }
            }
        }
        
        return lastPlayedChannel;
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // 暂停播放
        if (playerController != null) {
            playerController.pause();
        }
        
        // 停止网络速度监控
        if (uiUpdater != null) {
            uiUpdater.stopNetworkSpeedMonitoring();
        }
        
        // 停止时间更新
        if (uiUpdater != null) {
            uiUpdater.stopTimeUpdater();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 取消所有计时器任务
        cancelAllTimers();
        
        // 释放Handler管理器
        if (handlerManager != null) {
            handlerManager.release();
            handlerManager = null;
        }
        
        // 清理EPG控制器
        if (epgController != null) {
            // 清理EPG数据以释放内存
            epgController.clearEpgData();
        }
        
        // 清理授权管理器
        if (authorizationManager != null) {
            authorizationManager.destroy();
            authorizationManager = null;
        }
        
        // 清理资源
        if (ijkPlayerAdapter != null) {
            ijkPlayerAdapter.release();
            ijkPlayerAdapter = null;
        }
        
        // 注销夏杰语音接收器
        if (aiOpenReceiver != null) {
            try {
                unregisterReceiver(aiOpenReceiver);
            } catch (IllegalArgumentException e) {
                // 接收器可能已经注销
                LogUtil.w(TAG, "尝试注销AI语音接收器时出错: " + e.getMessage());
            }
            aiOpenReceiver = null;
        }
        
        // 重置初始化标志，确保下次创建时正确初始化
        isPlayerManagerInitialized = false;
        
        // 移除首选项变化监听器
        // settingsManager.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);

        // ========== 释放新架构组件 ==========
        releaseArchitectureComponents();
    }

    /**
     * 释放新架构组件资源
     */
    private void releaseArchitectureComponents() {
        // 释放 Controllers
        if (splashScreenController != null) {
            splashScreenController = null;
        }
        if (channelInfoBarController != null) {
            channelInfoBarController.release();
            channelInfoBarController = null;
        }
        if (drawerMenuController != null) {
            drawerMenuController.release();
            drawerMenuController = null;
        }
        if (touchEventHandler != null) {
            touchEventHandler.release();
            touchEventHandler = null;
        }
        if (keyEventController != null) {
            keyEventController.release();
            keyEventController = null;
        }
        if (favoriteController != null) {
            favoriteController.release();
            favoriteController = null;
        }
        if (channelNavigationController != null) {
            channelNavigationController.release();
            channelNavigationController = null;
        }
        if (menuNavigationController != null) {
            menuNavigationController.release();
            menuNavigationController = null;
        }

        // 释放 ViewModels
        mainViewModel = null;
        channelViewModel = null;
        playerViewModel = null;
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        
        // 注销更新管理器的广播接收器
        if (settingModule != null) {
            settingModule.unregisterUpdateReceiver();
        }
    }
    
    /**
     * 检查是否有任何菜单打开
     */
    private boolean isAnyMenuOpen() {
        if (mainMenuManager != null) {
            return mainMenuManager.isAnyMenuVisible();
        }
        return (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) ||
            (settingModule != null && settingModule.isSubSettingsVisible()) ||
            (leftEpgDrawer != null && leftEpgDrawer.getVisibility() == View.VISIBLE) ||
            (leftSubDrawer != null && leftSubDrawer.getVisibility() == View.VISIBLE) ||
            (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START));
    }



    /**
     * 取消所有与MainActivity相关的计时器任务
     */
    private void cancelAllTimers() {
        // 取消加载进度条的超时隐藏任务
        cancelLoadingProgressTimeout();
        
        // 取消信息条的自动隐藏任务
        if (handlerManager != null) {
            handlerManager.getInfoBarHandler().removeCallbacksAndMessages(null);
        }
        
        // 取消退出应用的延时任务
        if (handlerManager != null) {
            handlerManager.getExitHandler().removeCallbacksAndMessages(null);
        }
        
        // 取消时间更新任务
        if (handlerManager != null) {
            handlerManager.getTimeHandler().removeCallbacksAndMessages(null);
        }
        
        // 取消长按相关的任务
        if (handlerManager != null && longPressRunnable != null) {
            handlerManager.getMainHandler().removeCallbacks(longPressRunnable);
        }
        
        // 如果播放控制器存在，也取消其中的计时器
        if (playerController != null) {
            playerController.cancelAllTimers();
        }
    }
    
    private void recordFirstChannelChange() {
        if (firstChannelChangeTime == -1) {
            firstChannelChangeTime = System.currentTimeMillis();
        }
    }
    
    private void checkAuthorizationIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (authorizationManager != null && 
            (firstChannelChangeTime == -1 || (currentTime - firstChannelChangeTime) > AUTH_CHECK_INTERVAL)) {
            authorizationManager.checkAndShowAuthorizationDialog();
            firstChannelChangeTime = currentTime; // 更新检查时间
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyEventController != null) {
            boolean handled = keyEventController.onKeyDown(keyCode, event);
            if (handled) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    

    
    private boolean handleBack() {
        if (drawerLayout != null) {
            if (settingModule != null && settingModule.isSubSettingsVisible()) {
                settingModule.hideSubSettings();
                if (settingsList != null) {
                    settingsList.requestFocus();
                }
                return true;
            }
            // 2. 检查右侧主设置菜单是否打开
            else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                if (mainMenuManager != null && mainMenuManager.getSettingsMenuManager() != null) {
                    mainMenuManager.getSettingsMenuManager().hideMenu();
                } else {
                    drawerLayout.closeDrawer(GravityCompat.END);
                }
                if (playerContainer != null) {
                    playerContainer.requestFocus();
                }
                return true;
            }
            // 3. 检查左侧EPG抽屉是否可见
            else if (leftEpgDrawer != null && leftEpgDrawer.getVisibility() == View.VISIBLE) {
                if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                    mainMenuManager.getChannelMenuManager().hideEpgDrawer();
                } else {
                    leftEpgDrawer.setVisibility(View.GONE);
                }
                if (channelList != null) {
                    channelList.requestFocus();
                }
                return true;
            }
            // 4. 检查左侧二级频道菜单是否可见
            else if (leftSubDrawer != null && leftSubDrawer.getVisibility() == View.VISIBLE) {
                if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                    mainMenuManager.getChannelMenuManager().hideChannelList();
                } else {
                    leftSubDrawer.setVisibility(View.GONE);
                }
                if (groupList != null) {
                    groupList.requestFocus();
                }
                return true;
            }
            // 5. 检查左侧主频道菜单是否打开
            else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                // 如果二级菜单可见，先隐藏二级菜单，焦点回到主菜单
                if (leftSubDrawer != null && leftSubDrawer.getVisibility() == View.VISIBLE) {
                    if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                        mainMenuManager.getChannelMenuManager().hideChannelList();
                    } else {
                        leftSubDrawer.setVisibility(View.GONE);
                    }
                    if (groupList != null) {
                        groupList.requestFocus();
                    }
                } else {
                    // 如果二级菜单不可见，关闭主菜单
                    if (mainMenuManager != null && mainMenuManager.getChannelMenuManager() != null) {
                        mainMenuManager.getChannelMenuManager().hideMenu();
                    } else {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                    if (playerContainer != null) {
                        playerContainer.requestFocus();
                    }
                }
                return true;
            }
        }
        
        // 双击返回键退出应用
        if (isBackPressedOnce) {
            finish();
        } else {
            isBackPressedOnce = true;
            Toast.makeText(this, "再按一次退出应用", Toast.LENGTH_SHORT).show();
            if (handlerManager != null) {
                handlerManager.getExitHandler().postDelayed(exitRunnable, EXIT_DELAY);
            }
        }
        return true;
    }
    
    private boolean handleMenuKey() {
        LogUtil.d(TAG, "Menu key pressed");
        if (drawerLayout != null) {
            if (settingModule != null && settingModule.isSubSettingsVisible()) {
                LogUtil.d(TAG, "Closing sub settings");
                settingModule.hideSubSettings();
                if (settingsList != null) {
                    settingsList.requestFocus();
                }
                return true;
            }
            else if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                LogUtil.d(TAG, "Closing right drawer");
                drawerLayout.closeDrawer(GravityCompat.END);
                if (playerContainer != null) {
                    playerContainer.requestFocus();
                }
                return true;
            }
            else {
                LogUtil.d(TAG, "Opening right drawer with drawerLayout.openDrawer()");
                hideChannelInfoBar();
                drawerLayout.openDrawer(GravityCompat.END);
                if (settingsList != null) {
                    settingsList.post(() -> {
                        settingsList.requestFocus();
                        if (settingsList.getAdapter() != null && 
                            settingsList.getAdapter().getItemCount() > 0) {
                            settingsList.scrollToPosition(0);
                            UiEventUtils.setFocusToFirstWithLayoutChange(settingsList);
                        }
                    });
                    settingsList.postDelayed(() -> {
                        settingsList.requestFocus();
                    }, 300);
                }
                return true;
            }
        }
        LogUtil.d(TAG, "DrawerLayout is null");
        return false;
    }
    

    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyEventController != null) {
            boolean handled = keyEventController.onKeyUp(keyCode, event);
            if (handled) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.END)) {
            if (settingModule != null && settingModule.isSubSettingsVisible()) {
                boolean hasAnyFocus = false;
                if (settingsDetailList != null && settingsDetailList.hasFocus()) {
                    hasAnyFocus = true;
                }
                if (settingsList != null && settingsList.hasFocus()) {
                    hasAnyFocus = true;
                }
                if (!hasAnyFocus) {
                    if (settingsDetailList != null) {
                        settingsDetailList.requestFocus();
                    }
                }
            } else {
                if (settingsList != null && !settingsList.hasFocus()) {
                    settingsList.requestFocus();
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }
    

    
    private void loadChannelList() {
        if (channelRepository != null) {
            channelRepository.loadChannelList(this, settingsManager);
            
            // 临时添加观察者来处理初始化完成标志
            channelRepository.getChannelGroups().observe(this, groups -> {
                if (groups != null) {
                    isChannelListLoaded = true;
                    if (mainViewModel != null) {
                        mainViewModel.setChannelListLoaded(true);
                    }
                    checkAllInitializationComplete();
                }
            });

            channelRepository.getErrorMessage().observe(this, error -> {
                if (error != null && !error.isEmpty()) {
                    isChannelListLoaded = true;
                    if (mainViewModel != null) {
                        mainViewModel.setChannelListLoaded(true);
                    }
                    checkAllInitializationComplete();
                }
            });
        }
    }

    public void tryPlayNextChannel() {
        if (channelNavigationController != null) {
            channelNavigationController.tryPlayNextChannel();
        }
    }
    

    public ChannelAdapter getFullChannelAdapter() {
        if (channelGroups != null && channelAdapter != null) {
            // 创建一个包含所有频道组的临时ChannelAdapter用于EPG匹配
            return new ChannelAdapter(new ArrayList<>(channelGroups), null, settingsManager, MainActivity.this.getApplicationContext());
        }
        return channelAdapter;
    }
    
    /**
     * 创建主菜单容器
     * @param firstMenuWidth 一级菜单宽度
     * @param secondMenuWidth 二级菜单宽度
     * @return 主菜单容器LinearLayout
     */
    private LinearLayout createMainMenuContainer(int firstMenuWidth, int secondMenuWidth) {
        LinearLayout mainMenuContainer = new LinearLayout(this);
        mainMenuContainer.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                firstMenuWidth, // 只占据一级菜单的宽度
                FrameLayout.LayoutParams.MATCH_PARENT);
        containerParams.setMargins(secondMenuWidth, 0, 0, 0); // 位于占位视图右侧
        mainMenuContainer.setLayoutParams(containerParams);
        return mainMenuContainer;
    }
    
    @Override
    public void onChannelClick(Channel channel, int groupIndex, int channelIndex) {
        currentChannelIndex = channelIndex;
        currentGroupIndex = groupIndex;
        playChannel(channel);
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onEpgInfoClick(Channel channel) {
        showEpgProgramList(channel);
    }

    @Override
    public void onGroupClick(ChannelGroup group, int groupIndex) {

    }
}