package com.cocos.game;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;


public class WaterflowerTemplateAd implements ITemplateAdListenner {
    public static final String TAG = "WaterflowerTemplateAd";

    private final TemplateAd mTemplateAd;
    private final FrameLayout mAdContainer;
    private final FrameLayout mWaterflowerContainer;
    private final int mGravity;
    private final boolean mDebug;
    private final boolean mForce;
    private final String mWidthMode;
    private final int mWidthDp;

    private boolean mIsRet;

    public WaterflowerTemplateAd(String posId, boolean force, int gravity, boolean debug, String widthMode, int widthDp,
                                 FrameLayout adContainer, FrameLayout waterflowerContainer) {
        mIsRet = false;
        mAdContainer = adContainer;
        mGravity = gravity;
        mDebug = debug;
        mForce = force;
        mWidthMode = widthMode;
        mWidthDp = widthDp;
        mWaterflowerContainer = waterflowerContainer;
        mTemplateAd = new TemplateAd(AppActivity.get(), adContainer, posId, widthDp, this);
        AppActivity.doRunOnUiThread(()->{
            mTemplateAd.loadAd();
        });
    }

    public void close() {
        Log.d(TAG, "close");
        AppActivity.get().runOnUiThread(()->{
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mWaterflowerContainer.getLayoutParams();
            params.height = 0;
            mWaterflowerContainer.setLayoutParams(params);
            mTemplateAd.destory();
            mAdContainer.removeAllViews();
            mWaterflowerContainer.removeAllViews();
        });
    }

    @Override
    public void onError(String err) {
        ADKit.get().hideTemplate();
        Log.d(TAG, "onError " + mIsRet);
        if (!mIsRet) {
            JSBKit.get().TemplateAdRet(err.equals("-2") ? "-2" : "0");
            mIsRet = true;
        }
    }

    @Override
    public void onShow() {
        this.ajustLayout();
    }

    @Override
    public void onClick() {
        ADKit.get().hideTemplate();
        Log.d(TAG, "onClick " + mIsRet);
        if (!mIsRet) {
            JSBKit.get().TemplateAdRet("1");
            mIsRet = true;
        }
    }

    @Override
    public void onDismiss() {
        ADKit.get().hideTemplate();
        Log.d(TAG, "onDismiss " + mIsRet);
        if (!mIsRet) {
            JSBKit.get().TemplateAdRet("1");
            mIsRet = true;
        }
    }

    private void ajustLayout() {
        AppActivity.get().runOnUiThread(()->{
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mAdContainer.getLayoutParams();
            if (mWidthMode.equals("Dip")) {
                params.width = AppActivity.get().dpToPx(mWidthDp);
            } else if (mWidthMode.equals("MatchParent")){
                params.width = FrameLayout.LayoutParams.MATCH_PARENT;
            } else if(mWidthMode.equals("WrapContent")) {
                params.width = FrameLayout.LayoutParams.WRAP_CONTENT;
            }
            params.gravity = mGravity;
            mAdContainer.setLayoutParams(params);
            Handler handler = new Handler();
            handler.postDelayed(this::renderWaterflower, 666);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void renderWaterflower() {
        float[] hsv = new float[]{120f, 1f, 1f}; // 绿色的 HSV 值，色相为 120，和度和明度都为最大值（1）
        int alpha = mDebug ? 50 : 0; // 透明度，范围从 0 到 255，其中 0 表示完全透明，255 表示完全不透明
        int color = Color.HSVToColor(alpha, hsv);
        AppActivity.get().runOnUiThread(()->{
            FrameLayout.LayoutParams waterParams = (FrameLayout.LayoutParams) mWaterflowerContainer.getLayoutParams();
            Integer adWidth = mAdContainer.getMeasuredWidth();
            waterParams.width = adWidth;
            waterParams.height = mAdContainer.getMeasuredHeight();
            waterParams.gravity = mGravity;
//            Log.d(TAG, "AAAA " + layout.getMeasuredWidth() + "   " + layout.getMeasuredHeight() );
//            Log.d(TAG, "BBBB " + layout.getWidth() + "   " + layout.getHeight() );
            mWaterflowerContainer.setLayoutParams(waterParams);
            mWaterflowerContainer.setBackgroundColor(color);
            mWaterflowerContainer.setClickable(true);
            mWaterflowerContainer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    if (mForce) {
                        x = Math.min(adWidth / 4, x);
                        event.setLocation(x, y);
                    }
                    Log.d(TAG, "Touch " + x + " " + y);

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // 手指按下时的操作
                            mAdContainer.dispatchTouchEvent(event);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            // 手指移动时的操作
                            break;
                        case MotionEvent.ACTION_UP:
                            // 手指抬起时操作
                            // 将触摸事件转发目标按钮
                            mAdContainer.dispatchTouchEvent(event);
                            break;
                    }
                    return true;
                }
            });
        });
    }
}
