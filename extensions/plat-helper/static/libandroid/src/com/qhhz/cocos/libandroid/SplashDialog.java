package com.qhhz.cocos.libandroid;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.qhhz.cocos.libandroid.R;

public class SplashDialog extends Dialog {
    private static final String TAG = "SplashActivity"; // 延迟3秒
    private static SplashDialog me;
    private static int IMGRES;

    public static void Show(Context ctx, int imageResId) {
        SplashDialog.IMGRES = imageResId;
        SplashDialog dialog = new SplashDialog(ctx);
        dialog.show();
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
    private TextView mTextLoading;

    public SplashDialog(Context context) {
        super(context,  R.style.SplashDialog);
        this.setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_splash);
        getWindow().setBackgroundDrawableResource(R.color.gray_background);
// d
        mImageLoading = findViewById(R.id.iv_loading);
//        mTextLoading = findViewById(R.id.tv_loading_text);
//
//        // 开启动画
        mImageLoading.animate().alpha(1.0f).setDuration(800);
        mImageLoading.setImageResource(IMGRES);
        Runkit.FullScreen(getWindow());
//
//        mTextLoading.setText("游戏启动中"); // 设置加载文字

        SplashDialog.me = this;
    }
}
