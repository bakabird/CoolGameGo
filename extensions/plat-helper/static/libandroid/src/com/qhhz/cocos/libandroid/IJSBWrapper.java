package com.qhhz.cocos.libandroid;

public interface IJSBWrapper {
    void addScriptEventListener(String event, OnScriptEventListener listener);

    void dispatchEventToScript(String event, String arg);

    void dispatchEventToScript(String event);
}
