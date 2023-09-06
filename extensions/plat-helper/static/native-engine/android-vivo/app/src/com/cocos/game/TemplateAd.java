package com.cocos.game;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VideoPolicy;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.base.callback.MediaListener;
import com.vivo.mobilead.unified.nativead.UnifiedVivoNativeExpressAd;
import com.vivo.mobilead.unified.nativead.UnifiedVivoNativeExpressAdListener;
import com.vivo.mobilead.unified.nativead.VivoNativeExpressView;

public class TemplateAd {
    private static final String TAG = "TemplateAd";

    private UnifiedVivoNativeExpressAd nativeExpressAd;
    private VivoNativeExpressView nativeExpressView;
    private ITemplateAdListenner mListenner;
    private FrameLayout mContainer;
    private FrameLayout mWaterflowerContainer;
    private String mPositionId;
    private Activity mAct;
    private Integer mWidthDp;
    private Integer mScale;

    private Integer mGravity;

    public TemplateAd(Activity act, FrameLayout container,FrameLayout waterflowerContainer, String positionId, Integer widthDp, Integer scale,int gravity,ITemplateAdListenner listenner) {
        Log.i(TAG, "ctor");
        mAct = act;
        mContainer = container;
        mWaterflowerContainer = waterflowerContainer;
        mPositionId = positionId;
        mListenner = listenner;
        mWidthDp = widthDp;
        mScale = scale;
        mGravity =  gravity;
    }

    public void destory() {
        mAct.runOnUiThread(() -> {
            mContainer.removeAllViews();
            mContainer = null;
            nativeExpressAd = null;
            if (nativeExpressView != null) {
                nativeExpressView.destroy();
            }
        });
    }

    public void loadAd() {
        Log.i(TAG, "load");
        //销毁旧数据
        if (nativeExpressView != null) {
            nativeExpressView.destroy();
            mContainer.removeAllViews();
        }

        AdParams.Builder builder = new AdParams.Builder(mPositionId);
        /**
         * 用户角度视频播放策略，取值可以参考{@link com.vivo.mobilead.unified.base.VideoPolicy}
         * VideoPolicy.MANUAL           WIFI、流量环境下始终需要用户手动点击开始播放
         * VideoPolicy.AUTO_AWAYS       WIFI、流量环境下始终自动开始播放
         * VideoPolicy.MANUAL           WIFI环境下自动开始播放,流量环境下需要用户手动点击开始播放
         */
        builder.setVideoPolicy(VideoPolicy.AUTO_AWAYS);
        /**
         * 如何设置广告的尺寸：
         * 方法一：
         * 接入阶段可以通过setNativeExpressWidth、setNativeExpressHegiht方法设置最终生成的模板视图尺寸，根据实际广告展示场景找到适合尺寸,
         * 发布时在构造UnifiedVivoNativeExpressAd的时候传入预定尺寸 和 固定广告位id 进行展示
         *
         * 方法二：
         * 根据实际广告展示场景需要，设置一个固定宽度，高度不设置让广告的高度根据宽度去自适应。
         *
         * 小于0的值都会被SDK忽略而不生效，没有设置视图尺寸时将使用模板的默认尺寸进行渲染。
         * 一旦同时设置了视图尺寸，SDK会按实际设置大小进行渲染，如果设置尺寸比例、大小不合适，会出现视图被裁减的情况。
         */
        builder.setNativeExpressWidth(mWidthDp);
        nativeExpressAd = new UnifiedVivoNativeExpressAd(mAct, builder.build(), expressListener);
        nativeExpressAd.loadAd();
    }

    protected void showAd() {
        Log.i(TAG, "showAd");
        if (nativeExpressView != null) {
            nativeExpressView.setMediaListener(mediaListener);
            mContainer.removeAllViews();
            mContainer.addView(nativeExpressView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    /**
     * 广告加载监听
     */
    private UnifiedVivoNativeExpressAdListener expressListener = new UnifiedVivoNativeExpressAdListener() {
        /**
         * 广告加载、渲染成功回调，view为渲染后视图，
         *
         * 注意: 在onAdReady中对返回的view重新设置尺寸不会生效，如果尺寸设置过小会导致视图被裁减
         * @param view
         */
        @Override
        public void onAdReady(VivoNativeExpressView view) {
            Log.i(TAG, "onAdReady................");
            nativeExpressView = view;
            AppActivity.doRunOnUiThread(()->{
                showAd();
            });
        }

        @Override
        public void onAdFailed(VivoAdError error) {
            int code = error.getCode();
            Log.i(TAG, "onAdFailed................" + code + " " + error.toString());
            mListenner.onError("" + error.getCode());
            if (AdKit.get().noAdErrorCheck(code)) {
                mListenner.onError("-2");
            } else  {
                mListenner.onError("" + code);
            }
        }

        @Override
        public void onAdClick(VivoNativeExpressView view) {
            Log.i(TAG, "onAdClick................");
            mListenner.onClick();
        }

        @Override
        public void onAdShow(VivoNativeExpressView view) {
            Log.i(TAG, "onAdShow................");
            mListenner.onShow();
            mAct.runOnUiThread(()->{
                float fWidth = view.getWidth();
                float fHeight =  mContainer.getHeight();

                if((mGravity & Gravity.TOP) == Gravity.TOP){
//                    Log.d(TAG, "Gravity.TOP");
                    mContainer.setPivotY(0);
                    mWaterflowerContainer.setPivotY(0);
                }
                else if((mGravity & Gravity.BOTTOM) == Gravity.BOTTOM){
//                    Log.d(TAG, "Gravity.BOTTOM");
                    mContainer.setPivotY(fHeight);
                    mWaterflowerContainer.setPivotY(fHeight);
                }
                else if((mGravity & Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL){
//                    Log.d(TAG, "Gravity.CENTER_VERTICAL");
                    mContainer.setPivotY(fHeight/2);
                    mWaterflowerContainer.setPivotY(fHeight/2);
                }

                if((mGravity & Gravity.LEFT) == Gravity.LEFT){
//                    Log.d(TAG, "Gravity.LEFT");
                    mContainer.setPivotX(0);
                    mWaterflowerContainer.setPivotX(0);
                }
                else if((mGravity & Gravity.RIGHT) == Gravity.RIGHT){
//                    Log.d(TAG, "Gravity.RIGHT");
                    mContainer.setPivotX(fWidth);
                    mWaterflowerContainer.setPivotX(fWidth);
                }
                else if((mGravity & Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL){
//                    Log.d(TAG, "Gravity.CENTER_HORIZONTAL");
                    mContainer.setPivotX(fWidth/2);
                    mWaterflowerContainer.setPivotX(fWidth/2);
                }


                float fScale = mScale * 0.01f;
                mContainer.setScaleX(fScale);
                mContainer.setScaleY(fScale);

                mWaterflowerContainer.setScaleX(fScale);
                mWaterflowerContainer.setScaleY(fScale);

            });
        }

        @Override
        public void onAdClose(VivoNativeExpressView view) {
            Log.i(TAG, "onAdClose................");
            mContainer.removeAllViews();
            mListenner.onDismiss();
        }
    };

    /**
     * 视频播放状态监听
     */
    private MediaListener mediaListener = new MediaListener() {
        @Override
        public void onVideoStart() {
            Log.i(TAG, "onVideoStart................");
        }

        @Override
        public void onVideoPause() {
            Log.i(TAG, "onVideoPause................");
        }

        @Override
        public void onVideoPlay() {
            Log.i(TAG, "onVideoPlay................");
        }

        @Override
        public void onVideoError(VivoAdError error) {
            int code = error.getCode();
            Log.i(TAG, "onVideoError:" + code + "  " + error.toString());
            if (AdKit.get().noAdErrorCheck(code)) {
                mListenner.onError("-2");
            } else  {
                mListenner.onError("" + code);
            }
        }

        @Override
        public void onVideoCompletion() {
            Log.i(TAG, "onVideoCompletion................");
        }

        @Override
        public void onVideoCached() {
            Log.i(TAG, "onVideoCached................");
        }
    };

}
