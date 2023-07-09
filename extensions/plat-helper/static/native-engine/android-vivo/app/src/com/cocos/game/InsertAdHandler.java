package com.cocos.game;

import android.app.Activity;
import android.util.Log;

import com.qhhz.cocos.libandroid.SPStorage;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.base.callback.MediaListener;
import com.vivo.mobilead.unified.interstitial.UnifiedVivoInterstitialAd;
import com.vivo.mobilead.unified.interstitial.UnifiedVivoInterstitialAdListener;

public class InsertAdHandler {
    private static final String TAG = "InsertAdHandler";
    private static final String SPSKEY = "InsertAdHandler_KEY_";

    private String posId;
    private AdParams adParams;
    private UnifiedVivoInterstitialAd vivoInterstitialAd;
    private Activity act;
    private boolean isValid;
    // 0 视频插屏 1 图片插屏
    private int adType;
    private int oriAdType;
    private boolean everCloseBanner;

    public InsertAdHandler(String posId) {
        this.posId = posId;
        String spsVal = SPStorage.get().getStr(SPSKEY  + posId, "video");
        act = AppActivity.get();
        AdParams.Builder builder = new AdParams.Builder(posId);
        adParams = builder.build();
        Log.d(TAG, spsVal + " " + spsVal.equals("video"));
        adType = spsVal.equals("video") ? 0 : 1;
        oriAdType = adType;
        Log.d(TAG, "adType: " + adType);
        isValid = true;
        everCloseBanner = false;
    }

    public void Dispose() {
        adParams = null;
        vivoInterstitialAd = null;
        act = null;
        isValid = false;
    }

    public void play() {
        if(!isValid) return;
        if(vivoInterstitialAd != null) return;
        if(adType == 0) {
            loadVieoAd();
        } else {
            loadImgAd();
        }
    }
    private void loadVieoAd() {
        if(!isValid) return;
        vivoInterstitialAd = new UnifiedVivoInterstitialAd(act, adParams, interstitialAdListener);
        vivoInterstitialAd.setMediaListener(mediaListener);
        vivoInterstitialAd.loadVideoAd();
    }

    private void loadImgAd() {
        if(!isValid) return;
        vivoInterstitialAd = new UnifiedVivoInterstitialAd(act, adParams, interstitialAdListener);
        vivoInterstitialAd.loadAd();
        vivoInterstitialAd.setMediaListener(mediaListener);
    }
    private void showVideoAd() {
        if(!isValid) return;
        if (vivoInterstitialAd != null) {
            if(adType == 0) {
                vivoInterstitialAd.showVideoAd(act);
            } else {
                vivoInterstitialAd.showAd();
            }
        }
    }

    private UnifiedVivoInterstitialAdListener interstitialAdListener = new UnifiedVivoInterstitialAdListener() {
        @Override
        public void onAdReady() {
//            if (vivoInterstitialAd.getPrice() > 0 || !TextUtils.isEmpty(vivoInterstitialAd.getPriceLevel())) {
//                handlerBidding(materialType);
//            }
            Log.d(TAG, "onAdReady");
            if(BannerAd.checkIsShowing()) {
                BannerAd.hidedlg();
                everCloseBanner = true;
            }
            showVideoAd();
        }

        @Override
        public void onAdFailed(VivoAdError error) {
            Log.d(TAG, "onAdFailed: " + error.toString());
            if ((error.getCode() == 40120008 || error.getCode() == 4012) && adType == 0) {
                //[2023-04-25 09:50:56.540] [29581] [29581] [3] [InsertAdHandler] : onAdFailed: VivoAdError{code=40120008, msg='没有广告数据，建议稍后重试'}
                adType = 1;
                AppActivity.get().runOnUiThread(()->{
                    vivoInterstitialAd = null;
                    play();
                });
            } else {
                vivoInterstitialAd = null;
                if(everCloseBanner) {
                    BannerAd.showdlg();
                    everCloseBanner = false;
                }
            }
        }

        @Override
        public void onAdClick() {
            Log.d(TAG, "onAdClick");
        }

        @Override
        public void onAdShow() {
            Log.d(TAG, "onAdShow");
        }

        @Override
        public void onAdClose() {
            Log.d(TAG, "onAdClose");
            vivoInterstitialAd = null;
            if(oriAdType == 0 && adType == 1) {
                SPStorage.get().setStr(SPSKEY  + posId, "image");
                oriAdType = 1;
            }

            if(everCloseBanner) {
                BannerAd.showdlg();
                everCloseBanner = false;
            }
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
