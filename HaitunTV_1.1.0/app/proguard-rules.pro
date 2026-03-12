
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# 保持行号信息便于调试
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 保持注解
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# ===============================
# Android 组件
# ===============================
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.Application
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.fragment.app.FragmentActivity

# 保持自定义View的构造函数
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ===============================
# AndroidX 库（更精细的规则）
# ===============================
-keep class androidx.appcompat.** { *; }
-dontwarn androidx.appcompat.**

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

-keep class androidx.constraintlayout.** { *; }
-dontwarn androidx.constraintlayout.**

-keep class androidx.recyclerview.** { *; }
-dontwarn androidx.recyclerview.**

-keep class androidx.leanback.** { *; }
-dontwarn androidx.leanback.**

-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

-keep class androidx.preference.** { *; }
-dontwarn androidx.preference.**

# ===============================
# 媒体播放器库
# ===============================
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**
-keep class com.google.android.exoplayer.** { *; }

-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**

# ===============================
# 网络和图片库
# ===============================
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class okio.** { *; }
-dontwarn okio.**

-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# ===============================
# 应用自定义类（更精细的规则）
# ===============================
# 保持所有Activity、Controller、ViewModel等关键类
-keep public class * extends com.tv.mydiy.ui.MainActivity
-keep class com.tv.mydiy.ui.controller.** { *; }
-keep class com.tv.mydiy.ui.viewmodel.** { *; }
-keep class com.tv.mydiy.data.** { *; }
-keep class com.tv.mydiy.util.** { *; }
-keep class com.peasun.aispeech.** { *; }

# ===============================
# 序列化和Parcelable
# ===============================
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ===============================
# 其他配置
# ===============================
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
-dontwarn kotlin.**

# 优化配置
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,code/removal/advanced
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively

# ===============================
# Release 版本移除日志
# ===============================
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

-assumenosideeffects class com.tv.mydiy.util.LogUtil {
    public static void v(...);
    public static void d(...);
    public static void i(...);
    public static void w(...);
    public static void e(...);
}

