# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\developSoftware\Android\SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Proguard Cocos2d-x-lite for release
-keep public class com.cocos.** { *; }
-dontwarn com.cocos.**

# Proguard Apache HTTP for release
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

# Proguard okhttp for release
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

-keep class okio.** { *; }
-dontwarn okio.**

# Proguard Android Webivew for release. you can comment if you are not using a webview
-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient

-keep public class com.google.** { *; }

-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient

#VIVO START
-keep class com.bun.miitmdid.core.** {*;}

-keep class XI.CA.XI.**{*;}

-keep class XI.K0.XI.**{*;}

-keep class XI.XI.K0.**{*;}

-keep class XI.xo.XI.XI.**{*;}

-keep class com.asus.msa.SupplementaryDID.**{*;}

-keep class com.asus.msa.sdid.**{*;}

-keep class com.bun.lib.**{*;}

-keep class com.bun.miitmdid.**{*;}

-keep class com.huawei.hms.ads.identifier.**{*;}

-keep class com.samsung.android.deviceidservice.**{*;}

-keep class com.zui.opendeviceidlibrary.**{*;}

-keep class org.json.**{*;}

-keep public class com.netease.nis.sdkwrapper.Utils {public <methods>;}
#VIVO END


#VIVO AD START
-keepattributes SourceFile,LineNumberTable
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-keep class com.vivo.*.** { *; }
-dontwarn com.bytedance.article.common.nativecrash.NativeCrashInit
-keep class com.bytedance.sdk.openadsdk.** {*;}
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.pgl.sys.ces.* {*;}
-keep class com.qq.e.** {
 public protected *;
}
-keep class android.support.v4.**{
 public *;
}
-keep class android.support.v7.widget.** {*;}
-dontwarn com.vivo.secboxsdk.**
-keep class com.vivo.secboxsdk.SecBoxCipherException { *; }
-keep class com.vivo.secboxsdk.jni.SecBoxNative { *; }
-keep class com.vivo.secboxsdk.BuildConfig { *; }
-keep class com.vivo.advv.**{*;}
-keep class com.kwad.sdk.** { *;}
-keep class com.ksad.download.** { *;}
-keep class com.kwai.filedownloader.** { *;}
# sdk
-keep class com.bun.miitmdid.** { *; }
-keep interface com.bun.supplier.** { *; }
# asus
-keep class com.asus.msa.SupplementaryDID.** { *; }
-keep class com.asus.msa.sdid.** { *; }
# freeme
-keep class com.android.creator.** { *; }
-keep class com.android.msasdk.** { *; }
# huawei
-keep class com.huawei.hms.ads.** { *; }
-keep interface com.huawei.hms.ads.** {*; }
# lenovo
-keep class com.zui.deviceidservice.** { *; }
-keep class com.zui.opendeviceidlibrary.** { *; }
# meizu
-keep class com.meizu.flyme.openidsdk.** { *; }
# nubia
-keep class com.bun.miitmdid.provider.nubia.NubiaIdentityImpl { *; }
# oppo
-keep class com.heytap.openid.** { *; }
# samsung
-keep class com.samsung.android.deviceidservice.** { *; }
# vivo
-keep class com.vivo.identifier.** { *; }
# xiaomi
-keep class com.bun.miitmdid.provider.xiaomi.IdentifierManager { *; }
# zte
-keep class com.bun.lib.** { *; }
# coolpad
-keep class com.coolpad.deviceidsupport.** { *; }
# ---------掌酷 SDK--------------
-keep class com.wrapper.ZkViewSDK {
 public <fields>;
 public <methods>;
}
-keep class com.wrapper.ZkViewSDK$ActionCallBack {
 public <fields>;
 public <methods>;
}
# 保留枚举类不被混淆
-keepclassmembers enum * {
 public static **[] values();
 public static ** valueOf(java.lang.String);
}
-keep class com.wrapper.ZkViewSDK$KEY {
 public <fields>;
 public <methods>;
}
-keep class com.wrapper.ZkViewSDK$Event {
 public <fields>;
 public <methods>;
}
-keeppackagenames com.zk.**
# ---------掌酷 SDK--------------
#VIVO AD END