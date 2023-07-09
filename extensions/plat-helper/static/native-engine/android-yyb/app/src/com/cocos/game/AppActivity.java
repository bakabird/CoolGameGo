/****************************************************************************
 Copyright (c) 2015-2016 Chukong Technologies Inc.
 Copyright (c) 2017-2018 Xiamen Yaji Software Co., Ltd.

 http://www.cocos2d-x.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
package com.cocos.game;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.cocos.lib.CocosActivity;
import com.cocos.service.SDKWrapper;
//import com.qhhz.LinesXFree.taptap.BuildConfig;
//import com.qhhz.LinesXFree.taptap.R;
import com.qhhz.LinesXFree.yyb.R;
import com.qhhz.cocos.libandroid.SplashDialog;
//import com.tapsdk.antiaddiction.Config;
//import com.tapsdk.antiaddiction.constants.Constants;
//import com.tapsdk.antiaddictionui.AntiAddictionUICallback;
//import com.tapsdk.antiaddictionui.AntiAddictionUIKit;

import java.util.Map;

public class AppActivity extends CocosActivity {

    private static AppActivity _me;

    public static AppActivity get() {
        return _me;
    }

    public static void doRunOnUiThread(Runnable action) {
        _me.runOnUiThread(action);
    }

    // 0 未初始化
    // 1 广告初始化完毕
    // ...
    // 100 初始化完毕
    private int mem_initStep = 0;

    public ViewGroup bannerBox;

    public boolean IsAllReady() {
        return mem_initStep >= 100;
    }

    public boolean isPortatil() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _me = this;
        super.onCreate(savedInstanceState);
        SplashDialog.Show(this, R.mipmap.ic_launcher);
        // DO OTHER INITIALIZATION BELOW
        SDKWrapper.shared().init(this);
        JSBKit.get().build();

//        bannerBox = new FrameLayout(this);
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//        params.gravity = Gravity.BOTTOM;
//        addContentView(bannerBox, params);
    }

    public void checkPermissionAndInit(boolean inc) {
        if (inc) mem_initStep += 1;
        if (mem_initStep == 0) {
            JSBKit.get().CheckPlatReadyRet("");
//            App.get().initTTAd(suc -> {
//                AppActivity.get().checkPermissionAndInit(true);
//            });
        }
//        } else {
//            mem_initStep = 100;
//            JSBKit.get().CheckPlatReadyRet("");
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKWrapper.shared().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SDKWrapper.shared().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Workaround in https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508
        if (!isTaskRoot()) {
            return;
        }
        SDKWrapper.shared().onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDKWrapper.shared().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SDKWrapper.shared().onNewIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SDKWrapper.shared().onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SDKWrapper.shared().onStop();
    }

    @Override
    public void onBackPressed() {
        SDKWrapper.shared().onBackPressed();
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SDKWrapper.shared().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        SDKWrapper.shared().onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SDKWrapper.shared().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        SDKWrapper.shared().onStart();
        super.onStart();
    }

    @Override
    public void onLowMemory() {
        SDKWrapper.shared().onLowMemory();
        super.onLowMemory();
    }
}
