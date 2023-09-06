package com.cocos.game;

import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.LinesXFree.cocos.BuildConfig;
import com.xiaomi.ad.mediation.MMAdConfig;
import com.xiaomi.ad.mediation.MMAdError;
import com.xiaomi.ad.mediation.fullscreeninterstitial.MMAdFullScreenInterstitial;
import com.xiaomi.ad.mediation.fullscreeninterstitial.MMFullScreenInterstitialAd;

public class InsertAd {
    private static String TAG = "InsertAd";
    private String AD_TAG_ID = "";
    private MutableLiveData<MMFullScreenInterstitialAd> mAd = new MutableLiveData();
    private MutableLiveData<MMAdError> mAdError = new MutableLiveData();
    private MMAdFullScreenInterstitial mInterstitialAd;
    private boolean autoPlay;
//
    private Activity mActivity;

    public InsertAd(String posId) {
        AD_TAG_ID = posId;
        mActivity = AppActivity.get();
        mInterstitialAd = new MMAdFullScreenInterstitial(App.get(), AD_TAG_ID);
        autoPlay = true;
        mInterstitialAd.onCreate();
    }

    public void Dispose() {
        mAdError = null;
        if(mAd != null && mAd.getValue() != null) {
            mAd.getValue().onDestroy();
            mAd = null;
        }
        mActivity = null;
        mInterstitialAd = null;
        statu = -1;
    }

    public MutableLiveData<MMFullScreenInterstitialAd> getAd() {
        return mAd;
    }

    public MutableLiveData<MMAdError> getAdError() {
        return mAdError;
    }


    /**
     * 0 未加载
     * 1 加载中
     * 2 加载完毕
     * 3 展示中
     * -1 已关闭
     */
    private int statu = 0;

    /**
     * 0 未加载
     * 1 加载中
     * 2 加载完毕
     * -1 已关闭
     */
    public int getStatu() {
        return statu;
    }

    private MMAdFullScreenInterstitial.FullScreenInterstitialAdListener mFullScreenInterstitialAdListener = new MMAdFullScreenInterstitial.FullScreenInterstitialAdListener() {
        @Override
        public void onFullScreenInterstitialAdLoaded(
                MMFullScreenInterstitialAd ad) {
            if(statu == -1) return;
            if (ad != null) {
                Log.d(TAG, "rwdLoaded suc");
                statu = 2;
                mAd.setValue(ad);
                if(autoPlay) {
                    autoPlay = false;
                    playAd();
                }
            } else {
                Log.d(TAG, "rwdLoaded no ad");
                statu = 0;
                mAdError.setValue(new MMAdError(MMAdError.LOAD_NO_AD));
                JSBKit.get().InsertAdRet("-2");
            }
        }

        @Override
        public void onFullScreenInterstitialAdLoadError(MMAdError error) {
            if(statu == -1) return;
            Log.e(TAG, "rwdLoad error " + error.errorMessage + " " + error.errorCode);
            statu = 0;
            mAdError.setValue(error);
            JSBKit.get().InsertAdRet(error.externalErrorCode.equals("301007") ? "-2" : "0");
        }
    };

    public void requestAd() {
        if (statu != 0) {
            Log.d(TAG, "reqAdCancel statu is " + statu);
            return;
        }
        MMAdConfig adConfig = new MMAdConfig();
        adConfig.supportDeeplink = true;
        adConfig.imageWidth = BuildConfig.DESIGN_WIDTH;
        adConfig.imageHeight = BuildConfig.DESIGN_HEIGHT;
        //期望广告view的size,单位dp（*必填）
        adConfig.viewWidth = BuildConfig.DESIGN_WIDTH;
        adConfig.viewHeight = BuildConfig.DESIGN_HEIGHT;

        if (AppActivity.get().isPortrait()) {
            adConfig.interstitialOrientation  = MMAdConfig.Orientation.ORIENTATION_HORIZONTAL;
        } else {
            adConfig.interstitialOrientation  = MMAdConfig.Orientation.ORIENTATION_VERTICAL;
        }
        adConfig.setInsertActivity(mActivity);
        mInterstitialAd.load(adConfig, mFullScreenInterstitialAdListener);
        statu = 1;
    }

    public void playAd() {
        if (statu == 3) {
            JSBKit.get().InsertAdRet("0");
            return;
        }
        if(!AdKit.get().checkAdPermission()) {
            JSBKit.get().InsertAdRet("0");
            return;
        }
        if (statu != 2) {
            requestAd();
            return;
        }
        statu = 3;
        boolean isBannerShowing = BannerAd.checkIsShowing();
        MMFullScreenInterstitialAd ad = mAd.getValue();
        ad.setInteractionListener(
                new MMFullScreenInterstitialAd.FullScreenInterstitialAdInteractionListener() {
                    @Override
                    public void onAdShown(MMFullScreenInterstitialAd mmFullScreenInterstitialAd) {
                        mAd.setValue(null);
                        if(isBannerShowing) {
                            BannerAd.hidedlg();
                        }
                        Log.d(TAG, "onAdShown");
                    }

                    @Override
                    public void onAdClicked(MMFullScreenInterstitialAd mmFullScreenInterstitialAd) {
                        Log.d(TAG, "onAdClicked");
                    }

                    @Override
                    public void onAdVideoComplete(MMFullScreenInterstitialAd mmFullScreenInterstitialAd) {
                        Log.d(TAG, "onAdVideoComplete");
                    }

                    @Override
                    public void onAdClosed(MMFullScreenInterstitialAd mmFullScreenInterstitialAd) {
                        Log.d(TAG, "onAdClosed");
                        if(statu == -1) return;
                        statu = 0;
                        if(isBannerShowing) {
                            BannerAd.showdlg();
                        }
                        mActivity.runOnUiThread(() -> {
                            requestAd();
                        });
                        JSBKit.get().InsertAdRet("1");
                    }

                    @Override
                    public void onAdVideoSkipped(MMFullScreenInterstitialAd mmFullScreenInterstitialAd) {
                        Log.d(TAG, "onAdVideoSkipped");
                    }

                    @Override
                    public void onAdRenderFail(MMFullScreenInterstitialAd mmFullScreenInterstitialAd, int i, String s) {
                        Log.e(TAG, "onAdRenderFail " + i + " " + s);
                        statu = 0;
                        if(isBannerShowing) {
                            BannerAd.showdlg();
                        }
                        JSBKit.get().InsertAdRet("0");
                    }
        });
        mActivity.runOnUiThread(() -> {
            ad.showAd(mActivity);
        });
    }
}
