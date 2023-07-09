package com.cocos.game;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.LinesXFree.nearme.gamecenter.R;
import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.qhhz.cocos.libandroid.Runkit;
import com.qhhz.cocos.libandroid.SplashDialog;

import java.util.Timer;
import java.util.TimerTask;


public class LoginActivity extends Activity implements ApiCallback {
    public static final String TAG = "LoginActivity";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        Runkit.FullScreen(getWindow());
        SplashDialog.Close();
        SplashDialog.Show(this, R.mipmap.ic_launcher);
//        mem_act = AppActivity.get();
        App.get().initGameSDK();

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                login();
            }
        };
        timer.schedule(timerTask, 444);
    }

    public void login() {
        GameCenterSDK.getInstance().doLogin(App.get(), this);
    }

    /**
     * 横竖屏切换
     * demo 为了演示 特意手动更改屏幕方向
     * 媒体根据业务来实际处理
     */
    private void changeScreenOrientation(boolean isHor) {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (isHor) {
                return;
            }
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!isHor) {
                return;
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onSuccess(String resultMsg) {
//        Activity self = this;
        Log.d(TAG, "Login Suc ");
//        changeScreenOrientation(true);
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                // 登录成功
//                Intent intent = new Intent(self, AppActivity.class);
//                self.startActivity(intent);
                finish();
                AppActivity.get().OnLoginSuc();
            }
        };
        timer.schedule(timerTask, 444);
    }

    @Override
    public void onFailure(String resultMsg, int resultCode) {
        // 登录失败
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                login();
            }
        };
        Log.d(TAG, "Login Fail " + resultCode);
        timer.schedule(timerTask, 444);
    }
}