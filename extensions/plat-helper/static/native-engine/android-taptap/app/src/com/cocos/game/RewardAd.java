package com.cocos.game;

import static com.bytedance.sdk.openadsdk.TTAdLoadType.PRELOAD;

import android.os.Bundle;
import android.util.Log;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.qhhz.LinesXFree.taptap.R;

public class RewardAd {
    private final String TAG = "RewardAd";

    private boolean isLoadingRwd = false;
    private String posId;
    private ADKit _adKit;
    private boolean autoPlay;
    private boolean disposed;

    public TTRewardVideoAd mttRewardVideoAd;
    public AppActivity appAct;

    public RewardAd(String posId, ADKit adKit) {
        appAct = AppActivity.get();
        _adKit = adKit;
        this.posId = posId;
        autoPlay = true;
        disposed = false;
    }

    public void Dispose() {
        appAct = null;
        _adKit = null;
        mttRewardVideoAd = null;
        disposed = true;
    }

    public void loadRewardAD() {
        if(disposed) return;
        if (mttRewardVideoAd != null) return;
        isLoadingRwd = true;
        String codeId = posId;
        Log.d(TAG, "load rwdAd " + codeId);
        AdSlot adSlot = new AdSlot.Builder().setCodeId(codeId).setUserID(DeviceIdUtil.getDeviceId(appAct))//tag_id
                .setOrientation(AppActivity.get().isPortatil() ? TTAdConstant.VERTICAL: TTAdConstant.HORIZONTAL) //必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .setAdLoadType(PRELOAD)//推荐使用，用于标注此次的广告请求用途为预加载（当做缓存）还是实时加载，方便后续为开发者优化相关策略
                .build();
        _adKit.getTTAdNative().loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.d(TAG, "Callback --> onError: " + code + ", " + String.valueOf(message));
                isLoadingRwd = false;
            }

            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                Log.d(TAG, "Callback --> onRewardVideoAdLoad");
                mttRewardVideoAd = ad;
                isLoadingRwd = false;
                if(autoPlay) {
                    autoPlay = false;
                    playRewardAd();
                }
            }

            @Override
            public void onRewardVideoCached() {
                Log.d(TAG, "Callback --> onRewardVideoCached 1");
                isLoadingRwd = false;
            }

            @Override
            public void onRewardVideoCached(TTRewardVideoAd ttRewardVideoAd) {
                Log.d(TAG, "Callback --> onRewardVideoCached 2");
                mttRewardVideoAd = ttRewardVideoAd;
                isLoadingRwd = false;
                if(autoPlay) {
                    autoPlay = false;
                    playRewardAd();
                }
            }
        });
    }

    public void playRewardAd() {
        if(disposed) return;
        if (this.isLoadingRwd) {
            JSBKit.get().ShowAdRet("-1");
            return;
        }
        int rlt = _adKit.adCheck();
        if(rlt < 0) {
            JSBKit.get().ShowAdRet(Integer.toString(rlt));
            return;
        }
        AppActivity.get().runOnUiThread(() -> {
            if (mttRewardVideoAd != null) {
                mttRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {
                    //广告的下载bar点击回调
                    @Override
                    public void onAdVideoBarClick() {

                    }

                    //视频广告关闭回调
                    @Override
                    public void onAdClose() {
                        loadRewardAD();
                    }

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete() {
                        //  JsbBridge.sendToScript("onUserRewarded", "rewardAmount");
                    }

                    //视频广告播放错误回调
                    @Override
                    public void onVideoError() {

                    }

                    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称，code：错误码，msg：错误信息
                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int code, String msg) {
                        JSBKit.get().ShowAdRet("1");
                        mttRewardVideoAd = null;
                        loadRewardAD();
                    }

                    @Override
                    public void onRewardArrived(boolean b, int i, Bundle bundle) {

                    }

                    //视频广告跳过回调
                    @Override
                    public void onSkippedVideo() {

                    }

                    //视频广告展示回调
                    @Override
                    public void onAdShow() {

                    }
                });
                //展示广告，并传入广告展示的场景
                mttRewardVideoAd.showRewardVideoAd(appAct, TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
                mttRewardVideoAd = null;
            } else {
                Log.w(TAG, "请先加载广告");
//                JSBKit.get().ShowAdRet("-1");
                loadRewardAD();
            }
        });
    }
}
