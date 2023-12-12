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

# Add this global rule
-keepattributes Signature
-keepattributes Exceptions
# This rule will properly ProGuard all the model classes in
# the package com.yourcompany.models.
# Modify this rule to fit the structure of your app.
-keepclassmembers class info.free.scp.** {
  *;
}

-keep class com.google.firebase.database.** {
  *;
}
-keepclassmembers class com.google.firebase.database.GenericTypeIndicator { *; }
-keep class * extends com.google.firebase.database.GenericTypeIndicator { *; }

-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

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

# ==================retrofit2 start===================
# Retain service method parameters.
-keepclassmembernames,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep class retrofit.** { *; }
# ==================retrofit2 end=====================

# okhttp
-dontwarn okio.**
-dontwarn com.squareup.okhttp3.**
-dontwarn org.conscrypt.*
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-dontwarn javax.annotation.**

# Gson
-keep class info.free.scp.bean.**{*;} # 自定义数据模型的bean目录
# ==================gson start=====================
-dontwarn com.google.gson.**
-keep class com.google.gson.**{*;}
-keep interface com.google.gson.**{*;}
-dontwarn sun.misc.**
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


#ad
-keep class android.support.v4.*{ *;}
-ignorewarnings

-keep class okhttp3.** { *; }
-keep class okio.** { *; }


-keep class com.mock.sdk.main.** { *; }

# GooglePlayService
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames class * implements android.os.Parcelable
-keepclassmembers class * implements android.os.Parcelable {
  public static final *** CREATOR;
}


-keep @interface com.google.android.gms.common.annotation.KeepName
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
  @com.google.android.gms.common.annotation.KeepName *;
}

-keep @interface com.google.android.gms.common.util.DynamiteApi

-dontwarn android.security.NetworkSecurityPolicy
# Vungle
-keep class com.vungle.warren.** { *; }

-keep class com.google.android.gms.internal.** { *; }


# okdownload:okhttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keep public @com.google.android.gms.common.util.DynamiteApi class * {
  public <fields>;
  public <methods>;
}
