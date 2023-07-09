package com.cocos.game;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.LinesXFree.cocos.BuildConfig;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.qhhz.cocos.libandroid.PermissionTipDialog;
import com.qhhz.cocos.libandroid.SPStorage;
import com.xiaomi.ad.mediation.MMAdConfig;
import com.xiaomi.ad.mediation.MMAdError;
import com.xiaomi.ad.mediation.rewardvideoad.MMAdReward;
import com.xiaomi.ad.mediation.rewardvideoad.MMAdRewardVideo;
import com.xiaomi.ad.mediation.rewardvideoad.MMRewardVideoAd;


/**
 * Copyright (C) 2013, Xiaomi Inc. All rights reserved.
 */

public class RewardVideoAd {
    private static String TAG = "RewardVideoAd";
    private String AD_TAG_ID = "";
    private MutableLiveData<MMRewardVideoAd> mAd = new MutableLiveData();
    private MutableLiveData<MMAdError> mAdError = new MutableLiveData();
    private Activity mActivity;
    private MMAdRewardVideo mAdRewardVideo;
    private boolean autoPlay;

    public RewardVideoAd(String posId) {
        AD_TAG_ID = posId;
        mActivity = AppActivity.get();
        mAdRewardVideo = new MMAdRewardVideo(App.get(), AD_TAG_ID);
        autoPlay = true;
        mAdRewardVideo.onCreate();
    }

    public void Dispose() {
        mAdError = null;
        if(mAd != null && mAd.getValue() != null) {
            mAd.getValue().destroy();
            mAd = null;
        }
        mActivity = null;
        mAdRewardVideo = null;
        statu = -1;
    }

    public MutableLiveData<MMRewardVideoAd> getAd() {
        return mAd;
    }

    public MutableLiveData<MMAdError> getAdError() {
        return mAdError;
    }


    /**
     * 0 未加载
     * 1 加载中
     * 2 加载完毕
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

    private MMAdRewardVideo.RewardVideoAdListener mRewardVideoAdListener = new MMAdRewardVideo.RewardVideoAdListener() {
        @Override
        public void onRewardVideoAdLoaded(MMRewardVideoAd ad) {
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
            }
        }

        @Override
        public void onRewardVideoAdLoadError(MMAdError error) {
            if(statu == -1) return;
            Log.e(TAG, "rwdLoad error " + error.errorMessage + " " + error.errorCode);
            statu = 0;
            mAdError.setValue(error);
        }
    };

    //    public void  requestAd(boolean isHorizontal) {
    public void requestAd(String uid) {
        if(statu == -1) return;
        Log.d(TAG, "reqAd. id " + uid);
        if (statu != 0) {
            Log.d(TAG, "reqAdCancel statu is " + statu);
            return;
        }
        MMAdConfig adConfig = new MMAdConfig();
        adConfig.supportDeeplink = true;
        adConfig.imageHeight = BuildConfig.DESIGN_HEIGHT;
        adConfig.imageWidth = BuildConfig.DESIGN_WIDTH;
        //期望广告view的size,单位dp（*必填）
        adConfig.viewWidth = BuildConfig.DESIGN_WIDTH;
        adConfig.viewHeight = BuildConfig.DESIGN_HEIGHT;
        adConfig.rewardCount = 5;
        adConfig.rewardName = "金币";
        adConfig.userId = uid;
        adConfig.setRewardVideoActivity(mActivity);
        mAdRewardVideo.load(adConfig, mRewardVideoAdListener);
        statu = 1;
    }


    public void playAd() {
        if(!AdKit.get().checkAdPermission()) {
            JSBKit.get().ShowAdRet("-1");
            return;
        }
        if (statu != 2) {
            requestAd(AdKit.get().getUserId());
//            JSBKit.get().ShowAdRet("-1");
            return;
        }
        MMRewardVideoAd ad = mAd.getValue();
        ad.setInteractionListener(
                new MMRewardVideoAd.RewardVideoAdInteractionListener() {
                    @Override
                    public void onAdShown(MMRewardVideoAd mmRewardVideoAd) {
                    }

                    @Override
                    public void onAdClicked(MMRewardVideoAd mmRewardVideoAd) {
                    }

                    @Override
                    public void onAdError(MMRewardVideoAd mmRewardVideoAd, MMAdError error) {
                        JSBKit.get().ShowAdRet("0");
                    }

                    @Override
                    public void onAdVideoComplete(MMRewardVideoAd mmRewardVideoAd) {
                    }

                    @Override
                    public void onAdClosed(MMRewardVideoAd mmRewardVideoAd) {
                        if(statu == -1) return;
                        statu = 0;
                        AdKit.get().loadRwdAd(AD_TAG_ID);
                    }

                    @Override
                    public void onAdReward(MMRewardVideoAd mmRewardVideoAd, MMAdReward mmAdReward) {
                        JSBKit.get().ShowAdRet("1");
                    }

                    @Override
                    public void onAdVideoSkipped(MMRewardVideoAd mmRewardVideoAd) {
                        JSBKit.get().ShowAdRet("0");
                    }
                });
        ad.showAd(mActivity);
    }

    public Context requireContext() {
        return mActivity;
    }
}
