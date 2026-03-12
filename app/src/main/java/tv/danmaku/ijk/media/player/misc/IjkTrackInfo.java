package tv.danmaku.ijk.media.player.misc;

import android.text.TextUtils;
import tv.danmaku.ijk.media.player.IjkMediaMeta.IjkStreamMeta;

public class IjkTrackInfo implements ITrackInfo {
    public IjkStreamMeta mStreamMeta;
    public int mTrackType = 0;

    public IjkTrackInfo(IjkStreamMeta ijkStreamMeta) {
        this.mStreamMeta = ijkStreamMeta;
    }

    public IMediaFormat getFormat() {
        return new IjkMediaFormat(this.mStreamMeta);
    }

    public String getInfoInline() {
        String resolutionInline;
        StringBuilder stringBuilder = new StringBuilder(128);
        int i = this.mTrackType;
        String str = ", ";
        if (i == 1) {
            stringBuilder.append("VIDEO");
            stringBuilder.append(str);
            stringBuilder.append(this.mStreamMeta.getCodecShortNameInline());
            stringBuilder.append(str);
            stringBuilder.append(this.mStreamMeta.getBitrateInline());
            stringBuilder.append(str);
            resolutionInline = this.mStreamMeta.getResolutionInline();
        } else if (i == 2) {
            stringBuilder.append("AUDIO");
            stringBuilder.append(str);
            stringBuilder.append(this.mStreamMeta.getCodecShortNameInline());
            stringBuilder.append(str);
            stringBuilder.append(this.mStreamMeta.getBitrateInline());
            stringBuilder.append(str);
            resolutionInline = this.mStreamMeta.getSampleRateInline();
        } else if (i != 3) {
            resolutionInline = i != 4 ? "UNKNOWN" : "SUBTITLE";
        } else {
            stringBuilder.append("TIMEDTEXT");
            stringBuilder.append(str);
            resolutionInline = this.mStreamMeta.mLanguage;
        }
        stringBuilder.append(resolutionInline);
        return stringBuilder.toString();
    }

    public String getLanguage() {
        IjkStreamMeta ijkStreamMeta = this.mStreamMeta;
        if (ijkStreamMeta != null) {
            if (!TextUtils.isEmpty(ijkStreamMeta.mLanguage)) {
                return this.mStreamMeta.mLanguage;
            }
        }
        return "und";
    }

    public int getTrackType() {
        return this.mTrackType;
    }

    public void setMediaMeta(IjkStreamMeta ijkStreamMeta) {
        this.mStreamMeta = ijkStreamMeta;
    }

    public void setTrackType(int i) {
        this.mTrackType = i;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(IjkTrackInfo.class.getSimpleName());
        stringBuilder.append('{');
        stringBuilder.append(getInfoInline());
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
