package com.cocos.game;

import android.util.Log;

import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.base.callback.MediaListener;
import com.vivo.mobilead.unified.reward.UnifiedVivoRewardVideoAd;
import com.vivo.mobilead.unified.reward.UnifiedVivoRewardVideoAdListener;

public class RewardAdHandler {
    private static final String TAG = "RewardAdHandler";

    private UnifiedVivoRewardVideoAd vivoRewardVideoAd;
    private boolean isLoadAndShow = false;
    private boolean isRwded = false;
    private AdParams.Builder builder;

    public RewardAdHandler(String posId) {
        builder = new AdParams.Builder(posId);
    }

    public void Dispose() {
        builder = null;
        vivoRewardVideoAd = null;
    }

    public void playAd() {
        if(vivoRewardVideoAd != null) {
            JSBKit.get().ShowAdRet("-1");
            return;
        }
        isLoadAndShow = true;
        loadAd();
    }

    protected void loadAd() {
        vivoRewardVideoAd = new UnifiedVivoRewardVideoAd(AppActivity.get(), builder.build(), rewardVideoAdListener);
        vivoRewardVideoAd.setMediaListener(mediaListener);
        vivoRewardVideoAd.loadAd();
    }

    protected void showAd() {
        if (vivoRewardVideoAd != null) {
            vivoRewardVideoAd.showAd(AppActivity.get());
        }
    }

    private UnifiedVivoRewardVideoAdListener rewardVideoAdListener = new UnifiedVivoRewardVideoAdListener() {
        @Override
        public void onAdReady() {
            Log.d(TAG, "onAdReady");
            if (isLoadAndShow) {
                showAd();
                isLoadAndShow = false;
            }
            //此处可以调用showAd展示视频了，也可以等待视频缓存好，即onVideoCached后再展示视频
        }

        @Override
        public void onAdFailed(VivoAdError vivoAdError) {
//            ToastUtil.show("onAdFailed");
            Log.d(TAG, "onAdFailed: " + vivoAdError.toString());
            vivoRewardVideoAd = null;
            JSBKit.get().ShowAdRet("-1");
        }

        @Override
        public void onAdClick() {
//            ToastUtil.show("onAdClick");
            Log.d(TAG, "onAdClick");
        }

        @Override
        public void onAdShow() {
//            ToastUtil.show("onAdShow");
            Log.d(TAG, "onAdShow");
        }

        @Override
        public void onAdClose() {
//            ToastUtil.show("onAdClose");
            Log.d(TAG, "onAdClose");
            if (isRwded) {
                JSBKit.get().ShowAdRet("1");
            } else {
                JSBKit.get().ShowAdRet("0");
            }
            vivoRewardVideoAd = null;
            isRwded = false;
        }

        @Override
        public void onRewardVerify() {
//            ToastUtil.show("onRewardVerify");
            Log.d(TAG, "onRewardVerify");
            isRwded = true;
        }
    };

    private MediaListener mediaListener = new MediaListener() {
        @Override
        public void onVideoStart() {
            Log.d(TAG, "onVideoStart");
        }

        @Override
        public void onVideoPause() {
            Log.d(TAG, "onVideoPause");
        }

        @Override
        public void onVideoPlay() {
            Log.d(TAG, "onVideoPlay");
        }

        @Override
        public void onVideoError(VivoAdError error) {
            Log.d(TAG, "onVideoError: " + error.toString());
        }

        @Override
        public void onVideoCompletion() {
            Log.d(TAG, "onVideoCompletion");
        }

        @Override
        public void onVideoCached() {
            Log.d(TAG, "onVideoCached");
        }
    };
}
