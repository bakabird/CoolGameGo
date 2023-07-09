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

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.LinesXFree.cocos.R;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.cocos.lib.CocosActivity;
import com.cocos.service.SDKWrapper;
import com.qhhz.cocos.libandroid.IVoidCallback;
import com.qhhz.cocos.libandroid.Runkit;
import com.qhhz.cocos.libandroid.SplashDialog;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnExitListner;

public class AppActivity extends CocosActivity {
    private static AppActivity _me;

    public static AppActivity get() {
        return _me;
    }

    public static void doRunOnUiThread(Runnable action) {
        _me.runOnUiThread(action);
    }

    public final static String TAG = "AppActivity";

    private boolean mem_PlatReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _me = this;
        // DO OTHER INITIALIZATION BELOW
        SDKWrapper.shared().init(this);
        JSBKit.get().build();
        SplashDialog.Show(this, R.mipmap.ic_launcher);
    }

    public void checkReady(IVoidCallback onRdy) {
        if (mem_PlatReady) {
            onRdy.callback();
            return;
        }
        SplashDialog.Close();
        MiCommplatform.getInstance().onUserAgreed(App.get());
        AdKit.get().init();
        onRdy.callback();
        mem_PlatReady = true;
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

    public boolean isPortrait() {
        return getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT;
    }

    public void ExitGame(Runnable run) {
        MiCommplatform.getInstance().miAppExit(AppActivity.get(), new OnExitListner() {
            @Override
            public void onExit(int code) {
                if (code == MiErrorCode.MI_XIAOMI_EXIT) {
                    run.run();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }
        });
    }
}


//        try {
//                TTAdSdk.getAdManager().requestPermissionIfNecessary(this);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                //判断该应用是否有写SD卡权限，如果没有再去申请
//                if (ContextCompat.checkSelfPermission(this, Manifest.permission
//                .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.READ_PHONE_STATE}, 100);
//                }
//                }
//
//                int permissionLocation = ActivityCompat
//                .checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
//                if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
//                String[] location = {Manifest.permission.ACCESS_FINE_LOCATION};
//                ActivityCompat.requestPermissions(this, location, 1);
//                }
//                int permissionPhone = ActivityCompat
//                .checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
//                if (permissionPhone != PackageManager.PERMISSION_GRANTED) {
//                String[] phoneState = {Manifest.permission.READ_PHONE_STATE};
//                ActivityCompat.requestPermissions(this, phoneState, 1);
//                }
//                } catch (Exception e) {
//                e.printStackTrace();
//                }