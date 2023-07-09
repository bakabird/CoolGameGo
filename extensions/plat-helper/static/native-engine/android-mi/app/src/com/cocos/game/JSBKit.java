package com.cocos.game;

import android.util.Log;

import com.qhhz.cocos.libandroid.JSBKitBase;
import com.qhhz.cocos.libandroid.Runkit;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnExitListner;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;

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

    @Override
    protected void OnAntiAddiction(String uid) {
        MiCommplatform.getInstance().miLogin(AppActivity.get(),
                new OnLoginProcessListener() {
                    @Override
                    public void finishLoginProcess(int code, MiAccountInfo arg1) {
                        Log.d(TAG, "Milogin finish: " + code);
                        switch (code) {
                            case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS: // 登陆成功
                                //获取用户的登陆后的UID（即用户唯一标识）
                                AdKit.get().setUserId(arg1.getUid());
                                //以下为获取session并校验流程，如果是网络游戏必须校验，如果是单机游戏或应用可选//
                                //获取用户的登陆的Session（请参考3.3 用户session验证接口）
                                //请开发者完成将uid和session提交给开发者自己服务器进行session验证
                                AntiAddictionRet();
                                break;
                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                                // 登陆失败
                                break;
                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                                // 取消登录
                                break;
                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                                //登录操作正在进行中
                                break;
                            default:
                                // 登录失败
                                break;
                        }
                    }
                });
    }

    @Override
    protected void OnEndGame(String arg) {
        Runkit.get().ExitGame();
    }

    @Override
    protected void OnShowAd(String arg) {
        AdKit.get().playRwdAd(arg);
    }

    @Override
    protected void OnShowInterstitialAd(String arg) {
        AdKit.get().playInsertAd(arg);
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
        AppActivity.get().checkReady(() -> {
            CheckPlatReadyRet("");
        });
    }

    @Override
    public void CheckPlatReadyRet(String arg) {
        dispatch("CheckPlatReadyRet", "Debug");
    }
}
