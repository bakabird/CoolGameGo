package com.cocos.game;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;

public class InsertAd {
    private static final String TAG = "InsertAd";

    private String posId;
    private ADKit mAdKit;
    private AdLoadListener mAdLoadListener;
    private AppActivity appAct;

    /**
     * -1 已销毁
     * 0 初始
     * 1 加载中
     * 2 加载完毕
     * 3 播放中
     */
    private int statu;

    public InsertAd(String posId, ADKit kit) {
        this.posId = posId;
        statu = 0;
        appAct = AppActivity.get();
        mAdKit = kit;
    }

    public void Dispose() {
        appAct = null;
        mAdKit = null;
        mAdLoadListener = null;
        statu = -1;
    }

    /**
     * 加载广告
     */
    private void load() {
        if(statu != 0) return;
        statu = 1;
        String codeId = this.posId;
        //step5:创建广告请求参数AdSlot
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) // 广告代码位Id
                .setAdLoadType(TTAdLoadType.LOAD) // 本次广告用途：TTAdLoadType.LOAD实时；TTAdLoadType.PRELOAD预请求
                .build();
        //step6:注册广告加载生命周期监听，请求广告
        mAdLoadListener = new AdLoadListener(appAct, () -> {
            if (statu == -1) return;
            statu = 2;
            statu = 3;
            mAdLoadListener.showAd(TTAdConstant.RitScenes.CUSTOMIZE_SCENES, "scenes_test");
        }, code -> {
            statu = 0;
            mAdLoadListener = null;
            JSBKit.get().InsertAdRet(code);
        }, ()->{
            statu = 0;
            mAdLoadListener = null;
            JSBKit.get().InsertAdRet("1");
        });
        mAdKit.getTTAdNative().loadFullScreenVideoAd(adSlot, mAdLoadListener);
    }

    /**
     * 展示广告
     */
    public void play() {
        if(statu == -1) return;
        if (mAdKit.adCheck() < 0) {
            JSBKit.get().InsertAdRet("0");
            return;
        }
        if (statu == 0) {
            this.load();
            return;
        }
    }

    /**
     * 【必须】广告加载期间生命周期监听
     */

    private static class AdLoadListener implements TTAdNative.FullScreenVideoAdListener {
        public interface OnLoadFail {
            void onFail(String code);
        }

        private final Activity mActivity;

        private TTFullScreenVideoAd mAd;

        private Runnable mOnLoaded;
        private OnLoadFail mOnLoadfail;
        private Runnable mOnAdClose;

        public AdLoadListener(Activity activity, Runnable onLoaded, OnLoadFail onLoadfail, Runnable onAdClose) {
            mActivity = activity;
            mOnLoaded = onLoaded;
            mOnLoadfail = onLoadfail;
            mOnAdClose = onAdClose;
        }

        @Override
        public void onError(int code, String message) {
            Log.e(TAG, "Callback --> onError: " + code + ", " + message);
            dealCallback("" + code);
        }

        @Override
        public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
            Log.e(TAG, "Callback --> onFullScreenVideoAdLoad");
            handleAd(ad);
        }

        @Override

        public void onFullScreenVideoCached() {
            // 已废弃 请使用 onRewardVideoCached(TTRewardVideoAd ad) 方法
        }

        @Override

        public void onFullScreenVideoCached(TTFullScreenVideoAd ad) {
            Log.e(TAG, "Callback --> onFullScreenVideoCached");
            handleAd(ad);
        }

        private void dealCallback(String errcode) {
            boolean suc = errcode.equals("");
            Log.d(TAG, "dealCallback " +  suc);
            if(mOnLoaded == null) return;
            if(mOnLoadfail == null) return;
            if(suc) {
                mOnLoaded.run();
            } else  {
                mOnLoadfail.onFail(errcode);
            }
            mOnLoaded = null;
            mOnLoadfail = null;
        }

        /**
         * 处理广告对象
         */
        public void handleAd(TTFullScreenVideoAd ad) {
            if (mAd != null) {
                dealCallback("-2");
                return;
            }
            mAd = ad;
            //【必须】广告展示时的生命周期监听
            mAd.setFullScreenVideoAdInteractionListener(new AdLifeListener(mActivity, mOnAdClose));
            dealCallback("");
            //【可选】监听下载状态
//            mAd.setDownloadListener(new DownloadStatusListener());
        }

        /**
         * 触发展示广告
         */

        public void showAd(TTAdConstant.RitScenes ritScenes, String scenes) {
            if (mAd == null) {
//                TToast.show(mActivity, "当前广告未加载好，请先点击加载广告");
                return;
            }
            mAd.showFullScreenVideoAd(mActivity, ritScenes, scenes);
            // 广告使用后应废弃
            mAd = null;
        }
    }

    /**
     * 【必须】广告生命状态监听器
     */

    private static class AdLifeListener implements TTFullScreenVideoAd.FullScreenVideoAdInteractionListener {

        private boolean everHideBanner;
        private Runnable onClose;

        public AdLifeListener(Context context, Runnable onClose) {
            this.onClose = onClose;
            this.everHideBanner = false;
        }

        @Override
        public void onAdShow() {
            Log.d(TAG, "Callback --> FullVideoAd show");
            if(BannerAd.checkIsShowing()) {
                BannerAd.hidedlg();
                this.everHideBanner = true;
            }
        }

        @Override

        public void onAdVideoBarClick() {
            Log.d(TAG, "Callback --> FullVideoAd bar click");
        }

        @Override

        public void onAdClose() {
            Log.d(TAG, "Callback --> FullVideoAd close");
            onClose.run();
            if(this.everHideBanner) {
                BannerAd.showdlg();
            }
        }

        @Override
        public void onVideoComplete() {
            Log.d(TAG, "Callback --> FullVideoAd complete");
        }

        @Override
        public void onSkippedVideo() {
            Log.d(TAG, "Callback --> FullVideoAd skipped");
        }
    }

}
