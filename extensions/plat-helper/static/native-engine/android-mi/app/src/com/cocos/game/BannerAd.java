package com.cocos.game;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.LinesXFree.cocos.R;
import com.qhhz.cocos.libandroid.QhhzDialog;
import com.xiaomi.ad.mediation.MMAdConfig;
import com.xiaomi.ad.mediation.MMAdError;
import com.xiaomi.ad.mediation.bannermimo.MMAdBanner;
import com.xiaomi.ad.mediation.bannermimo.MMBannerAd;

import java.util.List;


public class BannerAd extends QhhzDialog {
    private static String TAG = "BannerAd";
    private static String id;
    private static String pos;
    private static long outdateTime;

    public static BannerAd instance;

    public static void fetch(String posId, String pos) {
        if(!AdKit.get().checkAdPermission()) {
            return;
        }
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
            if (instance != null) {
                instance.cancel();
            }
            BannerAd.id = posId;
            BannerAd.pos = pos;
            BannerAd.outdateTime = System.currentTimeMillis() + 15 * 1000;
            AppActivity.doRunOnUiThread(() -> {
                if(posId.equals(BannerAd.id)) {
                    instance = new BannerAd(AppActivity.get());
                    instance.show();
                }
            });
        }
    }

    public static boolean checkIsShowing() {
        return instance != null && instance.isShowing();
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

    public static void showdlg() {
        AppActivity.doRunOnUiThread(() -> {
            Log.d(TAG, "showdlg...");
            if (instance != null && !instance.isShowing()) {
                Log.d(TAG, "showdlg");
                instance.show();
            }
        });
    }

    private MMAdBanner mAdBanner;
    private ViewGroup mContainer;
    private MMBannerAd mBannerAd;
    /**
     * -1 已销毁
     * 0 初始
     * 1 加载中
     * 2 加载完毕
     */
    private int statu = 0;

    public BannerAd(@NonNull Context context) {
        super(context, R.style.Banner);
        statu = 0;
    }

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return false;
    }


    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        BannerAd self = this;
        setContentView(R.layout.banner_dialog);
        Window window = this.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        this.setCancelable(false);
        window.setGravity(Gravity.BOTTOM);
        mContainer = findViewById(R.id.view_ad_container);
        mAdBanner = new MMAdBanner(App.get(), BannerAd.id);
        mAdBanner.onCreate();
        loadBannerAd();
        setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                self.destroyBanner();
                statu = -1;
            }
        });
    }

    public void destroyBanner() {
        if (mBannerAd != null) {
            Log.d(TAG, "onDestroy");
            mBannerAd.destroy();
        }
    }

    public void loadBannerAd() {
        if(statu != 0 ) return;
        statu = 1;
        mContainer.removeAllViews();
        MMAdConfig adConfig = new MMAdConfig();
        adConfig.supportDeeplink = true;
        // 必选参数 设置广告图片的最大尺寸及期望的图片宽高比，单位Px
        adConfig.imageWidth = 640;
        adConfig.imageHeight = 320;
        /**
         * 需要写死 600 和 90,，单位dp
         */
        adConfig.viewWidth = 600;
        adConfig.viewHeight = 90;
        adConfig.setBannerContainer(mContainer);//传入banner view容器 建议宽高自适应
        adConfig.setBannerActivity(AppActivity.get());
        mAdBanner.load(adConfig, new MMAdBanner.BannerAdListener() {
            @Override
            public void onBannerAdLoaded(List<MMBannerAd> list) {
                Log.d(TAG, "onBannerAdLoaded...");
                if (list != null && list.size() > 0) {
                    Log.d(TAG, "onBannerAdLoaded");
                    mBannerAd = list.get(0);
                    statu = 2;
                    showAd();
                } else {
                    statu = 0;
                }
            }
            @Override
            public void onBannerAdLoadError(MMAdError error) {
                Log.e(TAG, error.errorMessage);
                statu = 0;
            }
        });
    }

    private void showAd() {
        if(statu != 2) return;
        mBannerAd.show(new MMBannerAd.AdBannerActionListener() {
            @Override
            public void onAdShow() {
                Log.d(TAG, "onAdShow");
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "onAdClicked");
            }

            @Override
            public void onAdDismissed() {
                statu = 0;
                Log.d(TAG, "onAdDismissed");
                hidedlg();
            }

            @Override
            public void onAdRenderFail(int code, String msg) {
                statu = 0;
                Log.d(TAG, "onAdRenderFail " + code + " " + msg);
                BannerAd.id = "";
                BannerAd.pos = "";
                cancel();
            }
        });
    }

}