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

    private int mClickCnt;

    private boolean mIsRet;
    private Handler mTryRenderHandler;

    public WaterflowerTemplateAd(String posId, boolean force, int gravity, boolean debug, String widthMode, int widthDp,
                                 FrameLayout adContainer, FrameLayout waterflowerContainer) {
        mIsRet = false;
        mClickCnt = 0;
        mAdContainer = adContainer;
        mGravity = gravity;
        mDebug = debug;
        mForce = force;
        mWidthMode = widthMode;
        mWidthDp = widthDp;
        mWaterflowerContainer = waterflowerContainer;
        mTemplateAd = new TemplateAd(posId, this);
        mTryRenderHandler = null;
        mTemplateAd.loadTemplateAd(adContainer);
    }

    public void close() {
        Log.d(TAG, "close");
        mClickCnt = 0;
        if(mTryRenderHandler != null) mTryRenderHandler.removeCallbacks(this::tryRenderWaterflower);
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
        mClickCnt = 0;
        AdKit.get().hideTemplate();
        Log.d(TAG, "onError " + mIsRet);
        if (!mIsRet) {
            JSBKit.get().TemplateAdRet("0");
            mIsRet = true;
        }
    }

    @Override
    public void onShow() {
        this.ajustLayout();
    }

    @Override
    public void onClick() {
        AdKit.get().hideTemplate();
        Log.d(TAG, "onClick " + mIsRet);
        if (!mIsRet) {
            JSBKit.get().TemplateAdRet("1");
            mIsRet = true;
        }
    }

    @Override
    public void onDismiss() {
        AdKit.get().hideTemplate();
        mClickCnt = 0;
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
            mTryRenderHandler = new Handler();
            mTryRenderHandler.postDelayed(this::tryRenderWaterflower, 333);
        });
    }

    private void tryRenderWaterflower() {
        Log.d(TAG, "tryRenderWaterflower");
        if(mIsRet) return;
        int adWidth =  mAdContainer.getMeasuredWidth();
        int adHeight =  mAdContainer.getMeasuredHeight();
        if(adWidth != 0 && adHeight != 0) {
            this.renderWaterflower();
        } else {
            mTryRenderHandler = new Handler();
            mTryRenderHandler.postDelayed(this::tryRenderWaterflower, 222);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void renderWaterflower() {
        float[] hsv = new float[]{mForce ? 120f : 200f, 1f, 1f}; // 绿色的 HSV 值，色相为 120，和度和明度都为最大值（1）
        int alpha = mDebug ? 50 : 0; // 透明度，范围从 0 到 255，其中 0 表示完全透明，255 表示完全不透明
        int color = Color.HSVToColor(alpha, hsv);
        WaterflowerTemplateAd self = this;
        AppActivity.get().runOnUiThread(()->{
            FrameLayout.LayoutParams waterParams = (FrameLayout.LayoutParams) mWaterflowerContainer.getLayoutParams();
            Integer adWidth = mAdContainer.getMeasuredWidth();
            Integer adHeight = mAdContainer.getMeasuredHeight();
            waterParams.width = adWidth;
            waterParams.height = adHeight;
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
                        if(mClickCnt == 0){
                            // 居中位置
                            x = adWidth / 2;
                            y = adHeight / 2;
                        }
                        else if(mClickCnt == 1){
                            // 左上
                            x = adWidth / 4;
                            y = adHeight / 4;
                        }
                        else if(mClickCnt == 2){
                            // 右上
                            x = adWidth / 4 * 3;
                            y = adHeight / 4;
                        }
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

                            mClickCnt++;
                            // 一个BUG修复，第一次启动的时候 原生广告的关闭按钮 按了没反应。。。
//                            Handler secCheck = new Handler();
//                            secCheck.postDelayed(()->{
//                                if(!mIsRet) {
//                                    Log.d(TAG,"MagicClose.");
//                                    self.onDismiss();
//                                }
//                            }, 111);
                            break;
                    }
                    return true;
                }
            });
        });
    }

}
