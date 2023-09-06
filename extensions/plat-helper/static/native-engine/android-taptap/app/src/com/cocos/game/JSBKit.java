package com.cocos.game;


import android.util.Log;
import android.view.Gravity;

import com.qhhz.LinesXFree.taptap.BuildConfig;
import com.qhhz.LinesXFree.taptap.R;
import com.qhhz.cocos.libandroid.JSBKitBase;
import com.qhhz.cocos.libandroid.Runkit;
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

    protected void OnShowTemplateAd(String arg) {
        Log.d(TAG, "OnShowTemplateAd " + arg);
        try {
            JSONObject data = new JSONObject(arg);
            String posId = data.getString("posId");
            int gravity = Gravity.NO_GRAVITY;
            boolean force = data.getBoolean("force");
            boolean debug = data.getBoolean("debug");
            int argGravity = data.getInt("gravity");
            String widthMode = data.getString("widthMode");
            int widthDp = data.getInt("widthDp");
            String binaryArgGravity = Integer.toBinaryString(argGravity);
            Log.d(TAG, binaryArgGravity);
            if (binaryArgGravity.charAt(5) == '1') gravity |= Gravity.TOP;
            if (binaryArgGravity.charAt(4) == '1') gravity |= Gravity.BOTTOM;
            if (binaryArgGravity.charAt(3) == '1') gravity |= Gravity.LEFT;
            if (binaryArgGravity.charAt(2) == '1') gravity |= Gravity.RIGHT;
            if (binaryArgGravity.charAt(1) == '1') gravity |= Gravity.CENTER_VERTICAL;
            if (binaryArgGravity.charAt(0) == '1') gravity |= Gravity.CENTER_HORIZONTAL;
            Log.d(TAG, posId + " " + debug + " " + gravity + " " + widthMode + " " + widthDp);
            ADKit.get().playTemplate(posId, force, gravity, debug , widthMode, widthDp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void OnHideTemplateAd(String arg) {
        Log.d(TAG, "OnHideTemplateAd " + arg);
        ADKit.get().hideTemplate();
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
            dispatch("CheckPlatReadyRet", BuildConfig.DEBUG ? "Debug" : "Release");
            mem_EverCheckPlatReady = false;
        }
    }

    @Override
    protected void OnEndGame(String arg) {
        Runkit.get().ExitGame();
    }
}
