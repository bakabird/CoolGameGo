package com.qhhz.cocos.libandroid;

import android.util.Log;
#IFUM
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
#ENDUM
public abstract class JSBKitBase {
    public static final String TAG = "JSBKitbase";

    protected IJSBWrapper mem_jbw;

    public JSBKitBase() {

    }

    public void build() {
        mem_jbw = Runkit.get().jbw();
        listen("AntiAddiction", uid -> {
            Log.d(TAG, "AntiAddiction");
            this.OnAntiAddiction(uid);
        });
        listen("EndGame", arg -> {
            Log.d(TAG, "EndGame");
            this.OnEndGame(arg);
        });
        listen("ShowAd", arg -> {
            Log.d(TAG, "ShowAd");
            this.OnShowAd(arg);
        });
        listen("EnterGameCenter", arg -> {
            Log.d(TAG, "EnterGameCenter");
            this.OnEnterGameCenter(arg);
        });
        listen("CheckPlatReady", arg -> {
            Log.d(TAG, "CheckPlatReady");
            this.OnCheckPlatReady(arg);
        });
        listen("ShowBannerAd", arg -> {
            Log.d(TAG, "ShowBannerAd");
            this.OnShowBannerAd(arg);
        });
        listen("HideBannerAd", arg -> {
            Log.d(TAG, "HideBannerAd");
            this.OnHideBannerAd(arg);
        });
        listen("ShowTemplateAd", arg -> {
            Log.d(TAG, "ShowTemplateAd");
            this.OnShowTemplateAd(arg);
        });
        listen("HideTemplateAd", arg -> {
            Log.d(TAG, "HideTemplateAd");
            this.OnHideTemplateAd(arg);
        });
        listen("ShowInterstitialAd", arg -> {
            Log.d(TAG, "ShowInterstitialAd");
            this.OnShowInterstitialAd(arg);
        });
        listen("Track", arg -> {
            Log.d(TAG, "Track");
            this.OnTrack(arg);
        });
    }

    /*
     * Listener BEGIN ---
     */

    protected void OnEndGame(String arg) {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    protected void OnTrack(String arg) {
#IFUM
        try {
            JSONObject data = new JSONObject(arg);
            Map<String, Object> post = new HashMap<String, Object>();
            String eventID = data.getString("eventID");
            JSONObject obj = data.getJSONObject("params");
            Iterator<String> keys = obj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                post.put(key, obj.get(key));
            }
            MobclickAgent.onEventObject(Runkit.get().app(), eventID, post);
        } catch (JSONException e) {
            e.printStackTrace();
        }
#ENDUM
    }
    protected void OnEnterGameCenter(String arg) {
//        DoNothings At Default
    }

    protected abstract void OnAntiAddiction(String uid);

    protected abstract void OnShowAd(String arg);

    protected abstract void OnCheckPlatReady(String arg);

    protected void OnShowBannerAd(String arg) {
//        DoNothings At Default
    }

    protected void OnHideBannerAd(String arg) {
//        DoNothings At Default
    }

    protected void OnShowTemplateAd(String arg) {
//        DoNothings At Default
    }

    protected void OnHideTemplateAd(String arg) {
//        DoNothings At Default
    }

    protected void OnShowInterstitialAd(String arg) {
//        DoNothings At Default
    }

    /*
     * Listener End ---
     */

    /*
     * Dispatcher BEGIN ---
     */

    /**
     * @param code 0失败 1成功 -1拉取中 -101(SDK未初始化) -102(SDK初始化中)
     */
    public void ShowAdRet(String code) {
        Log.d(TAG, "ShowAdRet " + code);
        dispatch("ShowAdRet", code);
    }

    /**
     * @param code 0失败 1已关闭 -2没广告
     */
    public void TemplateAdRet(String code) {
        Log.d(TAG, "TemplateAdRet " + code);
        dispatch("TemplateAdRet", code);
    }

    /**
     * @param code 0失败 1已关闭 -2没广告
     */
    public void InsertAdRet(String code) {
        Log.d(TAG, "InsertAdRet " + code);
        dispatch("InsertAdRet", code);
    }

    /**
     * 调用即表示能继续玩。
     */
    public void AntiAddictionRet() {
        Log.d(TAG, "AntiAddictionRet");
        dispatch("AntiAddictionRet");
    }

    /***
     * 原生代码 0.隐私确认完毕 1.SDK初始化完毕 后通知游戏
     * @return Debug | Release
     */
    public abstract void CheckPlatReadyRet(String arg);

    /*
     * Dispatcher END ---
     */


    protected void dispatch(String event) {
        mem_jbw.dispatchEventToScript(event);
    }

    protected void dispatch(String event, String arg) {
        mem_jbw.dispatchEventToScript(event, arg);
    }

    protected void listen(String event, OnScriptEventListener listener) {
        mem_jbw.addScriptEventListener(event, listener);
    }
}
