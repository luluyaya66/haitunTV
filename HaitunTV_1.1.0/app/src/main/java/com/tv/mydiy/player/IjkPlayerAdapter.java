package com.tv.mydiy.player;

import android.util.Log;
import android.view.SurfaceHolder;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static tv.danmaku.ijk.media.player.IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;

public class IjkPlayerAdapter {
    private static final String TAG = "IjkPlayerAdapter";
    
    private IjkMediaPlayer ijkMediaPlayer;
    // private CustomStreamLoader streamLoader;
    
    // 监听器
    private IMediaPlayer.OnPreparedListener preparedListener;
    private IMediaPlayer.OnCompletionListener completionListener;
    private IMediaPlayer.OnErrorListener errorListener;
    private IMediaPlayer.OnInfoListener infoListener;
    
    private SurfaceHolder surfaceHolder;
    
    public IjkPlayerAdapter() {
        // this.streamLoader = new CustomStreamLoader();
        initializePlayer();
    }
    
    private void initializePlayer() {
        try {
            // 释放现有播放器实例（如果存在）
            if (ijkMediaPlayer != null) {
                ijkMediaPlayer.release();
            }
            
            // 创建新的IjkMediaPlayer实例
            ijkMediaPlayer = new IjkMediaPlayer();
            
            // 减少日志输出级别，生产环境使用更少的日志
            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_ERROR);
            
            // 设置播放器选项
            // 参考配置中的关键选项
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0); // 不自动开始播放
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1); // 自动开始播放
            
            // 增强网络连接配置，根据规范[ed3e4234-0913-4ef0-a6d6-4db8e2273dfa]
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1); // 每次解析前清除DNS缓存
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1); // 禁用DNS缓存
            
            // 设置合适的缓冲区大小和超时时间，减少缓冲波动
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 15); // 最小帧数
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1024*1024); // 1MB缓冲区
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rw_timeout", 5000000); // 5秒读写超时
            
            // 启用HTTP重定向支持
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "enable-http-redirect", 1);
            
            // 设置TCP_NODELAY以减少延迟
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "tcp_nodelay", 1);
            
            // 设置较低的初始缓冲区大小以加快首帧显示
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "first_frame_fast_retry", 1);
            
            // 设置解码器选项
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 0);
            
            // 设置监听器
            setupListeners();
            
            // Log.d(TAG, "IjkMediaPlayer initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "初始化IjkMediaPlayer失败", e);
        }
    }

    private void setupListeners() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.setOnPreparedListener(mp -> {
                if (preparedListener != null) {
                    preparedListener.onPrepared(mp);
                }
                // Log.d(TAG, "Player prepared");
            });
            
            ijkMediaPlayer.setOnCompletionListener(mp -> {
                if (completionListener != null) {
                    completionListener.onCompletion(mp);
                }
            });
            
            ijkMediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Player error: what=" + what + ", extra=" + extra);
                if (errorListener != null) {
                    return errorListener.onError(mp, what, extra);
                }
                return false;
            });
            
            ijkMediaPlayer.setOnInfoListener((mp, what, extra) -> {
                // 检查是否是视频渲染开始的信息
                if (what == MEDIA_INFO_VIDEO_RENDERING_START) {
                    // Log.d(TAG, "Video rendering started");
                    // 视频开始渲染，通知MainActivity
                    if (infoListener != null) {
                        return infoListener.onInfo(mp, what, extra);
                    }
                }  // 缓冲开始

                if (infoListener != null) {
                    return infoListener.onInfo(mp, what, extra);
                }
                return false;
            });
        }
    }

    public void setDataSource(String path) throws IllegalArgumentException, SecurityException, IllegalStateException {
        // Log.v(TAG, "setDataSource: path " + path);
        if (ijkMediaPlayer == null) {
            throw new IllegalStateException("Media player is not initialized");
        }
        
        try {
            // 准备播放器以接收新的数据源
            preparePlayerForNewSource();
            
            // 检查是否需要使用自定义流加载器
            /* if (path != null && path.contains("migu.188766.xyz")) {
                Log.d(TAG, "Using custom stream loader for migu source: " + path);
                // 使用自定义流加载器处理咪咕源
                try {
                    File tempFile = streamLoader.loadStreamToFile(path);
                    // 检查文件是否存在以及大小是否大于0
                    if (tempFile != null && tempFile.exists()) {
                        Log.d(TAG, "Temp file exists, size: " + tempFile.length() + " bytes");
                        if (tempFile.length() > 0) {
                            Log.d(TAG, "Setting data source to temp file: " + tempFile.getPath());
                            ijkMediaPlayer.setDataSource(tempFile.getPath());
                        } else {
                            Log.e(TAG, "Temp file is empty, falling back to direct URL: " + path);
                            // 如果临时文件为空，回退到默认方式
                            ijkMediaPlayer.setDataSource(path);
                        }
                    } else {
                        Log.e(TAG, "Failed to create temp file, falling back to direct URL: " + path);
                        // 如果临时文件创建失败，回退到默认方式
                        ijkMediaPlayer.setDataSource(path);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Failed to load migu stream, falling back to direct URL: " + path + ", error: " + e.getMessage(), e);
                    // 如果自定义加载失败，回退到默认方式
                    ijkMediaPlayer.setDataSource(path);
                }
            } else */{
                // Log.d(TAG, "Not a migu source or path is null, using default method. Path: " + path);
                // 根据URL类型应用不同的配置
                if (path != null) {
                    applySourceSpecificConfigurations();
                }
                
                ijkMediaPlayer.setDataSource(path);
            }
        } catch (Exception e) {
            Log.e(TAG, "设置数据源失败: " + e.getMessage(), e);
            Log.e(TAG, "失败的数据源路径: " + path);
            throw e;
        }
    }
    
    /**
     * 根据片源URL应用特定的配置
     * 根据经验教训[bec7fb65-71d4-4281-8e34-3bf4150d9a4b]，对不同片源采用差异化配置策略
     */
    private void applySourceSpecificConfigurations() {
        if (ijkMediaPlayer == null) return;
        
        // 为所有源设置统一的通用配置
        // Log.d(TAG, "Applying default source configurations");
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "file,http,https,tcp,tls");
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "allowed_extensions", "ALL");
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1L);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0L);
    }
    
    /**
     * 准备播放器以接收新的数据源
     * 根据规范[f51bbc56-e089-447a-97b3-c579a0332103]，避免调用reset()方法，
     * 改为仅调用stop()确保安全状态
     */
    private void preparePlayerForNewSource() {
        if (ijkMediaPlayer != null) {
            try {
                // 检查播放器当前状态
                if (ijkMediaPlayer.isPlaying()) {
                    ijkMediaPlayer.stop();
                }
                // 重置播放器以准备接受新的数据源
                ijkMediaPlayer.reset();
            } catch (Exception e) {
                Log.e(TAG, "Error resetting media player: " + e.getMessage(), e);
            }
        }
    }

    public void prepareAsync() {
        if (ijkMediaPlayer != null) {
            // 根据规范[d8690001-e86f-40f7-9653-c9a86ad8dd20]，在调用prepareAsync前必须检查SurfaceHolder的Surface是否有效
            if (isSurfaceValid(surfaceHolder)) {
                ijkMediaPlayer.setDisplay(surfaceHolder);
                // Log.d(TAG, "Surface is valid, setting display before prepare");
            }
            ijkMediaPlayer.prepareAsync();
        }
    }

    public void start() {
        if (ijkMediaPlayer != null) {
            // 在开始播放前确保Surface已设置
            // 根据规范[d8690001-e86f-40f7-9653-c9a86ad8dd20]，必须检查Surface是否有效
            if (isSurfaceValid(surfaceHolder)) {
                ijkMediaPlayer.setDisplay(surfaceHolder);
                // Log.d(TAG, "Surface is valid, setting display before start");
            }
            ijkMediaPlayer.start();
        }
    }

    public void pause() {
        if (ijkMediaPlayer != null && ijkMediaPlayer.isPlaying()) {
            ijkMediaPlayer.pause();
        }
    }

    public void stop() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.stop();
        }
    }

    public void release() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.release();
            ijkMediaPlayer = null;
        }
    }

    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener listener) {
        this.completionListener = listener;
    }

    public void setOnErrorListener(IMediaPlayer.OnErrorListener listener) {
        this.errorListener = listener;
    }

    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener listener) {
        this.preparedListener = listener;
    }

    public void setOnInfoListener(IMediaPlayer.OnInfoListener listener) {
        this.infoListener = listener;
    }

    /**
     * 添加Surface相关方法
     */
    public void setDisplay(SurfaceHolder sh) {
        this.surfaceHolder = sh;
        if (ijkMediaPlayer != null && isSurfaceValid(sh)) {
            ijkMediaPlayer.setDisplay(sh);
        }
    }
    
    private boolean isSurfaceValid(SurfaceHolder sh) {
        return sh != null && sh.getSurface() != null && sh.getSurface().isValid();
    }
}