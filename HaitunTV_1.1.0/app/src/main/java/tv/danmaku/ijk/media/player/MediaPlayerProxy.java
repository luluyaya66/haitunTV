package tv.danmaku.ijk.media.player;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.FileDescriptor;
import java.util.Map;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnTimedTextListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;

public class MediaPlayerProxy implements IMediaPlayer {
    public final IMediaPlayer mBackEndMediaPlayer;

    public class b implements OnCompletionListener {
        public final /* synthetic */ OnCompletionListener b;
        public final /* synthetic */ MediaPlayerProxy c;

        public b(MediaPlayerProxy mediaPlayerProxy, OnCompletionListener onCompletionListener) {
            this.c = mediaPlayerProxy;
            this.b = onCompletionListener;
        }

        public void onCompletion(IMediaPlayer iMediaPlayer) {
            this.b.onCompletion(this.c);
        }
    }

    public class c implements OnBufferingUpdateListener {
        public final /* synthetic */ OnBufferingUpdateListener a;
        public final /* synthetic */ MediaPlayerProxy b;

        public c(MediaPlayerProxy mediaPlayerProxy, OnBufferingUpdateListener onBufferingUpdateListener) {
            this.b = mediaPlayerProxy;
            this.a = onBufferingUpdateListener;
        }

        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
            this.a.onBufferingUpdate(this.b, i);
        }
    }

    public class d implements OnSeekCompleteListener {
        public final /* synthetic */ OnSeekCompleteListener a;
        public final /* synthetic */ MediaPlayerProxy b;

        public d(MediaPlayerProxy mediaPlayerProxy, OnSeekCompleteListener onSeekCompleteListener) {
            this.b = mediaPlayerProxy;
            this.a = onSeekCompleteListener;
        }

        public void onSeekComplete(IMediaPlayer iMediaPlayer) {
            this.a.onSeekComplete(this.b);
        }
    }

    public class e implements OnVideoSizeChangedListener {
        public final /* synthetic */ OnVideoSizeChangedListener a;
        public final /* synthetic */ MediaPlayerProxy b;

        public e(MediaPlayerProxy mediaPlayerProxy, OnVideoSizeChangedListener onVideoSizeChangedListener) {
            this.b = mediaPlayerProxy;
            this.a = onVideoSizeChangedListener;
        }

        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i2, int i3, int i4) {
            this.a.onVideoSizeChanged(this.b, i, i2, i3, i4);
        }
    }

    public class f implements OnErrorListener {
        public final /* synthetic */ OnErrorListener b;
        public final /* synthetic */ MediaPlayerProxy c;

        public f(MediaPlayerProxy mediaPlayerProxy, OnErrorListener onErrorListener) {
            this.c = mediaPlayerProxy;
            this.b = onErrorListener;
        }

        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i2) {
            return this.b.onError(this.c, i, i2);
        }
    }

    public class g implements OnInfoListener {
        public final /* synthetic */ OnInfoListener b;
        public final /* synthetic */ MediaPlayerProxy c;

        public g(MediaPlayerProxy mediaPlayerProxy, OnInfoListener onInfoListener) {
            this.c = mediaPlayerProxy;
            this.b = onInfoListener;
        }

        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i2) {
            return this.b.onInfo(this.c, i, i2);
        }
    }

    public class h implements OnTimedTextListener {
        public final /* synthetic */ OnTimedTextListener a;
        public final /* synthetic */ MediaPlayerProxy b;

        public h(MediaPlayerProxy mediaPlayerProxy, OnTimedTextListener onTimedTextListener) {
            this.b = mediaPlayerProxy;
            this.a = onTimedTextListener;
        }

        public void onTimedText(IMediaPlayer iMediaPlayer, IjkTimedText ijkTimedText) {
            this.a.onTimedText(this.b, ijkTimedText);
        }
    }

    public class a implements OnPreparedListener {
        public final /* synthetic */ OnPreparedListener b;
        public final /* synthetic */ MediaPlayerProxy c;

        public a(MediaPlayerProxy mediaPlayerProxy, OnPreparedListener onPreparedListener) {
            this.c = mediaPlayerProxy;
            this.b = onPreparedListener;
        }

        public void onPrepared(IMediaPlayer iMediaPlayer) {
            this.b.onPrepared(this.c);
        }
    }

    public MediaPlayerProxy(IMediaPlayer iMediaPlayer) {
        this.mBackEndMediaPlayer = iMediaPlayer;
    }

    public int getAudioSessionId() {
        return this.mBackEndMediaPlayer.getAudioSessionId();
    }

    public long getCurrentPosition() {
        return this.mBackEndMediaPlayer.getCurrentPosition();
    }

    public String getDataSource() {
        return this.mBackEndMediaPlayer.getDataSource();
    }

    public long getDuration() {
        return this.mBackEndMediaPlayer.getDuration();
    }

    public IMediaPlayer getInternalMediaPlayer() {
        return this.mBackEndMediaPlayer;
    }

    public MediaInfo getMediaInfo() {
        return this.mBackEndMediaPlayer.getMediaInfo();
    }

    public ITrackInfo[] getTrackInfo() {
        return this.mBackEndMediaPlayer.getTrackInfo();
    }

    public int getVideoHeight() {
        return this.mBackEndMediaPlayer.getVideoHeight();
    }

    public int getVideoSarDen() {
        return this.mBackEndMediaPlayer.getVideoSarDen();
    }

    public int getVideoSarNum() {
        return this.mBackEndMediaPlayer.getVideoSarNum();
    }

    public int getVideoWidth() {
        return this.mBackEndMediaPlayer.getVideoWidth();
    }

    public boolean isLooping() {
        return this.mBackEndMediaPlayer.isLooping();
    }

    public boolean isPlayable() {
        return false;
    }

    public boolean isPlaying() {
        return this.mBackEndMediaPlayer.isPlaying();
    }

    public void pause() {
        this.mBackEndMediaPlayer.pause();
    }

    public void prepareAsync() {
        this.mBackEndMediaPlayer.prepareAsync();
    }

    public void release() {
        this.mBackEndMediaPlayer.release();
    }

    public void reset() {
        this.mBackEndMediaPlayer.reset();
    }

    public void seekTo(long j) {
        this.mBackEndMediaPlayer.seekTo(j);
    }

    public void setAudioStreamType(int i) {
        this.mBackEndMediaPlayer.setAudioStreamType(i);
    }

    public void setDataSource(Context context, Uri uri) {
        this.mBackEndMediaPlayer.setDataSource(context, uri);
    }

    @TargetApi(14)
    public void setDataSource(Context context, Uri uri, Map<String, String> map) {
        this.mBackEndMediaPlayer.setDataSource(context, uri, map);
    }

    public void setDataSource(FileDescriptor fileDescriptor) {
        this.mBackEndMediaPlayer.setDataSource(fileDescriptor);
    }

    public void setDataSource(String str) {
        this.mBackEndMediaPlayer.setDataSource(str);
    }

    public void setDataSource(IMediaDataSource iMediaDataSource) {
        this.mBackEndMediaPlayer.setDataSource(iMediaDataSource);
    }

    public void setDisplay(SurfaceHolder surfaceHolder) {
        this.mBackEndMediaPlayer.setDisplay(surfaceHolder);
    }

    public void setKeepInBackground(boolean z) {
        this.mBackEndMediaPlayer.setKeepInBackground(z);
    }

    public void setLogEnabled(boolean z) {
    }

    public void setLooping(boolean z) {
        this.mBackEndMediaPlayer.setLooping(z);
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener onBufferingUpdateListener) {
        if (onBufferingUpdateListener != null) {
            this.mBackEndMediaPlayer.setOnBufferingUpdateListener(new c(this, onBufferingUpdateListener));
        } else {
            this.mBackEndMediaPlayer.setOnBufferingUpdateListener(null);
        }
    }

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        if (onCompletionListener != null) {
            this.mBackEndMediaPlayer.setOnCompletionListener(new b(this, onCompletionListener));
        } else {
            this.mBackEndMediaPlayer.setOnCompletionListener(null);
        }
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
        if (onErrorListener != null) {
            this.mBackEndMediaPlayer.setOnErrorListener(new f(this, onErrorListener));
        } else {
            this.mBackEndMediaPlayer.setOnErrorListener(null);
        }
    }

    public void setOnInfoListener(OnInfoListener onInfoListener) {
        if (onInfoListener != null) {
            this.mBackEndMediaPlayer.setOnInfoListener(new g(this, onInfoListener));
        } else {
            this.mBackEndMediaPlayer.setOnInfoListener(null);
        }
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        if (onPreparedListener != null) {
            this.mBackEndMediaPlayer.setOnPreparedListener(new a(this, onPreparedListener));
        } else {
            this.mBackEndMediaPlayer.setOnPreparedListener(null);
        }
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener onSeekCompleteListener) {
        if (onSeekCompleteListener != null) {
            this.mBackEndMediaPlayer.setOnSeekCompleteListener(new d(this, onSeekCompleteListener));
        } else {
            this.mBackEndMediaPlayer.setOnSeekCompleteListener(null);
        }
    }

    public void setOnTimedTextListener(OnTimedTextListener onTimedTextListener) {
        if (onTimedTextListener != null) {
            this.mBackEndMediaPlayer.setOnTimedTextListener(new h(this, onTimedTextListener));
        } else {
            this.mBackEndMediaPlayer.setOnTimedTextListener(null);
        }
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
        if (onVideoSizeChangedListener != null) {
            this.mBackEndMediaPlayer.setOnVideoSizeChangedListener(new e(this, onVideoSizeChangedListener));
        } else {
            this.mBackEndMediaPlayer.setOnVideoSizeChangedListener(null);
        }
    }

    public void setScreenOnWhilePlaying(boolean z) {
        this.mBackEndMediaPlayer.setScreenOnWhilePlaying(z);
    }

    @TargetApi(14)
    public void setSurface(Surface surface) {
        this.mBackEndMediaPlayer.setSurface(surface);
    }

    public void setVolume(float f, float f2) {
        this.mBackEndMediaPlayer.setVolume(f, f2);
    }

    public void setWakeMode(Context context, int i) {
        this.mBackEndMediaPlayer.setWakeMode(context, i);
    }

    public void start() {
        this.mBackEndMediaPlayer.start();
    }

    public void stop() {
        this.mBackEndMediaPlayer.stop();
    }
}
