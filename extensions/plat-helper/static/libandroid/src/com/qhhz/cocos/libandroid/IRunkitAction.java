package com.qhhz.cocos.libandroid;

import android.app.Activity;
import android.app.Application;

public interface IRunkitAction {
    /**
     * 获取 主Application
     *
     * @return
     */
    Application getApp();

    /**
     * 获取 主Activity
     *
     * @return
     */
    Activity getAppActivity();


    /**
     * 获取 CocosCreator JSBWrapper 实例
     *
     * @return
     */
    IJSBWrapper getJSBWrapper();

    /**
     * 获取所使用的 SharedPrefernce 的仓库键时调用.
     *
     * @return
     */
    String getSharedPreferencesKey();

    /**
     * 在确认完协议后调用
     */
    void onConfirmProtocol();

    /**
     * 退出游戏
     */
    void ExitGame(Runnable onExit);
}