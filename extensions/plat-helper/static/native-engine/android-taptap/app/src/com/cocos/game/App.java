package com.cocos.game;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTCustomController;
import com.cocos.lib.JsbBridgeWrapper;
import com.qhhz.LinesXFree.taptap.BuildConfig;
import com.qhhz.cocos.libandroid.IJSBWrapper;
import com.qhhz.cocos.libandroid.IRunkitAction;
import com.qhhz.cocos.libandroid.OnScriptEventListener;
import com.qhhz.cocos.libandroid.Runkit;

interface OnTTAdInited {
    void onRlt(boolean suc);
}

public class App extends Application {
    public static String TAG = "LinesXFree";

    private static App _app = null;

    public static App get() {
        return _app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _app = this;
        Runkit.get().Init(new IRunkitAction() {
            @Override
            public Application getApp() {
                return App.get();
            }

            @Override
            public Activity getAppActivity() {
                return AppActivity.get();
            }

            @Override
            public IJSBWrapper getJSBWrapper() {
                return new IJSBWrapper() {
                    @Override
                    public void addScriptEventListener(String event, OnScriptEventListener listener) {
                        JsbBridgeWrapper.getInstance().addScriptEventListener(event, (arg) -> {
                            listener.onScriptEvent(arg);
                        });
                    }

                    @Override
                    public void dispatchEventToScript(String event, String arg) {
                        JsbBridgeWrapper.getInstance().dispatchEventToScript(event, arg);
                    }

                    @Override
                    public void dispatchEventToScript(String event) {
                        JsbBridgeWrapper.getInstance().dispatchEventToScript(event);
                    }
                };
            }

            @Override
            public String getSharedPreferencesKey() {
                return "LINESXFREE_TAPTAP";
            }

            @Override
            public void onConfirmProtocol() {
                Intent intent = new Intent(_app, AppActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                _app.startActivity(intent);
            }

            @Override
            public void ExitGame(Runnable onExit) {
                AppActivity.get().ExitGame(onExit);
            }
        });
    }

    public void initTTAd(OnTTAdInited cb) {
        //强烈建议在应用对应的Application#onCreate()方法中调用，避免出现content为null的异常
        TTAdConfig config = new TTAdConfig.Builder().appId(Constant.APP_ID)//xxxxxxx为穿山甲媒体平台注册的应用ID
                .useTextureView(true) //默认使用SurfaceView播放视频广告,当有SurfaceView冲突的场景，可以使用TextureView
                .appName(Constant.APP_NAME).titleBarTheme(TTAdConstant.TITLE_BAR_THEME_DARK)//落地页主题
                .allowShowNotify(true) //是否允许sdk展示通知栏提示,若设置为false则会导致通知栏不显示下载进度
                .debug(BuildConfig.DEBUG) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI) //允许直接下载的网络状态集合,没有设置的网络下点击下载apk会有二次确认弹窗，弹窗中会披露应用信息
                .supportMultiProcess(false) //是否支持多进程，true支持
                .customController(new TTCustomController() {
                    @Override
                    public boolean isCanUseLocation() {
                        return false;
                    }

                    @Override
                    public boolean isCanUseWriteExternal() {
                        return false;
                    }

                    @Override
                    public boolean alist() {
                        return false;
                    }

                    @Override
                    public boolean isCanUsePermissionRecordAudio() {
                        return false;
                    }
                }).build();
        TTAdSdk.init(this, config, new TTAdSdk.InitCallback() {
            @Override
            public void success() {
                Log.d(TAG, "initTTAd suc");
                cb.onRlt(true);
            }

            @Override
            public void fail(int i, String s) {
                Log.d(TAG, "initTTAd fail " + i + " " + s);
                cb.onRlt(false);
            }
        });
    }
}
