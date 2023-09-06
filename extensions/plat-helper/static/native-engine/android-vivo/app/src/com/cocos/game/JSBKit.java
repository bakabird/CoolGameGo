package com.cocos.game;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;

import com.LinesXFree.cocos.BuildConfig;
import com.qhhz.cocos.libandroid.JSBKitBase;
import com.qhhz.cocos.libandroid.Runkit;
import com.qhhz.cocos.libandroid.SplashDialog;
import com.vivo.unionsdk.open.VivoAccountCallback;
import com.vivo.unionsdk.open.VivoUnionSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class JSBKit extends JSBKitBase {
    private static JSBKit _me;

    public static JSBKit get() {
        if (_me == null) {
            _me = new JSBKit();
        }
        return _me;
    }

    private boolean mem_EverAntiAddiction = false;
    private boolean mem_PlatReady = false;
    private boolean mem_AntiAddiction = false;

    @Override
    protected void OnAntiAddiction(String uid) {
        if (!mem_AntiAddiction) {
            AppActivity act = AppActivity.get();
            VivoUnionSDK.registerAccountCallback(act, new VivoAccountCallback() {
                @Override
                public void onVivoAccountLogin(String username, String openid, String authToken) {
                    Log.d(TAG, "Vivo login suc");
                    //登录成功，openid参数为用户唯一标识
                    act.setM_usernamel(username);
                    act.setM_openid(openid);
                    act.setM_authToken(authToken);
                    if (mem_EverAntiAddiction) {
                        AntiAddictionRet();
                        mem_EverAntiAddiction = false;
                    }
                }

                @Override
                public void onVivoAccountLogout(int i) {
                    //登录退出
                    Log.d(TAG, "Vivo logout");
                }

                @Override
                public void onVivoAccountLoginCancel() {
                    //登录取消
                    Log.d(TAG, "Vivo login cancel");
                }
            });
            mem_AntiAddiction = true;
        }
        mem_EverAntiAddiction = true;

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                AppActivity.doRunOnUiThread(() -> {
                    VivoUnionSDK.login(AppActivity.get());
                });
            }
        };
        //1000ms执行一次
        timer.schedule(task, 666);
    }

    @Override
    protected void OnShowAd(String arg) {
        AdKit.get().playRwdAd(arg);
    }

    protected void OnShowInterstitialAd(String arg) {
        AdKit.get().playInsertAd(arg);
    }
    @Override
    protected void OnEndGame(String arg) {
        Runkit.get().ExitGame();
    }

    protected void OnShowBannerAd(String arg) {
        Log.d(TAG, "OnShowBannerAd " + arg);
        try {
            JSONObject data = new JSONObject(arg);
            AdKit.get().playBanner(data.getString("posId"), data.getString("pos"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void OnHideBannerAd(String arg) {
        Log.d(TAG, "OnHideBannerAd " + arg);
        AdKit.get().hideBanner();
    }

    @Override
    protected void OnCheckPlatReady(String arg) {
        Log.d(TAG, "OnCheckPlatReady " + arg);
        if (mem_PlatReady) {
            CheckPlatReadyRet("");
            return;
        }
        AppActivity act = AppActivity.get();
        act.runOnUiThread(() -> {
            App.get().initSDK(() -> {
                VivoUnionSDK.onPrivacyAgreed(act);
                SplashDialog.Close();
                mem_PlatReady = true;
                CheckPlatReadyRet("");
            });
        });
    }

    @Override
    protected void OnShowTemplateAd(String arg) {
        Log.d(TAG, "OnShowTemplateAd " + arg);
        try {
            JSONObject data = new JSONObject(arg);
            JSONObject miArg = data.getJSONObject("andMiArg");
            String posId = data.getString("posId");
            int gravity = Gravity.NO_GRAVITY;
            boolean force =miArg.getBoolean("force");
            boolean debug = miArg.getBoolean("debug");
            int argGravity = miArg.getInt("gravity");
            String widthMode = miArg.getString("widthMode");
            int widthDp = miArg.getInt("widthDp");
            int scale = miArg.getInt("scale");
            String binaryArgGravity = Integer.toBinaryString(argGravity);
            Log.d(TAG, binaryArgGravity);
            if (binaryArgGravity.charAt(5) == '1') gravity |= Gravity.TOP;
            if (binaryArgGravity.charAt(4) == '1') gravity |= Gravity.BOTTOM;
            if (binaryArgGravity.charAt(3) == '1') gravity |= Gravity.LEFT;
            if (binaryArgGravity.charAt(2) == '1') gravity |= Gravity.RIGHT;
            if (binaryArgGravity.charAt(1) == '1') gravity |= Gravity.CENTER_VERTICAL;
            if (binaryArgGravity.charAt(0) == '1') gravity |= Gravity.CENTER_HORIZONTAL;
            Log.d(TAG, posId + " " + debug + " " + gravity + " " + widthMode + " " + widthDp + " " + scale);
            AdKit.get().playTemplate(posId, force, gravity, debug , widthMode, widthDp, scale);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void OnHideTemplateAd(String arg) {
        Log.d(TAG, "OnHideTemplateAd " + arg);
        AdKit.get().hideTemplate();
    }

    @Override
    public void CheckPlatReadyRet(String arg) {
        dispatch("CheckPlatReadyRet", BuildConfig.BUILD_TYPE.equals("release") ? "Release" : "Debug");
    }
}
