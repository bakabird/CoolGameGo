package com.cocos.game;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;

import com.bytedance.sdk.openadsdk.TTDislikeDialogAbstract;
import com.qhhz.LinesXFree.taptap.R;
import com.qhhz.cocos.libandroid.Runkit;
import com.qhhz.cocos.libandroid.SplashDialog;

public class DislikeDialog extends TTDislikeDialogAbstract {
    private Runnable mOnClose;


    public DislikeDialog(Context context, Runnable onClose) {
        super(context);
        mOnClose = onClose;
    }

    public DislikeDialog(Context context, int i) {
        super(context, i);
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        autoClose();
//    }

//    private void autoClose() {
//        new Handler().postDelayed(()->{
//            if (this.isShowing()) {
//                this.dismiss();
//            }
//        }, 10);
//    }

    @Override
    public void show() {
        super.show();
        mOnClose.run();
        this.dismiss();
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_blank;
    }

    @Override
    public int[] getTTDislikeListViewIds() {
        return new int[0];
    }

    @Override
    public ViewGroup.LayoutParams getLayoutParams() {
        return null;
    }
}
