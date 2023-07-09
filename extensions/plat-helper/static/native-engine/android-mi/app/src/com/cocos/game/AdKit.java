package com.cocos.game;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;

import com.LinesXFree.cocos.BuildConfig;
import com.LinesXFree.cocos.R;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.qhhz.cocos.libandroid.PermissionTipDialog;
import com.qhhz.cocos.libandroid.SPStorage;
import com.xiaomi.ad.mediation.internal.config.IMediationConfigInitListener;
import com.xiaomi.ad.mediation.mimonew.MIMOAdSdkConfig;
import com.xiaomi.ad.mediation.mimonew.MiMoNewSdk;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class AdKit {

    private static String TAG = "AdKit";
    private static AdKit _me;

    public static AdKit get() {
        if (_me == null) {
            _me = new AdKit();
        }
        return _me;
    }

    private String mem_userId;
    private int gid;

    private HashMap<String, Pair<Integer,RewardVideoAd>> _rwdAdMap;
    private HashMap<String, Pair<Integer,InsertAd>> _insAdMap;

    public String getUserId() {
        return mem_userId;
    }

    public void setUserId(String uid) {
        mem_userId = uid;
    }


    /**
     * 0 未初始化
     * 1 初始化完毕
     * 2 初始化中
     */
    private int mimoSDKStatu = 0;

    private AdKit() {
        gid = 0;
        _rwdAdMap = new HashMap<String, Pair<Integer,RewardVideoAd>>();
        _insAdMap = new HashMap<String, Pair<Integer,InsertAd>>();
    }

    public void init() {
        Log.d(TAG, "init " + BuildConfig.MI_APP_NAME);
        mimoSDKStatu = 2;

        MiMoNewSdk.init(AppActivity.get(), BuildConfig.MI_APP_ID, BuildConfig.MI_APP_NAME,
                new MIMOAdSdkConfig.Builder()
                        .setDebug(BuildConfig.DEBUG) // true 打开调试，输出调试日志；false 关闭调试
                        .setStaging(false) // true 打开测试请求开关，请求测试广告；false关闭测试请求开关
                        .build(),
                new IMediationConfigInitListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "mediation config init success");
                        mimoSDKStatu = 1;
                    }

                    @Override
                    public void onFailed(int i) {
                        Log.d(TAG, "mediation config init failed:" + i);
                        mimoSDKStatu = 0;
                    }
                }
        );
    }

    public boolean checkAdPermission() {
        Long nextTryTime = Long.parseLong(SPStorage.get().getStr(Constant.SPKEY_NEXT_TRY_PERMISSION, "0"));
        String everShowTip = SPStorage.get().getStr(Constant.SPKEY_EVER_ADTIP_SHOW, "notShow");
        long now = System.currentTimeMillis();
        Activity act = AppActivity.get();
        Long next = now + 1000 * 60 * 60 * 24 * 3;
        if (now > nextTryTime) {
            if (!everShowTip.equals("showed")) {
                act.runOnUiThread(() -> {
                    PermissionTipDialog.Title = "为了提供更好的广告服务，请允许以下权限：";
                    PermissionTipDialog.ContentHtml = "<b>设备电话权限</b><br/>用于获取识别码，以进行广告监测归因、反作弊" +
                            "<br/><b>存储权限</b><br/>用于应用下载广告投放及广告素材存储";
                    PermissionTipDialog.Callback = () -> {
                        SPStorage.get().setStr(Constant.SPKEY_EVER_ADTIP_SHOW, "showed");
                        TTAdSdk.getAdManager().requestPermissionIfNecessary(act);
                        SPStorage.get().setStr(Constant.SPKEY_NEXT_TRY_PERMISSION, next.toString());
                    };
                    PermissionTipDialog dlg = new PermissionTipDialog(AppActivity.get());
                    dlg.show();
                });
            } else {
                TTAdSdk.getAdManager().requestPermissionIfNecessary(act);
                SPStorage.get().setStr(Constant.SPKEY_NEXT_TRY_PERMISSION, next.toString());
            }
            return false;
        }
        return true;
    }

    public void playRwdAd(String posId) {
        if (mimoSDKStatu == 1) {
            if(!_rwdAdMap.containsKey(posId))   {
                _rwdAdMap.put(posId, new Pair<Integer, RewardVideoAd>(gid++, new RewardVideoAd(posId)));
                if(_rwdAdMap.size() > 2) {
                    int min = _rwdAdMap.size();
                    String toRmv = "";
                    for (Iterator<Map.Entry<String, Pair<Integer, RewardVideoAd>>> it = _rwdAdMap.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<String, Pair<Integer, RewardVideoAd>> entry = it.next();
                        if (entry.getValue().first < min) {
                            min = entry.getValue().first;
                            toRmv = entry.getKey();
                        }
                    }
                    if(!toRmv.isEmpty()){
                        _rwdAdMap.get(toRmv).second.Dispose();
                        _rwdAdMap.remove(toRmv);
                    }
                }
            }
            _rwdAdMap.get(posId).second.playAd();
        } else {
            if (mimoSDKStatu == 0) {
                init();
            }
            JSBKit.get().ShowAdRet("-1");
        }
    }

    public void loadRwdAd(String posId) {
        if(!_rwdAdMap.containsKey(posId))   {
            return;
        }
        _rwdAdMap.get(posId).second.requestAd(mem_userId);
    }

    public void playInsertAd(String posId) {
        if (mimoSDKStatu == 1) {
            HashMap<String, Pair<Integer,InsertAd>> map = _insAdMap;
            if(!map.containsKey(posId))   {
                map.put(posId, new Pair<Integer, InsertAd>(gid++, new InsertAd(posId)));
                Log.d(TAG, "New InsertAd");
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
                        map.get(toRmv).second.Dispose();
                        map.remove(toRmv);
                        Log.d(TAG, "Remove InsertAd " + toRmv);
                    }
                }
            }
            map.get(posId).second.playAd();
        } else {
            if (mimoSDKStatu == 0) {
                init();
            }
        }
    }

    public void playBanner(String posId, String pos) {
        BannerAd.fetch(posId, "top");
    }

    public void hideBanner() {
        BannerAd.hidedlg();
    }

}
