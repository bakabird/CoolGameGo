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
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.LinesXFree.cocos.R;
import com.cocos.lib.CocosActivity;
import com.cocos.service.SDKWrapper;
import com.qhhz.cocos.libandroid.Runkit;
import com.qhhz.cocos.libandroid.SplashDialog;
import com.vivo.unionsdk.open.VivoUnionSDK;

public class AppActivity extends CocosActivity {
    public static String TAG = "VivoAppActivity";

    private static AppActivity _me;

    private String m_usernamel;
    private String m_openid;
    private String m_authToken;

    private FrameLayout mTemplateParentLayout = null;

    private FrameLayout mWaterFlowerLayout = null;

    public FrameLayout getTemplateParentLayout() {
        return mTemplateParentLayout;
    }

    public FrameLayout getWaterFlowerLayout() {
        return mWaterFlowerLayout;
    }

    public static AppActivity get() {
        return _me;
    }

    public static void doRunOnUiThread(Runnable action) {
        _me.runOnUiThread(action);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _me = this;
        // DO OTHER INITIALIZATION BELOW
        SDKWrapper.shared().init(this);
        JSBKit.get().build();
        initTemplateLayout();
        initWaterFlowerLayout();
        splashOpen();
    }

    private void initTemplateLayout() {
        mTemplateParentLayout =new FrameLayout(this);
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL; //上下的位置信息
        this.addContentView(mTemplateParentLayout, containerParams);
    }

    private void initWaterFlowerLayout() {
        mWaterFlowerLayout =new FrameLayout(this);
        FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL; //上下的位置信息
        this.addContentView(mWaterFlowerLayout, containerParams);
    }

    private void splashOpen() {
        SplashDialog.Show(this, R.mipmap.ic_launcher);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "down " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Runkit.get().ExitGame();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void ExitGame(Runnable run) {
        BannerAd.cancelDlg();
        AppActivity.get().runOnUiThread(() -> {
            VivoUnionSDK.exit(AppActivity.get(), new CustomExitCallback(run));
        });
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

    public String getM_usernamel() {
        return m_usernamel;
    }

    public void setM_usernamel(String m_usernamel) {
        this.m_usernamel = m_usernamel;
    }

    public String getM_openid() {
        return m_openid;
    }

    public void setM_openid(String m_openid) {
        this.m_openid = m_openid;
    }

    public String getM_authToken() {
        return m_authToken;
    }

    public void setM_authToken(String m_authToken) {
        this.m_authToken = m_authToken;
    }

    public int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }
}
