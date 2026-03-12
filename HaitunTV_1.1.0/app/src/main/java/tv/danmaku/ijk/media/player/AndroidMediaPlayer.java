package tv.danmaku.ijk.media.player;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import tv.danmaku.ijk.media.player.misc.AndroidTrackInfo;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import tv.danmaku.ijk.media.player.pragma.DebugLog;

public class AndroidMediaPlayer extends AbstractMediaPlayer {
    public static MediaInfo sMediaInfo;
    public String mDataSource;
    public final Object mInitLock;
    public final a mInternalListenerAdapter;
    public final MediaPlayer mInternalMediaPlayer;
    public boolean mIsReleased;
    public MediaDataSource mMediaDataSource;

    @TargetApi(23)
    public static class b extends MediaDataSource {
        public final IMediaDataSource b;

        public b(IMediaDataSource iMediaDataSource) {
            this.b = iMediaDataSource;
        }

        public void close() {
            this.b.close();
        }

        public long getSize() {
            return this.b.getSize();
        }

        public int readAt(long j, byte[] bArr, int i, int i2) {
            return this.b.readAt(j, bArr, i, i2);
        }
    }

    public class a implements IMediaPlayer.OnPreparedListener, OnCompletionListener, OnBufferingUpdateListener, OnSeekCompleteListener, OnVideoSizeChangedListener, OnErrorListener, OnInfoListener, OnTimedTextListener {
        public final WeakReference<AndroidMediaPlayer> a;
        public final /* synthetic */ AndroidMediaPlayer b;

        public a(AndroidMediaPlayer androidMediaPlayer, AndroidMediaPlayer androidMediaPlayer2) {
            this.b = androidMediaPlayer;
            this.a = new WeakReference(androidMediaPlayer2);
        }

        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
            if (((AndroidMediaPlayer) this.a.get()) != null) {
                this.b.notifyOnBufferingUpdate(i);
            }
        }

        public void onCompletion(IMediaPlayer iMediaPlayer) {
            if (((AndroidMediaPlayer) this.a.get()) != null) {
                this.b.notifyOnCompletion();
            }
        }

        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i2) {
            return ((AndroidMediaPlayer) this.a.get()) != null && this.b.notifyOnError(i, i2);
        }

        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i2) {
            return ((AndroidMediaPlayer) this.a.get()) != null && this.b.notifyOnInfo(i, i2);
        }

        public void onPrepared(IMediaPlayer iMediaPlayer) {
            if (((AndroidMediaPlayer) this.a.get()) != null) {
                this.b.notifyOnPrepared();
            }
        }

        public void onSeekComplete(IMediaPlayer iMediaPlayer) {
            if (((AndroidMediaPlayer) this.a.get()) != null) {
                this.b.notifyOnSeekComplete();
            }
        }

        public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {
            if (((AndroidMediaPlayer) this.a.get()) != null) {
                this.b.notifyOnTimedText(ijkTimedText);
            }
        }

        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i2, int i3, int i4) {
            if (((AndroidMediaPlayer) this.a.get()) != null) {
                this.b.notifyOnVideoSizeChanged(i, i2, i3, i4);
            }
        }
    }

    public AndroidMediaPlayer() {
        MediaPlayer mediaPlayer;
        Object obj = new Object();
        this.mInitLock = obj;
        synchronized (obj) {
            mediaPlayer = new MediaPlayer();
            this.mInternalMediaPlayer = mediaPlayer;
        }
        mediaPlayer.setAudioStreamType(3);
        this.mInternalListenerAdapter = new a(this, this);
        attachInternalListeners();
    }

    private void attachInternalListeners() {
        this.mInternalMediaPlayer.setOnPreparedListener((MediaPlayer.OnPreparedListener) this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnBufferingUpdateListener((MediaPlayer.OnBufferingUpdateListener) this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnCompletionListener((MediaPlayer.OnCompletionListener) this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnSeekCompleteListener((MediaPlayer.OnSeekCompleteListener) this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnVideoSizeChangedListener((MediaPlayer.OnVideoSizeChangedListener) this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnErrorListener((MediaPlayer.OnErrorListener) this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnInfoListener((MediaPlayer.OnInfoListener) this.mInternalListenerAdapter);
        this.mInternalMediaPlayer.setOnTimedTextListener((MediaPlayer.OnTimedTextListener) this.mInternalListenerAdapter);
    }

    private void releaseMediaDataSource() {
        MediaDataSource mediaDataSource = this.mMediaDataSource;
        if (mediaDataSource != null) {
            try {
                mediaDataSource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.mMediaDataSource = null;
        }
    }

    public int getAudioSessionId() {
        return this.mInternalMediaPlayer.getAudioSessionId();
    }

    public long getCurrentPosition() {
        try {
            return (long) this.mInternalMediaPlayer.getCurrentPosition();
        } catch (Throwable e) {
            DebugLog.printStackTrace(e);
            return 0;
        }
    }

    public String getDataSource() {
        return this.mDataSource;
    }

    public long getDuration() {
        try {
            return (long) this.mInternalMediaPlayer.getDuration();
        } catch (Throwable e) {
            DebugLog.printStackTrace(e);
            return 0;
        }
    }

    public MediaPlayer getInternalMediaPlayer() {
        return this.mInternalMediaPlayer;
    }

    public MediaInfo getMediaInfo() {
        if (sMediaInfo == null) {
            MediaInfo mediaInfo = new MediaInfo();
            String str = "android";
            mediaInfo.mVideoDecoder = str;
            String str2 = "HW";
            mediaInfo.mVideoDecoderImpl = str2;
            mediaInfo.mAudioDecoder = str;
            mediaInfo.mAudioDecoderImpl = str2;
            sMediaInfo = mediaInfo;
        }
        return sMediaInfo;
    }

    public ITrackInfo[] getTrackInfo() {
        return AndroidTrackInfo.fromMediaPlayer(this.mInternalMediaPlayer);
    }

    public int getVideoHeight() {
        return this.mInternalMediaPlayer.getVideoHeight();
    }

    public int getVideoSarDen() {
        return 1;
    }

    public int getVideoSarNum() {
        return 1;
    }

    public int getVideoWidth() {
        return this.mInternalMediaPlayer.getVideoWidth();
    }

    public boolean isLooping() {
        return this.mInternalMediaPlayer.isLooping();
    }

    public boolean isPlayable() {
        return true;
    }

    public boolean isPlaying() {
        try {
            return this.mInternalMediaPlayer.isPlaying();
        } catch (Throwable e) {
            DebugLog.printStackTrace(e);
            return false;
        }
    }

    public void pause() {
        this.mInternalMediaPlayer.pause();
    }

    public void prepareAsync() {
        this.mInternalMediaPlayer.prepareAsync();
    }

    public void release() {
        this.mIsReleased = true;
        this.mInternalMediaPlayer.release();
        releaseMediaDataSource();
        resetListeners();
        attachInternalListeners();
    }

    public void reset() {
        try {
            this.mInternalMediaPlayer.reset();
        } catch (Throwable e) {
            DebugLog.printStackTrace(e);
        }
        releaseMediaDataSource();
        resetListeners();
        attachInternalListeners();
    }

    public void seekTo(long j) {
        this.mInternalMediaPlayer.seekTo((int) j);
    }

    public void setAudioStreamType(int i) {
        this.mInternalMediaPlayer.setAudioStreamType(i);
    }

    public void setDataSource(Context context, Uri uri) {
        try {
            this.mInternalMediaPlayer.setDataSource(context, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(14)
    public void setDataSource(Context context, Uri uri, Map<String, String> map) {
        try {
            this.mInternalMediaPlayer.setDataSource(context, uri, map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDataSource(FileDescriptor fileDescriptor) {
        try {
            this.mInternalMediaPlayer.setDataSource(fileDescriptor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDataSource(String str) {
        this.mDataSource = str;
        Uri parse = Uri.parse(str);
        Object scheme = parse.getScheme();
        if (TextUtils.isEmpty((CharSequence) scheme) || !((String) scheme).equalsIgnoreCase("file")) {
            try {
                this.mInternalMediaPlayer.setDataSource(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.mInternalMediaPlayer.setDataSource(parse.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @TargetApi(23)
    public void setDataSource(IMediaDataSource iMediaDataSource) {
        releaseMediaDataSource();
        b bVar = new b(iMediaDataSource);
        this.mMediaDataSource = bVar;
        this.mInternalMediaPlayer.setDataSource(bVar);
    }

    public void setDisplay(SurfaceHolder surfaceHolder) {
        synchronized (this.mInitLock) {
            if (!this.mIsReleased) {
                this.mInternalMediaPlayer.setDisplay(surfaceHolder);
            }
        }
    }

    public void setKeepInBackground(boolean z) {
    }

    public void setLogEnabled(boolean z) {
    }

    public void setLooping(boolean z) {
        this.mInternalMediaPlayer.setLooping(z);
    }

    public void setScreenOnWhilePlaying(boolean z) {
        this.mInternalMediaPlayer.setScreenOnWhilePlaying(z);
    }

    @TargetApi(14)
    public void setSurface(Surface surface) {
        this.mInternalMediaPlayer.setSurface(surface);
    }

    public void setVolume(float f, float f2) {
        this.mInternalMediaPlayer.setVolume(f, f2);
    }

    public void setWakeMode(Context context, int i) {
        this.mInternalMediaPlayer.setWakeMode(context, i);
    }

    public void start() {
        this.mInternalMediaPlayer.start();
    }

    public void stop() {
        this.mInternalMediaPlayer.stop();
    }
}
