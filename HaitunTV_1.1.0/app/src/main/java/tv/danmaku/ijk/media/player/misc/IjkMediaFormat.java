package tv.danmaku.ijk.media.player.misc;

import android.annotation.TargetApi;
import android.text.TextUtils;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import tv.danmaku.ijk.media.player.IjkMediaCodecInfo;
import tv.danmaku.ijk.media.player.IjkMediaMeta;
import tv.danmaku.ijk.media.player.IjkMediaMeta.IjkStreamMeta;

public class IjkMediaFormat implements IMediaFormat {
    public static final String CODEC_NAME_H264 = "h264";
    public static final String KEY_IJK_BIT_RATE_UI = "ijk-bit-rate-ui";
    public static final String KEY_IJK_CHANNEL_UI = "ijk-channel-ui";
    public static final String KEY_IJK_CODEC_LONG_NAME_UI = "ijk-codec-long-name-ui";
    public static final String KEY_IJK_CODEC_NAME_UI = "ijk-codec-name-ui";
    public static final String KEY_IJK_CODEC_PIXEL_FORMAT_UI = "ijk-pixel-format-ui";
    public static final String KEY_IJK_CODEC_PROFILE_LEVEL_UI = "ijk-profile-level-ui";
    public static final String KEY_IJK_FRAME_RATE_UI = "ijk-frame-rate-ui";
    public static final String KEY_IJK_RESOLUTION_UI = "ijk-resolution-ui";
    public static final String KEY_IJK_SAMPLE_RATE_UI = "ijk-sample-rate-ui";
    public static final Map<String, j> sFormatterMap = new HashMap();
    public final IjkStreamMeta mMediaFormat;

    public static abstract class j {
        public /* synthetic */ j(a aVar) {
        }

        public abstract String a(IjkMediaFormat ijkMediaFormat);
    }

    public class h extends j {
        public h(IjkMediaFormat ijkMediaFormat) {
            super(null);
        }

        public String a(IjkMediaFormat ijkMediaFormat) {
            if (ijkMediaFormat.getInteger(IjkMediaMeta.IJKM_KEY_SAMPLE_RATE) <= 0) {
                return null;
            }
            return String.format(Locale.US, "%d Hz", new Object[]{Integer.valueOf(ijkMediaFormat.getInteger(IjkMediaMeta.IJKM_KEY_SAMPLE_RATE))});
        }
    }

    public class g extends j {
        public g(IjkMediaFormat ijkMediaFormat) {
            super(null);
        }

        public String a(IjkMediaFormat ijkMediaFormat) {
            int integer = ijkMediaFormat.getInteger(IjkMediaMeta.IJKM_KEY_FPS_NUM);
            int integer2 = ijkMediaFormat.getInteger(IjkMediaMeta.IJKM_KEY_FPS_DEN);
            if (integer > 0) {
                if (integer2 > 0) {
                    return String.valueOf(((float) integer) / ((float) integer2));
                }
            }
            return null;
        }
    }

    public class f extends j {
        public f(IjkMediaFormat ijkMediaFormat) {
            super(null);
        }

        public String a(IjkMediaFormat ijkMediaFormat) {
            int integer = ijkMediaFormat.getInteger("width");
            int integer2 = ijkMediaFormat.getInteger("height");
            int integer3 = ijkMediaFormat.getInteger(IjkMediaMeta.IJKM_KEY_SAR_NUM);
            int integer4 = ijkMediaFormat.getInteger(IjkMediaMeta.IJKM_KEY_SAR_DEN);
            if (integer > 0) {
                if (integer2 > 0) {
                    if (integer3 > 0) {
                        if (integer4 > 0) {
                            return String.format(Locale.US, "%d x %d [SAR %d:%d]", new Object[]{Integer.valueOf(integer), Integer.valueOf(integer2), Integer.valueOf(integer3), Integer.valueOf(integer4)});
                        }
                    }
                    return String.format(Locale.US, "%d x %d", new Object[]{Integer.valueOf(integer), Integer.valueOf(integer2)});
                }
            }
            return null;
        }
    }

    public class e extends j {
        public e(IjkMediaFormat ijkMediaFormat) {
            super(null);
        }

        public String a(IjkMediaFormat ijkMediaFormat) {
            return ijkMediaFormat.getString(IjkMediaMeta.IJKM_KEY_CODEC_PIXEL_FORMAT);
        }
    }

    public class d extends j {
        public d(IjkMediaFormat ijkMediaFormat) {
            super(null);
        }

        public String a(IjkMediaFormat ijkMediaFormat) {
            String str;
            switch (ijkMediaFormat.getInteger(IjkMediaMeta.IJKM_KEY_CODEC_PROFILE_ID)) {
                case 44:
                    str = "CAVLC 4:4:4";
                    break;
                case 66:
                    str = "Baseline";
                    break;
                case 77:
                    str = "Main";
                    break;
                case 88:
                    str = "Extended";
                    break;
                case 100:
                    str = "High";
                    break;
                case 110:
                    str = "High 10";
                    break;
                case 122:
                    str = "High 4:2:2";
                    break;
                case IjkMediaMeta.FF_PROFILE_H264_HIGH_444 /*144*/:
                    str = "High 4:4:4";
                    break;
                case IjkMediaMeta.FF_PROFILE_H264_HIGH_444_PREDICTIVE /*244*/:
                    str = "High 4:4:4 Predictive";
                    break;
                case IjkMediaMeta.FF_PROFILE_H264_CONSTRAINED_BASELINE /*578*/:
                    str = "Constrained Baseline";
                    break;
                case IjkMediaMeta.FF_PROFILE_H264_HIGH_10_INTRA /*2158*/:
                    str = "High 10 Intra";
                    break;
                case IjkMediaMeta.FF_PROFILE_H264_HIGH_422_INTRA /*2170*/:
                    str = "High 4:2:2 Intra";
                    break;
                case IjkMediaMeta.FF_PROFILE_H264_HIGH_444_INTRA /*2292*/:
                    str = "High 4:4:4 Intra";
                    break;
                default:
                    return null;
            }
            StringBuilder a = new StringBuilder(str);
            Object string = ijkMediaFormat.getString(IjkMediaMeta.IJKM_KEY_CODEC_NAME);
            if (!TextUtils.isEmpty((CharSequence) string) && string.toString().equalsIgnoreCase(IjkMediaFormat.CODEC_NAME_H264)) {
                int integer = ijkMediaFormat.getInteger(IjkMediaMeta.IJKM_KEY_CODEC_LEVEL);
                if (integer < 10) {
                    return a.toString();
                }
                a.append(" Profile Level ");
                a.append((integer / 10) % 10);
                integer %= 10;
                if (integer != 0) {
                    a.append(".");
                    a.append(integer);
                }
            }
            return a.toString();
        }
    }

    public class c extends j {
        public c(IjkMediaFormat ijkMediaFormat) {
            super(null);
        }

        public String a(IjkMediaFormat ijkMediaFormat) {
            int integer = ijkMediaFormat.getInteger(IjkMediaMeta.IJKM_KEY_BITRATE);
            if (integer <= 0) {
                return null;
            }
            if (integer < IjkMediaCodecInfo.RANK_MAX) {
                return String.format(Locale.US, "%d bit/s", new Object[]{Integer.valueOf(integer)});
            }
            return String.format(Locale.US, "%d kb/s", new Object[]{Integer.valueOf(integer / IjkMediaCodecInfo.RANK_MAX)});
        }
    }

    public class b extends j {
        public final /* synthetic */ IjkMediaFormat a;

        public b(IjkMediaFormat ijkMediaFormat) {
            super(null);
            this.a = ijkMediaFormat;
        }

        public String a(IjkMediaFormat ijkMediaFormat) {
            return this.a.mMediaFormat.getString(IjkMediaMeta.IJKM_KEY_CODEC_NAME);
        }
    }

    public class a extends j {
        public final /* synthetic */ IjkMediaFormat a;

        public a(IjkMediaFormat ijkMediaFormat) {
            super(null);
            this.a = ijkMediaFormat;
        }

        public String a(IjkMediaFormat ijkMediaFormat) {
            return this.a.mMediaFormat.getString(IjkMediaMeta.IJKM_KEY_CODEC_LONG_NAME);
        }
    }

    public class i extends j {
        public i(IjkMediaFormat ijkMediaFormat) {
            super(null);
        }

        public String a(IjkMediaFormat ijkMediaFormat) {
            int integer = ijkMediaFormat.getInteger(IjkMediaMeta.IJKM_KEY_CHANNEL_LAYOUT);
            if (integer <= 0) {
                return null;
            }
            long j = (long) integer;
            if (j == 4) {
                return "mono";
            }
            if (j == 3) {
                return "stereo";
            }
            return String.format(Locale.US, "%x", new Object[]{Integer.valueOf(integer)});
        }
    }

    public IjkMediaFormat(IjkStreamMeta ijkStreamMeta) {
        sFormatterMap.put(KEY_IJK_CODEC_LONG_NAME_UI, new a(this));
        sFormatterMap.put(KEY_IJK_CODEC_NAME_UI, new b(this));
        sFormatterMap.put(KEY_IJK_BIT_RATE_UI, new c(this));
        sFormatterMap.put(KEY_IJK_CODEC_PROFILE_LEVEL_UI, new d(this));
        sFormatterMap.put(KEY_IJK_CODEC_PIXEL_FORMAT_UI, new e(this));
        sFormatterMap.put(KEY_IJK_RESOLUTION_UI, new f(this));
        sFormatterMap.put(KEY_IJK_FRAME_RATE_UI, new g(this));
        sFormatterMap.put(KEY_IJK_SAMPLE_RATE_UI, new h(this));
        sFormatterMap.put(KEY_IJK_CHANNEL_UI, new i(this));
        this.mMediaFormat = ijkStreamMeta;
    }

    @TargetApi(16)
    public int getInteger(String str) {
        IjkStreamMeta ijkStreamMeta = this.mMediaFormat;
        return ijkStreamMeta == null ? 0 : ijkStreamMeta.getInt(str);
    }

    public String getString(String str) {
        if (this.mMediaFormat == null) {
            return null;
        }
        if (!sFormatterMap.containsKey(str)) {
            return this.mMediaFormat.getString(str);
        }
        str = ((j) sFormatterMap.get(str)).a(this);
        if (TextUtils.isEmpty(str)) {
            str = "N/A";
        }
        return str;
    }
}
