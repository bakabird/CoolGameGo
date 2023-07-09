package com.qhhz.cocos.libandroid;


import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
#IFUM
import android.content.res.Resources;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
#ENDUM

/**
 * 启动套件
 */
public class Runkit {
    private static Runkit _me;

    public static Runkit get() {
        if (_me == null) {
            _me = new Runkit();
        }
        return _me;
    }

    private IRunkitAction $;

    private Runkit() {

    }

    public void Init(IRunkitAction runkitAction) {
        $ = runkitAction;
#IFUM
        Application app = this.app();
        UMConfigure.setLogEnabled(true);
        UMConfigure.preInit(app, app.getResources().getString(R.string.um_appkey), app.getResources().getString(R.string.CHANNEL));
#ENDUM
    }

    public Application app() {
        return $.getApp();
    }

    public Activity act() {
        return $.getAppActivity();
    }

    public void onConfirmProtocol() {
        $.onConfirmProtocol();
    }

    public IJSBWrapper jbw() {
        return $.getJSBWrapper();
    }


    /**
     * 退出游戏时务必使用该接口
     */
    public void ExitGame() {
        $.ExitGame(()->{
#IFUM
            MobclickAgent.onKillProcess($.getApp());
#ENDUM
        });
    }


    public String spKey() {
        return $.getSharedPreferencesKey();
    }

    public boolean isAgreeProtocol() {
        return SPStorage.get().getStr(SPStorage.ProtocolAgreementKey, "").equals("agree");
    }

    public void userAgreeProtocol() {
        SPStorage.get().setStr(SPStorage.ProtocolAgreementKey, "agree");
    }

    public static void FullScreen(Window window) {
        //屏幕适配核心 在AppActivity的onCreate添加  让画布扩充到刘海部分
        if (Build.VERSION.SDK_INT >= 28) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(lp);
        }
    }
}
