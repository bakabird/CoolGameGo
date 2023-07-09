package com.cocos.game;


import android.util.Log;

import com.qhhz.LinesXFree.taptap.R;
import com.qhhz.cocos.libandroid.JSBKitBase;
import com.qhhz.cocos.libandroid.SplashDialog;
import com.tapsdk.antiaddictionui.AntiAddictionUIKit;

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

    private boolean mem_EverCheckPlatReady = false;

    @Override
    protected void OnAntiAddiction(String uid) {
        AntiAddictionUIKit.startup(AppActivity.get(), uid);
    }

    @Override
    protected void OnShowAd(String arg) {
        ADKit.get().playRwdAd(arg);
    }

    protected void OnShowInterstitialAd(String arg) {
        ADKit.get().playInsertAd(arg);
    }

    protected void OnShowBannerAd(String arg) {
        Log.d(TAG, "OnShowBannerAd " + arg);
        try {
            JSONObject data = new JSONObject(arg);
            ADKit.get().playBanner(data.getString("posId"), data.getString("pos"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void OnHideBannerAd(String arg) {
        Log.d(TAG, "OnHideBannerAd " + arg);
        ADKit.get().hideBanner();
    }

    @Override
    protected void OnCheckPlatReady(String arg) {
        mem_EverCheckPlatReady = true;
        if (AppActivity.get().IsAllReady()) {
            CheckPlatReadyRet("");
        } else {
            AppActivity.get().checkPermissionAndInit(false);
        }
    }

    @Override
    public void CheckPlatReadyRet(String arg) {
        Log.d(TAG, "CheckPlatReadyRet");
        SplashDialog.Close();
        if (mem_EverCheckPlatReady) {
            dispatch("CheckPlatReadyRet", "Debug");
            mem_EverCheckPlatReady = false;
        }
    }

}
