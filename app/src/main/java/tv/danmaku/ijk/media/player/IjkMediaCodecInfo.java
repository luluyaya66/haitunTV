package tv.danmaku.ijk.media.player;

import android.media.MediaCodecInfo;
import android.os.Build;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class IjkMediaCodecInfo {
    public static final int RANK_ACCEPTABLE = 700;
    public static final int RANK_LAST_CHANCE = 600;
    public static final int RANK_MAX = 1000;
    public static final int RANK_NON_STANDARD = 100;
    public static final int RANK_NO_SENSE = 0;
    public static final int RANK_SECURE = 300;
    public static final int RANK_SOFTWARE = 200;
    public static final int RANK_TESTED = 800;
    public static final String TAG = "IjkMediaCodecInfo";
    public static Map<String, Integer> sKnownCodecList;
    public MediaCodecInfo mCodecInfo;
    public String mMimeType;
    public int mRank = 0;

    public static synchronized Map<String, Integer> getKnownCodecList() {
        synchronized (IjkMediaCodecInfo.class) {
            if (sKnownCodecList != null) {
                Map<String, Integer> map = sKnownCodecList;
                return map;
            }
            Map treeMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
            sKnownCodecList = treeMap;
            treeMap.put("OMX.Nvidia.h264.decode", Integer.valueOf(800));
            sKnownCodecList.put("OMX.Nvidia.h264.decode.secure", Integer.valueOf(RANK_SECURE));
            sKnownCodecList.put("OMX.Intel.hw_vd.h264", Integer.valueOf(IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE));
            sKnownCodecList.put("OMX.Intel.VideoDecoder.AVC", Integer.valueOf(800));
            sKnownCodecList.put("OMX.qcom.video.decoder.avc", Integer.valueOf(800));
            sKnownCodecList.put("OMX.ittiam.video.decoder.avc", Integer.valueOf(0));
            sKnownCodecList.put("OMX.SEC.avc.dec", Integer.valueOf(800));
            sKnownCodecList.put("OMX.SEC.AVC.Decoder", Integer.valueOf(799));
            sKnownCodecList.put("OMX.SEC.avcdec", Integer.valueOf(798));
            sKnownCodecList.put("OMX.SEC.avc.sw.dec", Integer.valueOf(200));
            sKnownCodecList.put("OMX.Exynos.avc.dec", Integer.valueOf(800));
            sKnownCodecList.put("OMX.Exynos.AVC.Decoder", Integer.valueOf(799));
            sKnownCodecList.put("OMX.k3.video.decoder.avc", Integer.valueOf(800));
            sKnownCodecList.put("OMX.IMG.MSVDX.Decoder.AVC", Integer.valueOf(800));
            sKnownCodecList.put("OMX.TI.DUCATI1.VIDEO.DECODER", Integer.valueOf(800));
            sKnownCodecList.put("OMX.rk.video_decoder.avc", Integer.valueOf(800));
            sKnownCodecList.put("OMX.amlogic.avc.decoder.awesome", Integer.valueOf(800));
            sKnownCodecList.put("OMX.MARVELL.VIDEO.HW.CODA7542DECODER", Integer.valueOf(800));
            sKnownCodecList.put("OMX.MARVELL.VIDEO.H264DECODER", Integer.valueOf(200));
            sKnownCodecList.remove("OMX.Action.Video.Decoder");
            sKnownCodecList.remove("OMX.allwinner.video.decoder.avc");
            sKnownCodecList.remove("OMX.BRCM.vc4.decoder.avc");
            sKnownCodecList.remove("OMX.brcm.video.h264.hw.decoder");
            sKnownCodecList.remove("OMX.brcm.video.h264.decoder");
            sKnownCodecList.remove("OMX.cosmo.video.decoder.avc");
            sKnownCodecList.remove("OMX.duos.h264.decoder");
            sKnownCodecList.remove("OMX.hantro.81x0.video.decoder");
            sKnownCodecList.remove("OMX.hantro.G1.video.decoder");
            sKnownCodecList.remove("OMX.hisi.video.decoder");
            sKnownCodecList.remove("OMX.LG.decoder.video.avc");
            sKnownCodecList.remove("OMX.MS.AVC.Decoder");
            sKnownCodecList.remove("OMX.RENESAS.VIDEO.DECODER.H264");
            sKnownCodecList.remove("OMX.RTK.video.decoder");
            sKnownCodecList.remove("OMX.sprd.h264.decoder");
            sKnownCodecList.remove("OMX.ST.VFM.H264Dec");
            sKnownCodecList.remove("OMX.vpu.video_decoder.avc");
            sKnownCodecList.remove("OMX.WMT.decoder.avc");
            sKnownCodecList.remove("OMX.bluestacks.hw.decoder");
            sKnownCodecList.put("OMX.google.h264.decoder", Integer.valueOf(200));
            sKnownCodecList.put("OMX.google.h264.lc.decoder", Integer.valueOf(200));
            sKnownCodecList.put("OMX.k3.ffmpeg.decoder", Integer.valueOf(200));
            sKnownCodecList.put("OMX.ffmpeg.video.decoder", Integer.valueOf(200));
            sKnownCodecList.put("OMX.sprd.soft.h264.decoder", Integer.valueOf(200));
            Map<String, Integer> map = sKnownCodecList;
            return map;
        }
    }

    public static String getLevelName(int i) {
        if (i == 1) {
            return "1";
        }
        if (i == 2) {
            return "1b";
        }
        switch (i) {
            case 4:
                return "11";
            case 8:
                return "12";
            case 16:
                return "13";
            case 32:
                return "2";
            case 64:
                return "21";
            case 128:
                return "22";
            case 256:
                return "3";
            case IjkMediaMeta.FF_PROFILE_H264_CONSTRAINED /*512*/:
                return "31";
            case 1024:
                return "32";
            case IjkMediaMeta.FF_PROFILE_H264_INTRA /*2048*/:
                return "4";
            case 4096:
                return "41";
            case 8192:
                return "42";
            case 16384:
                return "5";
            case 32768:
                return "51";
            case 65536:
                return "52";
            default:
                return "0";
        }
    }

    public static String getProfileLevelName(int i, int i2) {
        return String.format(Locale.US, " %s Profile Level %s (%d,%d)", new Object[]{getProfileName(i), getLevelName(i2), Integer.valueOf(i), Integer.valueOf(i2)});
    }

    public static String getProfileName(int i) {
        return i != 1 ? i != 2 ? i != 4 ? i != 8 ? i != 16 ? i != 32 ? i != 64 ? "Unknown" : "High444" : "High422" : "High10" : "High" : "Extends" : "Main" : "Baseline";
    }

    @android.annotation.TargetApi(16)
    public static IjkMediaCodecInfo setupCandidate(android.media.MediaCodecInfo codecInfo, String mimeType) {
        if (codecInfo == null) {
            return null;
        }

        String name = codecInfo.getName();
        if (TextUtils.isEmpty(name)) {
            return null;
        }

        String codecName = name.toLowerCase(Locale.US);
        int rank = 0;

        // Skip software decoders unless no hardware decoder found
        if (!codecName.startsWith("omx.")) {
            rank = RANK_NON_STANDARD;
        } else if (codecName.startsWith("omx.pv")) {
            // PV OMX decoders are buggy
            rank = RANK_SOFTWARE;
        } else if (codecName.startsWith("omx.google.")) {
            // Google OMX decoders offer better quality but poor performance
            rank = RANK_SOFTWARE;
        } else if (codecName.startsWith("omx.ffmpeg.")) {
            // FFmpeg OMX decoders are not well tested
            rank = RANK_SOFTWARE;
        } else if (codecName.startsWith("omx.k3.ffmpeg.")) {
            rank = RANK_SOFTWARE;
        } else if (codecName.startsWith("omx.avcodec.")) {
            rank = RANK_SOFTWARE;
        } else if (codecName.startsWith("omx.ittiam.")) {
            // Ittiam OMX decoders are not reliable
            rank = RANK_NO_SENSE;
        } else if (codecName.startsWith("omx.mtk.")) {
            // MTK OMX decoders are not well tested
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                rank = RANK_NO_SENSE;
            } else {
                rank = RANK_TESTED;
            }
        } else {
            // Other hardware decoders
            Integer knownRank = getKnownCodecList().get(codecName);
            if (knownRank != null) {
                rank = knownRank;
            } else {
                // Check capabilities for unknown codecs
                try {
                    MediaCodecInfo.CodecCapabilities cap = codecInfo.getCapabilitiesForType(mimeType);
                    if (cap != null) {
                        rank = RANK_ACCEPTABLE;
                    }
                } catch (Throwable e) {
                    rank = RANK_LAST_CHANCE;
                }
            }
        }

        IjkMediaCodecInfo candidate = new IjkMediaCodecInfo();
        candidate.mCodecInfo = codecInfo;
        candidate.mRank = rank;
        candidate.mMimeType = mimeType;
        return candidate;
    }

    @android.annotation.TargetApi(16)
    public void dumpProfileLevels(java.lang.String r9) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:tv.danmaku.ijk.media.player.IjkMediaCodecInfo.dumpProfileLevels(java.lang.String):void. bs: []
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:60)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
*/
        /*
        r8 = this;
        r0 = "IjkMediaCodecInfo";
        r1 = r8.mCodecInfo;	 Catch:{ all -> 0x0042 }
        r9 = r1.getCapabilitiesForType(r9);	 Catch:{ all -> 0x0042 }
        r1 = 0;	 Catch:{ all -> 0x0042 }
        if (r9 == 0) goto L_0x002b;	 Catch:{ all -> 0x0042 }
    L_0x000b:
        r2 = r9.profileLevels;	 Catch:{ all -> 0x0042 }
        if (r2 == 0) goto L_0x002b;	 Catch:{ all -> 0x0042 }
    L_0x000f:
        r9 = r9.profileLevels;	 Catch:{ all -> 0x0042 }
        r2 = r9.length;	 Catch:{ all -> 0x0042 }
        r3 = 0;	 Catch:{ all -> 0x0042 }
        r4 = 0;	 Catch:{ all -> 0x0042 }
        r5 = 0;	 Catch:{ all -> 0x0042 }
    L_0x0015:
        if (r3 >= r2) goto L_0x002d;	 Catch:{ all -> 0x0042 }
    L_0x0017:
        r6 = r9[r3];	 Catch:{ all -> 0x0042 }
        if (r6 != 0) goto L_0x001c;	 Catch:{ all -> 0x0042 }
    L_0x001b:
        goto L_0x0028;	 Catch:{ all -> 0x0042 }
    L_0x001c:
        r7 = r6.profile;	 Catch:{ all -> 0x0042 }
        r4 = java.lang.Math.max(r4, r7);	 Catch:{ all -> 0x0042 }
        r6 = r6.level;	 Catch:{ all -> 0x0042 }
        r5 = java.lang.Math.max(r5, r6);	 Catch:{ all -> 0x0042 }
    L_0x0028:
        r3 = r3 + 1;	 Catch:{ all -> 0x0042 }
        goto L_0x0015;	 Catch:{ all -> 0x0042 }
    L_0x002b:
        r4 = 0;	 Catch:{ all -> 0x0042 }
        r5 = 0;	 Catch:{ all -> 0x0042 }
    L_0x002d:
        r9 = java.util.Locale.US;	 Catch:{ all -> 0x0042 }
        r2 = "%s";	 Catch:{ all -> 0x0042 }
        r3 = 1;	 Catch:{ all -> 0x0042 }
        r3 = new java.lang.Object[r3];	 Catch:{ all -> 0x0042 }
        r4 = getProfileLevelName(r4, r5);	 Catch:{ all -> 0x0042 }
        r3[r1] = r4;	 Catch:{ all -> 0x0042 }
        r9 = java.lang.String.format(r9, r2, r3);	 Catch:{ all -> 0x0042 }
        android.util.Log.i(r0, r9);	 Catch:{ all -> 0x0042 }
        goto L_0x0047;
    L_0x0042:
        r9 = "profile-level: exception";
        android.util.Log.i(r0, r9);
    L_0x0047:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: tv.danmaku.ijk.media.player.IjkMediaCodecInfo.dumpProfileLevels(java.lang.String):void");
    }
}
