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
            
            ijkMediaPlayer = new IjkMediaPlayer();

            IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_ERROR);

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
            // ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0); // 不自动开始播放
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 1); // 自动开始播放

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1); // 每次解析前清除DNS缓存
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1); // 禁用DNS缓存

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "min-frames", 15); // 最小帧数
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", 1024*1024); // 1MB缓冲区
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rw_timeout", 5000000); // 5秒读写超时

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "enable-http-redirect", 1);

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "tcp_nodelay", 1);

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "first_frame_fast_retry", 1);

            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);
            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 0);

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

                if (what == MEDIA_INFO_VIDEO_RENDERING_START) {
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
        if (ijkMediaPlayer == null) {
            throw new IllegalStateException("Media player is not initialized");
        }
        
        try {
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

    private void applySourceSpecificConfigurations() {
        if (ijkMediaPlayer == null) return;

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "file,http,https,tcp,tls");
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "allowed_extensions", "ALL");
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 1L);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0L);
    }

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
            if (isSurfaceValid(surfaceHolder)) {
                ijkMediaPlayer.setDisplay(surfaceHolder);
                // Log.d(TAG, "Surface is valid, setting display before prepare");
            }
            ijkMediaPlayer.prepareAsync();
        }
    }

    public void start() {
        if (ijkMediaPlayer != null) {

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

