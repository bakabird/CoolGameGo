package com.qhhz.cocos.libandroid;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreference Storage
 */
public class SPStorage {
    private static SPStorage _me;
    public static String ProtocolAgreementKey = "__LIBAND__agree_protocol";

    public static SPStorage get() {
        if (_me == null) {
            _me = new SPStorage();
        }
        return _me;
    }

    private SharedPreferences mem_sp;

    private SPStorage() {
        mem_sp = Runkit.get().app().getSharedPreferences(Runkit.get().spKey(), Context.MODE_PRIVATE);
    }

    public String getStr(String key, String defV) {
        return mem_sp.getString(key, defV);
    }

    public void setStr(String key, String val) {
        mem_sp.edit().putString(key, val).apply();
    }
}
