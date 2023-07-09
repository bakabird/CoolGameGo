package com.cocos.game;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.LinesXFree.cocos.R;
import com.vivo.mobilead.unified.banner.UnifiedVivoBannerAd;
import com.vivo.mobilead.unified.banner.UnifiedVivoBannerAdListener;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;

public class BannerAd extends Dialog {
    private static String TAG = "BannerAd";
    private static String id;
    private static String pos;
    private static long outdateTime;

    public static BannerAd instance;

    public static void fetch(String posId, String pos) {
        boolean justReuse = instance != null && posId.equals(BannerAd.id) && pos.equals(BannerAd.pos) && BannerAd.outdateTime > System.currentTimeMillis();
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
                instance.CancelBanner();
                instance = null;
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

    public static void cancelDlg() {
        AppActivity.doRunOnUiThread(()->{
            Log.d(TAG, "cancelDlg...");
            if (instance != null) {
                Log.d(TAG, "cancelDlg");
                instance.CancelBanner();
                instance = null;
            }
        });
    }

    private ViewGroup mContainer;
    private AdParams adParams;
    private Activity act;
    private UnifiedVivoBannerAd vivoBannerAd;

    /**
     * -1 已销毁
     * 0 初始
     * 1 加载中
     * 2 加载完毕
     */
    private int statu = 0;

    public BannerAd(@NonNull Context context) {
        super(context, R.style.Banner);
        AdParams.Builder builder = new AdParams.Builder(BannerAd.id);
        builder.setRefreshIntervalSeconds(30);
        adParams = builder.build();
        statu = 0;
    }

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return false;
    }

    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        BannerAd self = this;
        act = AppActivity.get();
        setContentView(R.layout.banner_dialog);
        Window window = this.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        this.setCancelable(false);
        window.setGravity(Gravity.BOTTOM);
        mContainer = findViewById(R.id.view_ad_container);
        loadExpressAd();
        setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                self.destroyBanner();
                statu = -1;
            }
        });
    }

    public void destroyBanner() {
        mContainer.removeAllViews();
        if (vivoBannerAd != null) {
            Log.d(TAG, "destory mTTAd");
            vivoBannerAd.destroy();
        }
        act = null;
        vivoBannerAd = null;
        mContainer = null;
    }

    private void loadExpressAd() {
        if(statu != 0 ) return;
        statu = 1;
        mContainer.removeAllViews();

        if (vivoBannerAd != null) {
            vivoBannerAd.destroy();
        }
        vivoBannerAd = new UnifiedVivoBannerAd(act, adParams, bannerAdListener);
        vivoBannerAd.loadAd();
    }

    private UnifiedVivoBannerAdListener bannerAdListener = new UnifiedVivoBannerAdListener() {
        @Override
        public void onAdShow() {
            Log.d(TAG, "onAdShow");
        }

        @Override
        public void onAdFailed(@NonNull VivoAdError vivoAdError) {
            Log.d(TAG, "onAdFailed " + vivoAdError.getCode() + " " + vivoAdError.getMsg());
            mContainer.removeAllViews();
            statu = 0;
        }

        @Override
        public void onAdReady(@NonNull View adView) {
            if(statu != 1) return;
            statu = 2;
            Log.d(TAG, "onAdReady");
            //此处即可调用showAd展示广告
            mContainer.removeAllViews();
            mContainer.addView(adView);
        }

        @Override
        public void onAdClick() {
            Log.d(TAG, "onAdClick");
        }

        @Override
        public void onAdClose() {
            Log.d(TAG, "onAdClose");
            CancelBanner();
        }
    };

    public void CancelBanner() {
        if(mContainer != null) {
            mContainer.removeAllViews();
        }
        statu = 0;
        BannerAd.id = "";
        BannerAd.pos = "";
        cancel();
    }

    //对物理按钮的监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                CancelBanner();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }


}