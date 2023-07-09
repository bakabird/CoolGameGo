package com.cocos.game;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;

public class BannerAd {
//public class BannerAd extends Dialog {
    private static String TAG = "BannerAd";
    private static String id;
    private static String pos;
    private static long outdateTime;
    public static BannerAd instance;

    public static void fetch(String posId, String pos) {
        boolean justReuse = posId.equals(BannerAd.id) && pos.equals(BannerAd.pos) && BannerAd.outdateTime > System.currentTimeMillis();
        Log.d(TAG, posId + ",,," + BannerAd.pos);
        Log.d(TAG, System.currentTimeMillis() + "..." + BannerAd.outdateTime);
        Log.d(TAG, "justReuse " + justReuse);
        if(justReuse) {
            AppActivity.doRunOnUiThread(() -> {
                if(instance != null && !instance.isShowing()) {
                    if(posId.equals(BannerAd.id)) {
                        instance.show();
                    }
                }
            });
        } else {
            if (instance != null && instance.isShowing()) {
                instance.cancel();
            }
            BannerAd.id = posId;
            BannerAd.pos = pos;
            BannerAd.outdateTime = System.currentTimeMillis() + 15 * 1000;
            try {
                AppActivity.doRunOnUiThread(() -> {
                    if(posId.equals(BannerAd.id)) {
                        instance = new BannerAd(AppActivity.get());
                        instance.show();
                    }
                });
            } catch (Exception e) {
                Log.e("fish", e.toString());
            }
        }
    }

    public static boolean checkIsShowing() {
        return instance != null && instance.isShowing();
    }

    public static void showdlg() {
        AppActivity.doRunOnUiThread(() -> {
            Log.d(TAG, "showdlg...");
            if (instance != null && !instance.isShowing()) {
                Log.d(TAG, "showdlg");
                instance.show();
            }
        });
    }
    public static void hidedlg() {
        AppActivity.doRunOnUiThread(() -> {
            Log.d(TAG, "hidedlg...");
            if (instance != null && instance.isShowing()) {
                Log.d(TAG, "hidedlg");
                instance.hide();
            }
        });
    }

    public static void canceldlg() {
        AppActivity.doRunOnUiThread(()->{
            Log.d(TAG, "cancelDlg...");
            if (instance != null) {
                Log.d(TAG, "cancelDlg");
                BannerAd.id = "";
                BannerAd.pos = "";
                if(instance.mContainer != null) {
                    instance.mContainer.removeAllViews();
                }
                instance.cancel();
                instance = null;
            }
        });
    }

    private ViewGroup mContainer;
    private TTNativeExpressAd mTTAd;
    private long startTime = 0;
    private AppActivity act;

    /**
     * -1 已销毁
     * 0 初始
     * 1 加载中
     * 2 加载完毕
     */
    private int statu = 0;

    public BannerAd(@NonNull Context context) {
//        super(context, R.style.Banner);
        statu = 0;
        onInit();
    }

    public boolean isShowing() {
        return mContainer.getVisibility() == View.VISIBLE;
    }

    public void show() {
        if(mContainer.getVisibility() != View.VISIBLE){
            AppActivity.doRunOnUiThread(()->{
                mContainer.setVisibility(View.VISIBLE);
            });
        }
    }

    public void hide() {
        if(mContainer.getVisibility() == View.VISIBLE){
            AppActivity.doRunOnUiThread(()->{
                mContainer.setVisibility(View.INVISIBLE);
            });
        }
    }

    public void cancel() {
        destroyBanner();
    }

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return false;
    }

    protected void onInit() {
        act = AppActivity.get();
        mContainer = act.bannerBox;
        loadExpressAd(BannerAd.id, Constant.BANNER_WIDTH, Constant.BANNER_HEIGHT);
    }

    protected void onCreate(Bundle bundle) {
//        super.onCreate(bundle);
//        BannerAd self = this;
//        act = AppActivity.get();
//        setContentView(R.layout.banner_dialog);
//        Window window = this.getWindow();
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
//        this.setCancelable(false);
//        window.setGravity(Gravity.BOTTOM);
//        mContainer = findViewById(R.id.view_ad_container);
//        loadExpressAd(BannerAd.id, Constant.BANNER_WIDTH, Constant.BANNER_HEIGHT);
//        setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                self.destroyBanner();
//                statu = -1;
//            }
//        });
    }

    public void destroyBanner() {
        mContainer.removeAllViews();
        if (mTTAd != null) {
            Log.d(TAG, "destory mTTAd");
            mTTAd.destroy();
        }
        act = null;
        mTTAd = null;
        mContainer = null;
    }

    private void loadExpressAd(String codeId, int expressViewWidth, int expressViewHeight) {
        if(statu != 0 ) return;
        statu = 1;
        mContainer.removeAllViews();
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //广告位id
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .build();
        //step5:请求广告，对请求回调的广告作渲染处理

        ADKit.get().getTTAdNative().loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int code, String message) {
//                TToast.show(BannerExpressActivity.this, "load error : " + code + ", " + message);
                Log.e(TAG, "load error : " + code + ", " + message);
                mContainer.removeAllViews();
                statu = 0;
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    statu = 0;
                    return;
                }
                /*******************
                 * 如果旧广告对象不使用了，在替换成新广告对象前，必须进行销毁，否则可能导致多个广告对象同时存在，影响SSR
                 */
                if (mTTAd != null) {
                    mTTAd.destroy();
                }
                /********************/
                mTTAd = ads.get(0);
                mTTAd.setSlideIntervalTime(30 * 1000);
                bindAdListener(mTTAd);
                startTime = System.currentTimeMillis();
                Log.d(TAG, "Load sucess");
                statu = 2;
                mTTAd.render();
//                TToast.show(mContext, "load success!");
            }
        });
    }

    private void bindAdListener(TTNativeExpressAd ad) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int type) {
//                TToast.show(mContext, "广告被点击");
                Log.d(TAG, "onAdClicked " + type);
            }

            @Override

            public void onAdShow(View view, int type) {
                Log.d(TAG, "onAdShow " + type);
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                Log.e(TAG, "render fail:" + (System.currentTimeMillis() - startTime));
                statu = 0;
                BannerAd.canceldlg();
            }

            @Override
            public void onRenderSuccess(View view, float width, float height) {
                Log.d(TAG, "render suc:" + (System.currentTimeMillis() - startTime));
                //返回view的宽高 单位 dp
                Log.d(TAG, "widht " + width + " height " + height);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.topMargin = 0;
                params.gravity = Gravity.CENTER_HORIZONTAL;
                mContainer.removeAllViews();
                mContainer.addView(view, params);
            }
        });
        //dislike设置
        bindDislike(ad, false);

//        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
//            return;
//        }
//        ad.setDownloadListener(new TTAppDownloadListener() {
//            @Override
//            public void onIdle() {
//                TToast.show(BannerExpressActivity.this, "点击开始下载", Toast.LENGTH_LONG);
//            }
//
//            @Override
//            public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
//                if (!mHasShowDownloadActive) {
//                    mHasShowDownloadActive = true;
//                    TToast.show(BannerExpressActivity.this, "下载中，点击暂停", Toast.LENGTH_LONG);
//                }
//            }
//
//            @Override
//            public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "下载暂停，点击继续", Toast.LENGTH_LONG);
//            }
//
//            @Override
//            public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "下载失败，点击重新下载", Toast.LENGTH_LONG);
//            }
//
//            @Override
//            public void onInstalled(String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "安装完成，点击图片打开", Toast.LENGTH_LONG);
//            }
//
//            @Override
//            public void onDownloadFinished(long totalBytes, String fileName, String appName) {
//                TToast.show(BannerExpressActivity.this, "点击安装", Toast.LENGTH_LONG);
//            }
//        });
    }

    /**
     * 设置广告的不喜欢, 注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板广告中的 dislike区域不响应dislike事件。
     *
     * @param ad
     * @param customStyle 是否自定义样式，true:样式自定义
     */

    private void bindDislike(TTNativeExpressAd ad, boolean customStyle) {
        if (customStyle) {
//            //使用自定义样式
//            final DislikeInfo dislikeInfo = ad.getDislikeInfo();
//            if (dislikeInfo == null || dislikeInfo.getFilterWords() == null || dislikeInfo.getFilterWords().isEmpty()) {
//                return;
//            }
//            final DislikeDialog dislikeDialog = new DislikeDialog(this, dislikeInfo);
//            dislikeDialog.setOnDislikeItemClick(new DislikeDialog.OnDislikeItemClick() {
//                @Override
//                public void onItemClick(FilterWord filterWord) {
//                    //屏蔽广告
//                    TToast.show(mContext, "点击 " + filterWord.getName());
//                    //用户选择不喜欢原因后，移除广告展示
//                    mExpressContainer.removeAllViews();
//                }
//            });
//            ad.setDislikeDialog(dislikeDialog);
//            return;
        }
        //使用默认模板中默认dislike弹出样式

        ad.setDislikeCallback(act, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {

            }

            @Override
            public void onSelected(int position, String value, boolean enforce) {
//                TToast.show(mContext, "点击 " + value);
                Log.d(TAG, "Dislike 点击 " + value);
                mContainer.removeAllViews();
                //用户选择不喜欢原因后，移除广告展示
                if (enforce) {
                    Log.d(TAG,"模版Banner 穿山甲sdk强制将view关闭了");
//                    TToast.show(mContext, "模版Banner 穿山甲sdk强制将view关闭了");
                }
                statu = 0;
                hidedlg();
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"模版Banner 点击取消");
//                TToast.show(mContext, "点击取消 ");
            }

        });
    }

}