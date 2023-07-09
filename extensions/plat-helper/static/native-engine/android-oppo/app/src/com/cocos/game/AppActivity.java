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

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.content.res.Configuration;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.LinesXFree.nearme.gamecenter.BuildConfig;
import com.LinesXFree.nearme.gamecenter.R;
import com.cocos.service.SDKWrapper;
import com.cocos.lib.CocosActivity;
import com.heytap.msp.mobad.api.InitParams;
import com.heytap.msp.mobad.api.MobAdManager;
import com.heytap.msp.mobad.api.listener.IInitListener;
import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.nearme.game.sdk.callback.GameExitCallback;
import com.nearme.game.sdk.common.util.AppUtil;
import com.qhhz.cocos.libandroid.Runkit;
import com.qhhz.cocos.libandroid.SplashDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AppActivity extends CocosActivity {
    private static AppActivity _me;
    private static final int REQUEST_PERMISSIONS_CODE = 42;
    private static final String TAG = "LinesXFree_oppo";
    public static AppActivity get() {
        return _me;
    }

    public static void doRunOnUiThread(Runnable action) {
        _me.runOnUiThread(action);
    }

    private int oriOrientation;
    private int step = 0;
//    private boolean _isAllReady = false;
    private boolean _loginSuc= false;
    private Runnable waitLoginSuc;
    private Runnable mem_onReady;
//    public boolean IsAllReady() {
//        return this._isAllReady;
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        _me = this;
        oriOrientation = getScreenOrientation();
        // DO OTHER INITIALIZATION BELOW
        SDKWrapper.shared().init(this);
        JSBKit.get().build();
        Runkit.FullScreen(getWindow());
        SplashDialog.Show(this, R.mipmap.ic_launcher);
        delay(666, ()->{
            startActivity(new Intent(App.get(), LoginActivity.class));
        });
    }

    public void login() {
        AppActivity self = this;
        GameCenterSDK.getInstance().doLogin(this, new ApiCallback() {
            @Override
            public void onSuccess(String resultMsg) {
                if (waitLoginSuc != null) {
                    waitLoginSuc.run();
                }
                _loginSuc = true;
            }

            @Override
            public void onFailure(String resultMsg, int resultCode) {
                self.delay(666, self::login);
            }
        });
    }

    public void OnLoginSuc() {
        runOnUiThread(()->{
            SplashDialog.Show(this, R.mipmap.ic_launcher);
            if (waitLoginSuc != null) {
                waitLoginSuc.run();
            }
            _loginSuc = true;
        });
    }

    public void delay(long delay, Runnable run) {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                run.run();
            }
        };
        timer.schedule(timerTask, delay);
    }

    public void CheckReady(@Nullable Runnable onReady) {
        if(step == 0) {
            mem_onReady = onReady;
            step = 100;
            if(_loginSuc) {
                CheckReady(null);
            } else  {
                waitLoginSuc = ()->{
                    CheckReady(null);
                };
            }
        } else if(step == 100) {
            step = 200;
            checkAndRequestPermissions();
        } else if(step == 200) {
            step = 300;
            initModAd();
        } else if(step == 300) {
            SplashDialog.Close();
            mem_onReady.run();
        }
    }

    public void initModAd() {
        /*
         * 获取权限成功之后需初始化，可解决第一次打开应用未获取权限导致SDK初始化失败的问题
         */
        InitParams initParams = new InitParams.Builder()
//                .setDebug(true)//true打开SDK日志，当应用发布Release版本时，必须注释掉这行代码的调用，或者设为false
                .build();
        /**
         * 调用这行代码初始化广告SDK
         */
        MobAdManager.getInstance().init(this, BuildConfig.OPPO_APP_ID, initParams, new IInitListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "IInitListener onSuccess");
                /**
                 * 应用已经获得SDK运行必须的READ_PHONE_STATE权限，直接请求广告。
                 */
//                fetchSplashAd();
                CheckReady(null);
            }

            @Override
            public void onFailed(String reason) {
                Log.d(TAG, "IInitListener onFailed");
            }
        });
    }

    public void onPermissionsFetched() {
        CheckReady(null);
    }

    private List<String> mNeedRequestPMSList = new ArrayList<String>();

    /**
     * 申请SDK运行需要的权限
     * 注意：READ_PHONE_STATE权限是必须权限，没有这个权限SDK无法正常获得广告。
     * WRITE_EXTERNAL_STORAGE 、ACCESS_FINE_LOCATION 是可选权限；没有不影响SDK获取广告；但是如果应用申请到该权限，会显著提升应用的广告收益。
     */
    public void checkAndRequestPermissions() {
        /**
         * Android Q以下READ_PHONE_STATE 权限是必须权限，没有这个权限SDK无法正常获得广告。
         */
        if (Build.VERSION.SDK_INT < 29 && PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            mNeedRequestPMSList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (0 == mNeedRequestPMSList.size()) {
            /**
             * 权限都已经有了，那么直接调用SDK请求广告。
             */
            onPermissionsFetched();
//            fetchSplashAd();
        } else {
            /**
             * 有权限需要申请，主动申请。
             */
            String[] temp = new String[mNeedRequestPMSList.size()];
            mNeedRequestPMSList.toArray(temp);
            ActivityCompat.requestPermissions(this, temp, REQUEST_PERMISSIONS_CODE);
        }
    }

    public void ExitGame(Runnable onExit) {
        Activity self = this;
        MyBannerAd.canceldlg();
        GameCenterSDK.getInstance().onExit(self,new GameExitCallback() {
            @Override
            public void exitGame() {
                onExit.run();
                // CP 实现游戏退出操作，也可以直接调用
                // AppUtil工具类里面的实现直接强杀进程~
                AppUtil.exitGameProcess(self);
            }
        });
    }

    /**
     * 处理权限申请的结果
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            /**
             *处理SDK申请权限的结果。
             */
            case REQUEST_PERMISSIONS_CODE:
                if (hasNecessaryPMSGranted()) {
                    onPermissionsFetched();
                } else {
                    /**
                     * 如果用户没有授权，那么应该说明意图，引导用户去设置里面授权。
                     */
                    Toast.makeText(this, "应用缺少SDK运行必须的READ_PHONE_STATE权限！请点击\"应用权限\"，打开所需要的权限。", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 判断应用是否已经获得SDK运行必须的READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE两个权限。
     *
     * @return
     */
    private boolean hasNecessaryPMSGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            return true;
        }
        return false;
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "down " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Runkit.get().ExitGame();
                break;
        }
        return super.onKeyDown(keyCode, event);
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

    /**
     * 横竖屏切换
     * demo 为了演示 特意手动更改屏幕方向
     * 媒体根据业务来实际处理
     */
    private void setScreenOrientation(int orientation) {
        int currentOrientation = getRequestedOrientation();
        if (currentOrientation != orientation) {
            setRequestedOrientation(orientation);
        }
    }

    private int getScreenOrientation() {
        return getRequestedOrientation();
    }
}
