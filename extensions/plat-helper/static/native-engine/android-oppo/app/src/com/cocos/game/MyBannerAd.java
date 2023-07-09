package com.cocos.game;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.LinesXFree.nearme.gamecenter.R;
import com.heytap.msp.mobad.api.ad.BannerAd;
import com.heytap.msp.mobad.api.listener.IBannerAdListener;
import com.qhhz.cocos.libandroid.QhhzDialog;


public class MyBannerAd extends Dialog implements IBannerAdListener {
    private static String TAG = "MyBannerAd";
    private static String id;
    private static String pos;
    private static long outdateTime;

    public static MyBannerAd instance;

    public static void fetch(String posId, String pos) {
        boolean justReuse = posId.equals(MyBannerAd.id) && pos.equals(MyBannerAd.pos) && MyBannerAd.outdateTime > System.currentTimeMillis();
        Log.d(TAG, posId + ",,," + MyBannerAd.pos);
        Log.d(TAG, System.currentTimeMillis() + "..." + MyBannerAd.outdateTime);
        Log.d(TAG, "justReuse " + justReuse);
        if(justReuse) {
            AppActivity.doRunOnUiThread(() -> {
                if(instance != null && !instance.isShowing()) {
                    if(posId.equals(MyBannerAd.id)) {
                        instance.show();
                    }
                }
            });
        } else {
            MyBannerAd.id = posId;
            MyBannerAd.pos = pos;
            MyBannerAd.outdateTime = System.currentTimeMillis() + 15 * 1000;
            if (instance != null && instance.isShowing()) {
                instance.cancel();
            }
            try {
                AppActivity.doRunOnUiThread(() -> {
                    if(posId.equals(MyBannerAd.id)) {
                        instance = new MyBannerAd(AppActivity.get());
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

    public static void canceldlg() {
        AppActivity.doRunOnUiThread(()->{
            Log.d(TAG, "cancelDlg...");
            if (instance != null) {
                Log.d(TAG, "cancelDlg");
                MyBannerAd.id = "";
                MyBannerAd.pos = "";
                if(instance.mBannerAd != null) {
                    instance.mBannerAd.destroyAd();
                }
                if(instance.mContainer != null) {
                    instance.mContainer.removeAllViews();
                }
                instance.cancel();
                instance = null;
            }
        });
    }

    private BannerAd mBannerAd;
    private ViewGroup mContainer;

    public MyBannerAd(@NonNull Context context) {
        super(context, R.style.Banner);
    }

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return false;
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.banner_dialog);
        Window window = this.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        mContainer = findViewById(R.id.view_ad_container);
        this.setCancelable(false);

        loadBannerAd();
    }

    public void loadBannerAd() {
        if(mBannerAd != null) {
            mBannerAd.destroyAd();
        }
        /**
         * 构造 bannerAd
         */
        mBannerAd = new BannerAd(AppActivity.get(), MyBannerAd.id);
        /**
         * 设置Banner广告行为监听器
         */
        mBannerAd.setAdListener(this);
        /**
         * 获取Banner广告View，将View添加到你的页面上去
         *
         */
        View adView = mBannerAd.getAdView();
        /**
         * mBannerAd.getAdView()返回可能为空，判断后在添加
         */
        if (null != adView) {
            /**
             * 这里addView是可以自己指定Banner广告的放置位置【一般是页面顶部或者底部】
             */
//            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM);
            if(MyBannerAd.pos.equals("bottom")) {
//                Log.d(TAG, "pos bot");
                getWindow().setGravity(Gravity.BOTTOM);
            } else {
//                Log.d(TAG, "pos top");
                getWindow().setGravity(Gravity.TOP);
            }
            mContainer.addView(adView);
//            mContainer.addView(adView, layoutParams);
//            getWindow().addContentView(adView, layoutParams);
        }
        /**
         * 调用loadAd()方法请求广告.
         */
        mBannerAd.loadAd();
    }

    @Override
    public void onAdClose() {
        Log.d(TAG, "onAdClose");
        hidedlg();
    }

    @Override
    public void onAdReady() {
        Log.d(TAG, "onAdReady");
    }

    @Override
    public void onAdClick() {
        Log.d(TAG, "onAdClick");

    }

    @Override
    public void onAdFailed(int i, String s) {
        Log.d(TAG, "Banner广告加载失败，错误码：$i" + ", 错误信息：" + s != null ? s : "");
        MyBannerAd.canceldlg();
    }

    @Override
    public void onAdFailed(String errMsg) {
        Log.d(TAG, "onAdFailed:errMsg=" + null != errMsg ? errMsg : "");
    }

    @Override
    public void onAdShow() {
        Log.d(TAG, "onAdShow");
    }


    //对物理按钮的监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                MyBannerAd.canceldlg();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
