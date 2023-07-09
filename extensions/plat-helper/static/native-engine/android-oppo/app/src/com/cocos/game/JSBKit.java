package com.cocos.game;

import android.util.Log;

import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.nearme.game.sdk.callback.GameExitCallback;
import com.nearme.game.sdk.common.model.ApiResult;
import com.nearme.game.sdk.common.util.AppUtil;
import com.qhhz.cocos.libandroid.JSBKitBase;
import com.qhhz.cocos.libandroid.Runkit;
import com.qhhz.cocos.libandroid.SplashDialog;

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

    private boolean mem_EverCheckPlatReady = false;

    @Override
    protected void OnAntiAddiction(String uid) {
        Log.d(TAG, "AntiAddiction");
        DoLogin();
    }

    @Override
    protected void OnShowAd(String arg) {
        Log.d(TAG, "ShowAd");
        AdKit.getMe().playVideo(arg);
    }

    @Override
    protected void OnEndGame(String arg) {
        Log.d(TAG, "EndGame");
        Runkit.get().ExitGame();
    }

//    /***
//     * 原生代码初始化完毕后通知游戏
//     */
//    public void CheckPlatReadyComplete() {
//        Log.d(TAG, "OnCheckPlatReady");
//        if(mem_EverCheckPlatReady) {
//            CheckPlatReadyRet("Suc");
//            mem_EverCheckPlatReady = false;
//        }
//    }

    @Override
    protected void OnCheckPlatReady(String arg) {
        Log.d(TAG, "CheckPlatReady");
        AppActivity.get().CheckReady(()->{
            CheckPlatReadyRet("suc");
        });
//        mem_EverCheckPlatReady = true;
//        if(mem_act.IsAllReady()) {
//            CheckPlatReadyComplete();
//        } else {
//            mem_act.checkAndRequestPermissions();
//        }
    }

    @Override
    protected void OnEnterGameCenter(String arg) {
        Log.d(TAG, "EnterGameCenter");
        GameCenterSDK.getInstance().jumpLeisureSubject();
    }

    @Override
    protected void OnShowBannerAd(String arg) {
        Log.d(TAG, "OnShowBannerAd " + arg);
        try {
            JSONObject data = new JSONObject(arg);
            AdKit.getMe().playBanner(data.getString("posId"), data.getString("pos"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        DoNothings At Default
    }

    @Override
    protected  void OnHideBannerAd(String arg) {
        Log.d(TAG, "OnHideBannerAd " + arg);
        AdKit.getMe().hideBanner();
    }

    @Override
    protected void OnShowInterstitialAd(String arg) {
        Log.d(TAG, "OnShowInterstitialAd " + arg);
        AdKit.getMe().playInterstitialAd(arg);
    }

    @Override
    public void CheckPlatReadyRet(String arg) {
        dispatch("CheckPlatReadyRet", "Debug");
    }


    public  void DoLogin() {
        AppActivity mem_act = AppActivity.get();
        JSBKit self = this;
        // 因为在部分机型横屏下登录会闪退，因此需要把登录放在一个竖屏界面中。
        // 在 Login Activity 中已登录过了
        GameCenterSDK.getInstance().doGetVerifiedInfo(new ApiCallback() {
            @Override
            public void onSuccess(String resultMsg) {
                try {
                    //解析年龄（age）
                    int age = Integer.parseInt(resultMsg);
                    if (age < 18) {
                        //已实名但未成年，CP开始处理防沉迷
                    } else {
                        //已实名且已成年，尽情玩游戏吧
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "AntiAddiction Suc");
                self.AntiAddictionRet();
            }
            @Override
            public void onFailure(String resultMsg, int resultCode) {
                if(resultCode == ApiResult.RESULT_CODE_VERIFIED_FAILED_AND_RESUME_GAME){
                    //实名认证失败，但还可以继续玩游戏
                }else if(resultCode == ApiResult.RESULT_CODE_VERIFIED_FAILED_AND_STOP_GAME){
                    //实名认证失败，不允许继续游戏，CP需自己处理退出游戏
                    AppUtil.exitGameProcess(mem_act);
                }
                Log.d(TAG, "AntiAddiction Fail " + resultCode);
            }
        });
    }
}
