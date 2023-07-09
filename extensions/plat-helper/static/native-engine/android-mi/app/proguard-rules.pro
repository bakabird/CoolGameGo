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


# 小米 START
#小米SDK
-keep class com.xiaomi.** {*;}
-keep class com.wali.** {*;}
-keep class cn.com.wali.** {*;}
-keep class com.miui.**{*;}

#支付宝SDK
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-keep class com.alipay.sdk.app.H5PayCallback {
    <fields>;
    <methods>;
}
-keep class com.alipay.android.phone.mrpc.core.** { *; }
-keep class com.alipay.apmobilesecuritysdk.** { *; }
-keep class com.alipay.mobile.framework.service.annotation.** { *; }
-keep class com.alipay.mobilesecuritysdk.face.** { *; }
-keep class com.alipay.tscenter.biz.rpc.** { *; }
-keep class org.json.alipay.** { *; }
-keep class com.alipay.tscenter.** { *; }
-keep class com.ta.utdid2.** { *;}
-keep class com.ut.device.** { *;}

-dontwarn com.ta.utdid2.**
-dontwarn com.ut.device.**

-dontwarn com.alipay.mobilesecuritysdk.**
-dontwarn com.alipay.security.**

-dontwarn android.net.SSLCertificateSocketFactory
# 小米 END


# MIMO START
# Analytics SDK
-keep class com.miui.analytics.** { *; }
-keep class com.xiaomi.analytics.* { public protected *; }
# Bytedance SDK
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep class com.bytedance.frameworks.** { *; }
-keep class ms.bd.c.Pgl.**{*;}
-keep class com.bytedance.mobsec.metasec.ml.**{*;}
-keep class com.ss.android.**{*;}
-keep class com.bytedance.embedapplog.** {*;}
-keep class com.bytedance.embed_dr.** {*;}
-keep class com.bykv.vk.** {*;}
#jingdong
-keep class com.jd.ad.sdk.** { *; }
# Mediation SDK
-keep class com.xiaomi.ad.mediation.** { *; }
#tencent sdk
-keep class com.qq.e.** {public*;}
#Ad SDK
-keep class com.miui.zeus.mimo.sdk.** { *; }
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class * extends android.os.IInterface{
   *;
}
# MIMO END