package com.qhhz.cocos.libandroid;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class SplashDialog extends Dialog {
    private static final String TAG = "SplashActivity"; // 延迟3秒
    private static SplashDialog me;
    private static int IMGRES;

    public static void Show(Context ctx, int imageResId) {
        SplashDialog.IMGRES = imageResId;
        SplashDialog dialog = new SplashDialog(ctx);
        dialog.show();
        dialog.withIcon();
    }

    public static void ShowWithAd(Context ctx, ISplashAdCallback callback) {
        SplashDialog dialog = new SplashDialog(ctx);
        dialog.show();
        dialog.withAd();
        callback.reqAd(dialog.mSplashBox);
    }

    public static void Close() {
        if(me != null) {
            me.dismiss();
            Log.d(TAG, "finished");
        } else {
            Log.d(TAG, "isNull");
        }
    }

    private ImageView mImageLoading;
    private ViewGroup mSplashBox;

    public SplashDialog(Context context) {
        super(context,  R.style.SplashDialog);
        this.setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_splash);
        getWindow().setBackgroundDrawableResource(R.color.gray_background);
        mImageLoading = findViewById(R.id.iv_loading);
        mSplashBox = findViewById(R.id.splash_container);
        Runkit.FullScreen(getWindow());
        SplashDialog.me = this;
    }

    public void withAd() {
        mSplashBox.setVisibility(View.VISIBLE);
        mImageLoading.setVisibility(View.GONE);
    }

    public void withIcon() {
        mSplashBox.setVisibility(View.GONE);
        mImageLoading.setVisibility(View.VISIBLE);
        mImageLoading.animate().alpha(1.0f).setDuration(800);
        mImageLoading.setImageResource(IMGRES);
    }
}
