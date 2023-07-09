package com.cocos.game;

import com.vivo.unionsdk.open.VivoExitCallback;

public class CustomExitCallback implements VivoExitCallback {
    private Runnable _run;

    public CustomExitCallback(Runnable run) {
        this._run = run;
    }

    @Override
    public void onExitCancel() {

    }

    @Override
    public void onExitConfirm() {
        AppActivity.doRunOnUiThread(()-> {
            this._run.run();
            android.os.Process.killProcess(android.os.Process.myPid());
        });
    }
}
