package com.cocos.game;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;


import com.cocos.lib.JsbBridgeWrapper;
import com.qhhz.cocos.libandroid.IJSBWrapper;
import com.qhhz.cocos.libandroid.IRunkitAction;
import com.qhhz.cocos.libandroid.IVoidCallback;
import com.qhhz.cocos.libandroid.OnScriptEventListener;
import com.qhhz.cocos.libandroid.Runkit;
import com.vivo.mobilead.manager.VInitCallback;
import com.vivo.mobilead.manager.VivoAdManager;
import com.vivo.mobilead.model.VAdConfig;
import com.vivo.mobilead.model.VCustomController;
import com.vivo.mobilead.model.VLocation;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.base.annotation.NonNull;
import com.vivo.unionsdk.open.VivoConfigInfo;
import com.vivo.unionsdk.open.VivoUnionSDK;


public class App extends Application {
    private static App _me;

    public static App get() {
        return _me;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _me = this;
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
                JsbBridgeWrapper jbw = JsbBridgeWrapper.getInstance();
                return new IJSBWrapper() {
                    @Override
                    public void addScriptEventListener(String event, OnScriptEventListener listener) {
                        jbw.addScriptEventListener(event, arg -> listener.onScriptEvent(arg));
                    }

                    @Override
                    public void dispatchEventToScript(String event, String arg) {
                        jbw.dispatchEventToScript(event, arg);
                    }

                    @Override
                    public void dispatchEventToScript(String event) {
                        jbw.dispatchEventToScript(event);
                    }
                };
            }

            @Override
            public String getSharedPreferencesKey() {
                return "LINESXFREE_VIVO";
            }

            @Override
            public void onConfirmProtocol() {
                Intent intent = new Intent(_me, AppActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                _me.startActivity(intent);
            }

            @Override
            public void ExitGame(Runnable run) {
                AppActivity.get().ExitGame(run);
            }
        });
    }

    public void initSDK(IVoidCallback initOver) {
        initVivoUnionSDK();
        initVivoAdSDK(initOver);
    }

    protected void initVivoUnionSDK() {
        Boolean isPassPrivacy = Runkit.get().isAgreeProtocol(); //是否已同意隐私协议
        VivoConfigInfo sdkConfig = new VivoConfigInfo();
        sdkConfig.setPassPrivacy(isPassPrivacy);
        VivoUnionSDK.initSdk(this, Constant.APP_ID, false, sdkConfig);
    }

    protected void initVivoAdSDK(IVoidCallback initOver) {
        VAdConfig adConfig = new VAdConfig.Builder()
                .setMediaId(Constant.AD_MEDIA_ID)
//                .setDebug(BuildConfig.DEBUG) //是否开启日志输出
                .setDebug(false) //是否开启日志输出
                .setCustomController(new VCustomController() {
                    @Override
                    public boolean isCanUseLocation() {
//是否允许获取位置信息，默认允许
                        return false;
                    }
                    @Override
                    public VLocation getLocation() {
//若不允许获取位置信息，亦可主动传给 SDK 位置信息
                        return null;
                    }
                    @Override
                    public boolean isCanUsePhoneState() {
//是否允许获取 imei 信息，默认允许
                        return true;
                    }
                    @Override
                    public String getImei() {
//若不允许获取 imei 信息，亦可主动传给 SDK imei 信息
                        return null;
                    }
                    @Override
                    public boolean isCanUseWifiState() {
//是否允许获取网络信息（mac、ip 等），默认允许
                        return true;
                    }
                    @Override
                    public boolean isCanUseWriteExternal() {
//是否允许 SDK 使用公共存储空间
                        return true;
                    }
                    @Override
                    public boolean isCanPersonalRecommend() {
//是否允许推荐个性化广告
                        return true;
                    }
                    @Override
                    public boolean isCanUseImsi() {
                        return true;
                    }
                    @Override
                    public boolean isCanUseApplist() {
                        return true;
                    }
                }).build();
        // 这里完成 SDK 的初始化
        VivoAdManager.getInstance().init(this, adConfig, new VInitCallback() {
            @Override
            public void suceess() {
                Log.d("SDKInit", "suceess");
                initOver.callback();
            }
            @Override
            public void failed(@NonNull VivoAdError adError) {
//若是报超时错误，则检查是否正常出现广告，如无异常，则是正常的
                Log.e("SDKInit", "failed: " + adError.toString());
                initOver.callback();
            }
        });
    }
}
