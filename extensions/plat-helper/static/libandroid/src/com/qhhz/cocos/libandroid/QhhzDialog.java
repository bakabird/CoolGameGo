package com.qhhz.cocos.libandroid;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;

/**
 * 前海合众弹窗基类，处理一些通用事务
 */
public class QhhzDialog extends Dialog {


    public QhhzDialog(Context context) {
        super(context);
    }

    public QhhzDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected QhhzDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    //对物理按钮的监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
//            case KeyEvent.KEYCODE_MENU:
//                break;
            case KeyEvent.KEYCODE_BACK:
                Runkit.get().ExitGame();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

}
