# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class info.free.scp.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class info.free.scp.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class info.free.scp.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}
-keep public class info.free.scp.R$*{
public static final int *;
}

# Retrofit
-dontnote retrofit2.Platform
-dontnote retrofit2.Platform$IOS$MainThreadExecutor
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions

# okhttp
-dontwarn okio.**
-dontwarn com.squareup.okhttp3.**
-dontwarn org.conscrypt.*
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-dontwarn javax.annotation.**

# Gson
-keep class info.free.scp.bean.**{*;} # 自定义数据模型的bean目录

#umeng
-keep class com.umeng.** {*;}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


#GDT
-keep class com.qq.e.** {
    public protected *;
}
-keep class android.support.v4.**{
    public *;
}
-keep class android.support.v7.**{
    public *;
}

#ad
-keep class android.support.v4.*{ *;}
-ignorewarnings

-keepattributes Signature
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# TGSDK
-keep class com.soulgame.sgsdk.** { *; }

-keep class com.mock.sdk.main.** { *; }

# GooglePlayService
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final *** CREATOR;
}

-keep @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
  @android.support.annotation.Keep <methods>;
}

-keep @interface com.google.android.gms.common.annotation.KeepName
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
  @com.google.android.gms.common.annotation.KeepName *;
}

-keep @interface com.google.android.gms.common.util.DynamiteApi
-keep public @com.google.android.gms.common.util.DynamiteApi class * {
  public <fields>;
  public <methods>;
}

-dontwarn android.security.NetworkSecurityPolicy



    # Shenqiad
-keep class com.shenqi.video.** {*;}

# sigmob
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep public class * extends android.support.v4.**
-keep class sun.misc.Unsafe { *; }
-dontwarn com.sigmob.**
    -keep class com.sigmob.**.**{*;}

# Bugly
-dontwarn com.tencent.bugly.tgsdk.**
-keep class com.tencent.bugly.tgsdk.** { *; }
# ADX
-dontwarn com.yomob.tgsdklib.**
-keep class com.yomob.tgsdklib.** { *; }
-keep interface com.yomob.tgsdklib.** { *; }

# Guangdiantong
-keep class com.qq.e.** {
    public protected *;
}
-keep class android.support.v4.**{
    public *;
}
-keep class android.support.v7.**{
public *;
}
-keep class MTT.ThirdAppInfoNew {
    *;
}
-keep class com.tencent.** {
    *;
}

# Baidu
 -keepclassmembers class * extends android.app.Activity { public void *(android.view.View);
}
-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}
-keep class com.baidu.mobads.*.** { *; }

# Mintegral
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.mintegral.** {*; }
-keep interface com.mintegral.** {*; }
-keep class android.support.v4.** { *; }
-dontwarn com.mintegral.**
-keep class **.R$* { public static final int mintegral*; }
-keep class com.alphab.** {*; }
-keep interface com.alphab.** {*; }

# Uniplay
-keep class com.uniplay.adsdk.**
-keep class com.joomob.**
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepattributes Annotation
-keepattributes JavascriptInterface
-keepclassmembers class * {
@android.webkit.JavascriptInterface <methods>;
}
-keepclassmembers public class com.uniplay.adsdk.JavaScriptInterface{
<fields>;
<methods>;
public *;
private *;
}

# Vungle
-keep class com.vungle.warren.** { *; }

# Evernote
-dontwarn com.evernote.android.job.gcm.**
-dontwarn com.evernote.android.job.GcmAvailableHelper

-dontwarn com.google.android.gms.ads.identifier.**

-keep public class com.evernote.android.job.v21.PlatformJobService
-keep public class com.evernote.android.job.v14.PlatformAlarmService
-keep public class com.evernote.android.job.v14.PlatformAlarmReceiver
-keep public class com.evernote.android.job.JobBootReceiver
-keep public class com.evernote.android.job.JobRescheduleService

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-keep class com.google.android.gms.internal.** { *; }

# Moat SDK
-keep class com.moat.** { *; }
-dontwarn com.moat.**

#Yoads
-keep class **.R$* { *; }
-keep class com.yomob.yomobads.** { *; }

# ByteDance
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep class com.androidquery.callback.** { *; }

# OneWay
-keepattributes *Annotation*
-keep enum mobi.oneway.sdk.* {*;}
-keep class mobi.oneway.sdk.** {*;}
-dontwarn com.liulishuo.okdownload.**
-keep class com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite {
    public com.liulishuo.okdownload.core.breakpoint.DownloadStore createRemitSelf();
    public com.liulishuo.okdownload.core.breakpoint.BreakpointStoreOnSQLite(android.content.Context);
}
# okdownload:okhttp
-keepnames class com.liulishuo.okdownload.core.connection.DownloadOk
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
