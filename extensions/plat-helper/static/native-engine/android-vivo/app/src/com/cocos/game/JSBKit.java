package com.cocos.game;

import android.util.Log;

import com.qhhz.cocos.libandroid.JSBKitBase;
import com.qhhz.cocos.libandroid.Runkit;
import com.vivo.unionsdk.open.VivoAccountCallback;
import com.vivo.unionsdk.open.VivoUnionSDK;

import org.json.JSONException;
import org.json.JSONObject;

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
        AppActivity act = AppActivity.get();
        if (!mem_AntiAddiction) {
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
        VivoUnionSDK.login(act);
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
        if (mem_PlatReady) {
            CheckPlatReadyRet("");
            return;
        }
        AppActivity act = AppActivity.get();
        act.runOnUiThread(() -> {
            App.get().initSDK(() -> {
                VivoUnionSDK.onPrivacyAgreed(act);
                mem_PlatReady = true;
                CheckPlatReadyRet("");
            });
        });

    }

    @Override
    public void CheckPlatReadyRet(String arg) {
        dispatch("CheckPlatReadyRet", "Debug");
    }
}
