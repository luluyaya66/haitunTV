package tv.danmaku.ijk.media.player.misc;

import android.annotation.TargetApi;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.TrackInfo;
import android.os.Build.VERSION;

public class AndroidTrackInfo implements ITrackInfo {
    public final TrackInfo mTrackInfo;

    public AndroidTrackInfo(TrackInfo trackInfo) {
        this.mTrackInfo = trackInfo;
    }

    public static AndroidTrackInfo[] fromMediaPlayer(MediaPlayer mediaPlayer) {
        return fromTrackInfo(mediaPlayer.getTrackInfo());
    }

    public static AndroidTrackInfo[] fromTrackInfo(TrackInfo[] trackInfoArr) {
        if (trackInfoArr == null) {
            return null;
        }
        AndroidTrackInfo[] androidTrackInfoArr = new AndroidTrackInfo[trackInfoArr.length];
        for (int i = 0; i < trackInfoArr.length; i++) {
            androidTrackInfoArr[i] = new AndroidTrackInfo(trackInfoArr[i]);
        }
        return androidTrackInfoArr;
    }

    @TargetApi(19)
    public IMediaFormat getFormat() {
        TrackInfo trackInfo = this.mTrackInfo;
        if (trackInfo == null || VERSION.SDK_INT < 19) {
            return null;
        }
        MediaFormat format = trackInfo.getFormat();
        return format == null ? null : new AndroidMediaFormat(format);
    }

    @TargetApi(16)
    public String getInfoInline() {
        TrackInfo trackInfo = this.mTrackInfo;
        return trackInfo != null ? trackInfo.toString() : "null";
    }

    @TargetApi(16)
    public String getLanguage() {
        TrackInfo trackInfo = this.mTrackInfo;
        return trackInfo == null ? "und" : trackInfo.getLanguage();
    }

    @TargetApi(16)
    public int getTrackType() {
        TrackInfo trackInfo = this.mTrackInfo;
        return trackInfo == null ? 0 : trackInfo.getTrackType();
    }

    @TargetApi(16)
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(128);
        stringBuilder.append(AndroidTrackInfo.class.getSimpleName());
        stringBuilder.append('{');
        TrackInfo trackInfo = this.mTrackInfo;
        stringBuilder.append(trackInfo != null ? trackInfo.toString() : "null");
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
