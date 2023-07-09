package com.cocos.game;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.cocos.lib.JsbBridgeWrapper;
import com.qhhz.cocos.libandroid.IJSBWrapper;
import com.qhhz.cocos.libandroid.IRunkitAction;
import com.qhhz.cocos.libandroid.OnScriptEventListener;
import com.qhhz.cocos.libandroid.Runkit;

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
                return "LINESXFREE_MI";
            }


            @Override
            public void ExitGame(Runnable run) {
                AppActivity.get().ExitGame(run);
            }

            @Override
            public void onConfirmProtocol() {
                Intent intent = new Intent(_me, AppActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                _me.startActivity(intent);
            }
        });
    }
}
