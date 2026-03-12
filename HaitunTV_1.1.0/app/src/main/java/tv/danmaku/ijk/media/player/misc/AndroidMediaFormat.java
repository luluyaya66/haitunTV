package tv.danmaku.ijk.media.player.misc;

import android.annotation.TargetApi;
import android.media.MediaFormat;

public class AndroidMediaFormat implements IMediaFormat {
    public final MediaFormat mMediaFormat;

    public AndroidMediaFormat(MediaFormat mediaFormat) {
        this.mMediaFormat = mediaFormat;
    }

    @TargetApi(16)
    public int getInteger(String str) {
        MediaFormat mediaFormat = this.mMediaFormat;
        return mediaFormat == null ? 0 : mediaFormat.getInteger(str);
    }

    @TargetApi(16)
    public String getString(String str) {
        MediaFormat mediaFormat = this.mMediaFormat;
        return mediaFormat == null ? null : mediaFormat.getString(str);
    }

    @TargetApi(16)
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(128);
        stringBuilder.append(AndroidMediaFormat.class.getName());
        stringBuilder.append('{');
        MediaFormat mediaFormat = this.mMediaFormat;
        stringBuilder.append(mediaFormat != null ? mediaFormat.toString() : "null");
        stringBuilder.append('}');
        return stringBuilder.toString();
    }
}
