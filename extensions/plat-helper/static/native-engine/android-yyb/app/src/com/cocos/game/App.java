package com.cocos.game;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.cocos.lib.JsbBridgeWrapper;
import com.qhhz.cocos.libandroid.IJSBWrapper;
import com.qhhz.cocos.libandroid.IRunkitAction;
import com.qhhz.cocos.libandroid.OnScriptEventListener;
import com.qhhz.cocos.libandroid.Runkit;

public class App extends Application {
    public static String TAG = "LinesXFree-YYB";

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
                return "LINESXFREE_YYB";
            }

            @Override
            public void onConfirmProtocol() {
                Intent intent = new Intent(_app, AppActivity.class);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                _app.startActivity(intent);
            }

            @Override
            public void ExitGame() {

            }
        });
    }

}
