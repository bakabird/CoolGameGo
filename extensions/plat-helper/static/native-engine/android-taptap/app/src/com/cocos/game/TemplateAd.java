package com.cocos.game;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;

public class TemplateAd {
    private static final String TAG = "TemplateAd";

    private ITemplateAdListenner mListenner;
    private FrameLayout mContainer;
    private String mPositionId;
    private Activity mAct;
    private Integer mWidthDp;

    private TTNativeExpressAd mTTAd;
    private long startTime = 0;
    private boolean mHasShowDownloadActive;


    public TemplateAd(Activity act, FrameLayout container, String positionId, Integer widthDp, ITemplateAdListenner listenner) {
        Log.i(TAG, "ctor");
        mAct = act;
        mContainer = container;
        mPositionId = positionId;
        mListenner = listenner;
        mWidthDp = widthDp;
        startTime = 0;
        mHasShowDownloadActive = false;
    }

    public void destory() {
        mAct.runOnUiThread(() -> {
            mContainer.removeAllViews();
            mContainer = null;
            if (mTTAd != null) {
                mTTAd.destroy();
            }
        });
    }

    public void loadAd() {
        Log.i(TAG, "load");
        mContainer.removeAllViews();
        // 高度设置为0，则高度会自适应
        float adHeight = 0;
        float adWidth = 0;
        try{
            adWidth = (float)mWidthDp;
        }catch (Exception e){
            adWidth = 0; //高度设置为0,则高度会自适应
        }
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(mPositionId)
                .setAdCount(1)
                .setExpressViewAcceptedSize(adWidth, adHeight)
                .build();
        ADKit.get().getTTAdNative().loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "load error : " + code + ", " + message);
                mContainer.removeAllViews();
                if (code == 20001) {
                    mListenner.onError("-2");
                } else {
                    mListenner.onError("" + code);
                }
            }

            @Override

            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0){
                    return;
                }
                mTTAd = ads.get(0);
                bindAdListener(mTTAd);
                showAd();
            }
        });
    }

    protected void showAd() {
        Log.i(TAG, "showAd");
        if (mTTAd != null) {
            startTime = System.currentTimeMillis();
            mTTAd.render();
        }
    }

    private void bindAdListener(TTNativeExpressAd ad) {

        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override

            public void onAdClicked(View view, int type) {
                Log.d(TAG, "广告被点击");
                mListenner.onClick();
            }

            @Override

            public void onAdShow(View view, int type) {
                Log.d(TAG, "广告展示");
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.e(TAG,"render fail:"+(System.currentTimeMillis() - startTime));
                Log.e(TAG, msg+" code:"+code);
                mListenner.onError("" + code);
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.d(TAG,"render suc:"+(System.currentTimeMillis() - startTime));
                mContainer.removeAllViews();
                mContainer.addView(view);
                mListenner.onShow();
            }
        });
        //dislike设置
        bindDislike(ad, true);

        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD){
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle() {
                Log.d(TAG, "点击开始下载");
            }

            @Override
            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    Log.e(TAG, "下载中，点击暂停");
                }
            }

            @Override
            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                Log.d(TAG, "下载暂停，点击继续");
            }

            @Override
            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                Log.e(TAG, "下载失败，点击重新下载");
            }

            @Override
            public void onInstalled(String fileName, String appName) {
                Log.d(TAG, "安装完成，点击图片打开");
            }

            @Override
            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                Log.d(TAG, "点击安装");
            }
        });
    }

    /**
     * 设置广告的不喜欢，注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板广告中的 dislike区域不响应dislike事件。
     * @param ad
     * @param customStyle 是否自定义样式，true:样式自定义
     */

    private void bindDislike(TTNativeExpressAd ad, boolean customStyle) {
        if (customStyle) {
//            //使用自定义样式
//            DislikeInfo dislikeInfo = ad.getDislikeInfo();
//            if (dislikeInfo == null || dislikeInfo.getFilterWords() == null || dislikeInfo.getFilterWords().isEmpty()) {
//                return;
//            }
            final DislikeDialog dislikeDialog = new DislikeDialog(AppActivity.get(), ()->{
                mContainer.removeAllViews();
                mListenner.onDismiss();
            });
//            dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
//                @Override
//                public void onItemClick(FilterWord filterWord) {
//                    //屏蔽广告
//                    TToast.show(mContext, "点击 " + filterWord.getName());
//                    //用户选择不喜欢原因后，移除广告展示
//                    mExpressContainer.removeAllViews();
//                }
//            });
            ad.setDislikeDialog(dislikeDialog);
            return;
        }
        //使用默认模板中默认dislike弹出样式

        ad.setDislikeCallback(mAct, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {
                Log.d(TAG, "Dislike 展示~");
            }

            @Override
            public void onSelected(int position, String value, boolean enforce) {
                Log.d(TAG, "点击 " + value);
                //用户选择不喜欢原因后，移除广告展示
                mContainer.removeAllViews();
                if (enforce) {
                        Log.d(TAG, "NativeExpressActivity 模版信息流 sdk强制移除View ");
                }
                mListenner.onDismiss();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "点击取消");
            }

        });
    }

}
