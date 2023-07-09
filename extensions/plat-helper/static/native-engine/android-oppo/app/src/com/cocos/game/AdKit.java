package com.cocos.game;

import android.content.Context;
import android.util.Log;

import com.heytap.msp.mobad.api.ad.RewardVideoAd;
import com.heytap.msp.mobad.api.listener.IRewardVideoAdListener;
import com.heytap.msp.mobad.api.params.RewardVideoAdParams;

import java.util.Timer;
import java.util.TimerTask;

public class AdKit implements IRewardVideoAdListener {
    private final String TAG = "Adkit";
    private static String LastPosId = "";
    private static boolean autoPlay = false;
    private static AdKit me;
    public static AdKit getMe() {
        if (me == null) {
            me = new AdKit();
        }
        return me;
    }
    private RewardVideoAd mRewardVideoAd;
    // 0未拉 1拉中 2拉完 3播放中 4有奖励
    private int status = 0;
    private AdKit() {

    }
    private void loadVideo() {
        if(status == 0) {
            /**
             * 调用loadAd方法请求激励视频广告;通过RewardVideoAdParams.setFetchTimeout方法可以设置请求
             * 视频广告最大超时时间，单位毫秒；
             */
            status = 1;
            RewardVideoAdParams rewardVideoAdParams = new RewardVideoAdParams.Builder()
                    .setFetchTimeout(3000)
                    .build();
            mRewardVideoAd.loadAd(rewardVideoAdParams);
            Log.d(TAG, "loadVideo");
        }
    }

    private void reloadVideo(int delayMS) {
        Timer timer = new Timer();
        delayMS = Math.min(delayMS, 100);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                status = 0;
                loadVideo();
            }
        };
        timer.schedule(timerTask, delayMS);
    }

    public void playVideo(String posId) {
        if(!posId.equals(LastPosId)) {
            if(mRewardVideoAd != null) {
                mRewardVideoAd.destroyAd();
                mRewardVideoAd = null;
            }
            mRewardVideoAd = new RewardVideoAd(AppActivity.get(), posId, this);
            LastPosId = posId;
            status = 0;
            autoPlay = true;
            loadVideo();
        } else if (status == 1) {
            JSBKit.get().ShowAdRet("-1");
//            NativeFunction.OnShowAd("-1");
        } else if (mRewardVideoAd.isReady()) {
            status = 3;
            mRewardVideoAd.showAd();
        }
        Log.d(TAG, "playVideo");
    }

    @Override
    public void onAdClick(long l) {
        Log.d(TAG,"视频广告被点击，当前播放进度 = " + l + " 秒");
    }

    @Override
    public void onAdFailed(int i, String s) {
        /**
         * 请求广告失败、不展示播放视频的入口Dialog
         */
        Log.d(TAG,"请求视频广告失败. code:" +i + ",msg:"+ s);
        status = 0;
        autoPlay = false;
        JSBKit.get().ShowAdRet("-1");
        LastPosId = "";
    }

    /**
     * 已废弃，使用onAdFailed(int i, String s)
     */
    // Deprecated do nothing
    @Override
    public void onAdFailed(String s) {
    }

    @Override
    public void onAdSuccess() {
        Log.d(TAG, "请求视频广告成功.");
        status = 2;
        if(autoPlay) {
            playVideo(LastPosId);
            autoPlay = false;
        }
    }

    @Override
    public void onLandingPageClose() {
        Log.d(TAG,"视频广告落地页关闭.");
        if(status == 4) {
            JSBKit.get().ShowAdRet("1");
//            NativeFunction.OnShowAd("1");
        }
        reloadVideo(1000);
    }

    @Override
    public void onLandingPageOpen() {
        Log.d(TAG,"视频广告落地页打开.");
    }

    @Override
    public void onVideoPlayClose(long l) {
        Log.d(TAG,"视频广告被关闭，当前播放进度 = " + l + " 秒 statu " + status);
        if(status == 4) {
            JSBKit.get().ShowAdRet("1");
//            NativeFunction.OnShowAd("1");
        } else {
            JSBKit.get().ShowAdRet("0");
        }
        reloadVideo(1000);
    }

    @Override
    public void onVideoPlayComplete() {
        /**
         * TODO 注意：从SDK 3.0.1版本开始，不能在激励视频广告播放完成回调-onVideoPlayComplete里做任何激励操作，统一在onReward回调里给予用户激励。
         */
        Log.d(TAG,"视频广告播放完成.");
    }

    @Override
    public void onVideoPlayError(String s) {
        Log.d(TAG,"视频播放错误，错误信息=" + s);
    }

    @Override
    public void onVideoPlayStart() {
        Log.d(TAG,"视频开始播放.");
    }

    @Override
    public void onReward(Object... objects) {
        Log.d(TAG, "奖励发放");
        status = 4;
    }

    public void playBanner(String posId, String pos) {
        Log.d(TAG, "playbanner " + posId + "  " + pos);
        MyBannerAd.fetch(posId, pos);
    }

    public void hideBanner() {
        Log.d(TAG, "hideBanner");
        MyBannerAd.hidedlg();
    }

    public void playInterstitialAd(String posId) {
        Log.d(TAG, "playInterstitialAd");
        MyInterstitialAd.get().play(posId);
    }
}
