package com.cocos.game;

import android.util.Log;
import android.util.Pair;

import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.qhhz.cocos.libandroid.PermissionTipDialog;
import com.qhhz.cocos.libandroid.SPStorage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ADKit {
    private static ADKit _me;

    public static ADKit get() {
        if (_me == null) {
            _me = new ADKit();
        }
        return _me;
    }

    private final String TAG = "ADKit";

    public AppActivity appAct;

    private int gid;
    private boolean isSDKIniting = false;
    private boolean hasTryReqPermission = false;
    private String mEverShowPermissionTip = "";
    private TTAdNative mTTAdNative;
    private HashMap<String, Pair<Integer,RewardAd>> _rwdAdMap;
    private HashMap<String, Pair<Integer,InsertAd>> _insAdMap;

    private ADKit() {
        gid = 0;
        appAct = AppActivity.get();
        _rwdAdMap = new HashMap<String, Pair<Integer,RewardAd>>();
        _insAdMap = new HashMap<String, Pair<Integer,InsertAd>>();
    }

    public TTAdNative getTTAdNative() {
        if (mTTAdNative == null) {
            TTAdManager ttAdManager = TTAdSdk.getAdManager();
            mTTAdNative = ttAdManager.createAdNative(appAct.getApplicationContext());
        }
        return mTTAdNative;
    }

    public boolean isEverShowPermissionTip() {
        if (mEverShowPermissionTip.equals("")) {
            mEverShowPermissionTip = SPStorage.get().getStr(Constant.SPKEY_AD_PERMISSION_TIP, "notShow");
        }
        return mEverShowPermissionTip.equals("showed");
    }

    public void setEverShowPermissionTip(String v) {
        SPStorage.get().setStr(Constant.SPKEY_AD_PERMISSION_TIP, v);
        mEverShowPermissionTip = v;
    }

    public void reqPermission() {
        if (hasTryReqPermission) return;
        TTAdManager ttAdManager = TTAdSdk.getAdManager();
        ttAdManager.requestPermissionIfNecessary(appAct);
        Long nextTime = System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 3;
        SPStorage.get().setStr(Constant.SPKEY_AD_NEXTTRY_REQPERMISSION, nextTime.toString());
        hasTryReqPermission = true;
    }

    public int adCheck() {
        if (this.isSDKIniting) {
            return -102;
        }
        if (!TTAdSdk.isInitSuccess()) {
            this.isSDKIniting = true;
            App.get().initTTAd(suc -> this.onSDKInited(suc));
            return -101;
        }
        Long nextTryTime = Long.parseLong(SPStorage.get().getStr(Constant.SPKEY_AD_NEXTTRY_REQPERMISSION, "0"));
        Log.d(TAG, "now: " + System.currentTimeMillis() + " " + nextTryTime);
        if (System.currentTimeMillis() > nextTryTime) {
            if (!isEverShowPermissionTip()) {
                AppActivity.get().runOnUiThread(() -> {
                    PermissionTipDialog.Title = "为了提供更好的广告服务，请允许以下权限：";
                    PermissionTipDialog.ContentHtml = "<b>设备电话权限</b><br/>用于获取识别码，以进行广告监测归因、反作弊";
                    PermissionTipDialog.Callback = () -> {
                        setEverShowPermissionTip("showed");
                        reqPermission();
                    };
                    PermissionTipDialog dlg = new PermissionTipDialog(AppActivity.get());
                    dlg.show();
                });
            } else {
                reqPermission();
            }
            return -1;
        }
        return 0;
    }

    public void onSDKInited(boolean suc) {
        Log.d(TAG, "ADSDK Inited rlt: " + suc);
        isSDKIniting = false;
    }

    public void playRwdAd(String posId) {
        HashMap<String, Pair<Integer,RewardAd>> map = _rwdAdMap;
        if (!map.containsKey(posId)) {
            map.put(posId, new Pair<Integer, RewardAd>(gid++, new RewardAd(posId, this)));
            Log.d(TAG, "Rwd new： " + posId);
            if(map.size() > 2) {
                int min = map.size();
                String toRmv = "";
                for (Iterator<Map.Entry<String, Pair<Integer, RewardAd>>> it = map.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, Pair<Integer, RewardAd>> entry = it.next();
                    if (entry.getValue().first < min) {
                        min = entry.getValue().first;
                        toRmv = entry.getKey();
                    }
                }
                if(!toRmv.isEmpty()){
                    Log.d(TAG, "Rwd remove: " + toRmv);
                    map.get(toRmv).second.Dispose();
                    map.remove(toRmv);
                }
            }
        }
        map.get(posId).second.playRewardAd();
    }

    public void playInsertAd(String posId) {
        HashMap<String, Pair<Integer,InsertAd>> map = _insAdMap;
        if (!map.containsKey(posId)) {
            map.put(posId, new Pair<Integer, InsertAd>(gid++, new InsertAd(posId, this)));
            Log.d(TAG, "Rwd new： " + posId);
            if(map.size() > 2) {
                int min = map.size();
                String toRmv = "";
                for (Iterator<Map.Entry<String, Pair<Integer, InsertAd>>> it = map.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, Pair<Integer, InsertAd>> entry = it.next();
                    if (entry.getValue().first < min) {
                        min = entry.getValue().first;
                        toRmv = entry.getKey();
                    }
                }
                if(!toRmv.isEmpty()){
                    Log.d(TAG, "Rwd remove: " + toRmv);
                    map.get(toRmv).second.Dispose();
                    map.remove(toRmv);
                }
            }
        }
        map.get(posId).second.play();
    }

    public void playBanner(String posId, String pos) {
        if(this.adCheck() < 0) return;
        BannerAd.fetch(posId, "top");
    }

    public void hideBanner() {
        BannerAd.hidedlg();
    }
}
