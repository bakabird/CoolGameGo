package com.cocos.game;

import android.util.Log;

import com.heytap.msp.mobad.api.ad.InterstitialAd;
import com.heytap.msp.mobad.api.listener.IInterstitialAdListener;

public class MyInterstitialAd implements IInterstitialAdListener {
    private static final String TAG = "MyInterstitialAd";

    private static MyInterstitialAd _instance;

    public static MyInterstitialAd get() {
        if(_instance == null) {
            _instance = new MyInterstitialAd();
        }
        return _instance;
    }

    private InterstitialAd mInterstitialAd;
    // 0 起始
    // 1 加载中
    // 2 播放中
    private int state;
    private boolean everCloseBanenr = false;

    private MyInterstitialAd() {
        this.state = 0;
    }

    public void play(String posId) {
        if(this.state != 0) return;
        /**
         * 构造 InterstitialAd.
         */
        mInterstitialAd = new InterstitialAd(AppActivity.get(), posId);
        /**
         * 设置插屏广告行为监听器.
         */
        mInterstitialAd.setAdListener(this);
        /**
         * 调用 loadAd() 方法请求广告.
         */
        state = 1;
        mInterstitialAd.loadAd();
    }

    @Override
    public void onAdClose() {
        Log.d(TAG, "onAdClose");
        this.state = 0;
        if(everCloseBanenr && !MyBannerAd.checkIsShowing()) {
            MyBannerAd.showdlg();
        }

    }

    @Override
    public void onAdReady() {
        Log.d(TAG, "onAdReady");
        mInterstitialAd.showAd();
        if (MyBannerAd.checkIsShowing()) {
            everCloseBanenr = true;
            MyBannerAd.hidedlg();
        } else  {
            everCloseBanenr = false;
        }
    }

    @Override
    public void onAdClick() {
        Log.d(TAG, "onAdClick");
    }

    @Override
    public void onAdFailed(int i, String s) {
        Log.d(TAG, "onAdFailed:code=" + i + ", msg:" + s);
        mInterstitialAd.destroyAd();
        this.state = 0;
        if(everCloseBanenr && !MyBannerAd.checkIsShowing()) {
            MyBannerAd.showdlg();
        }
    }

    @Override
    public void onAdFailed(String s) {
        /**
         *请求广告失败，已废弃，请使用onAdFailed(int i, String s)
         */
        // Deprecated , do nothing
    }

    @Override
    public void onAdShow() {
        Log.d(TAG, "onAdShow");
        this.state = 1;
    }
}
