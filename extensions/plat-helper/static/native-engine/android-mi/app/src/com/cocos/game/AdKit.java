package com.cocos.game;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.LinesXFree.cocos.BuildConfig;
//import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.qhhz.cocos.libandroid.PermissionTipDialog;
import com.qhhz.cocos.libandroid.SPStorage;
import com.xiaomi.ad.common.util.MLog;
import com.xiaomi.ad.mediation.MMAdConfig;
import com.xiaomi.ad.mediation.MMAdError;
import com.xiaomi.ad.mediation.internal.config.IMediationConfigInitListener;
import com.xiaomi.ad.mediation.mimonew.MIMOAdSdkConfig;
import com.xiaomi.ad.mediation.mimonew.MiMoNewSdk;
import com.xiaomi.ad.mediation.splashad.MMAdSplash;

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
    /**
     * 0 未初始化
     * 1 初始化完毕
     * 2 初始化中
     */
    private int mimoSDKStatu = 0;
    private WaterflowerTemplateAd lastWaterflower;

    public String getUserId() {
        return mem_userId;
    }

    public void setUserId(String uid) {
        mem_userId = uid;
    }


    private AdKit() {
        gid = 0;
        _rwdAdMap = new HashMap<String, Pair<Integer,RewardVideoAd>>();
        _insAdMap = new HashMap<String, Pair<Integer,InsertAd>>();
    }

    public void init(@Nullable Runnable onSuc) {
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
                        if (onSuc != null) {
                            onSuc.run();
                        }
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
//        Long nextTryTime = Long.parseLong(SPStorage.get().getStr(Constant.SPKEY_NEXT_TRY_PERMISSION, "0"));
//        String everShowTip = SPStorage.get().getStr(Constant.SPKEY_EVER_ADTIP_SHOW, "notShow");
//        long now = System.currentTimeMillis();
//        Activity act = AppActivity.get();
//        Long next = now + 1000 * 60 * 60 * 24 * 3;
//        if (now > nextTryTime) {
//            if (!everShowTip.equals("showed")) {
//                act.runOnUiThread(() -> {
//                    PermissionTipDialog.Title = "为了提供更好的广告服务，请允许以下权限：";
//                    PermissionTipDialog.ContentHtml = "<b>设备电话权限</b><br/>用于获取识别码，以进行广告监测归因、反作弊" +
//                            "<br/><b>存储权限</b><br/>用于应用下载广告投放及广告素材存储";
//                    PermissionTipDialog.Callback = () -> {
//                        SPStorage.get().setStr(Constant.SPKEY_EVER_ADTIP_SHOW, "showed");
//                        TTAdSdk.getAdManager().requestPermissionIfNecessary(act);
//                        SPStorage.get().setStr(Constant.SPKEY_NEXT_TRY_PERMISSION, next.toString());
//                    };
//                    PermissionTipDialog dlg = new PermissionTipDialog(AppActivity.get());
//                    dlg.show();
//                });
//            } else {
//                TTAdSdk.getAdManager().requestPermissionIfNecessary(act);
//                SPStorage.get().setStr(Constant.SPKEY_NEXT_TRY_PERMISSION, next.toString());
//            }
//            return false;
//        }
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
                init(null);
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
                init(null);
            }
            JSBKit.get().InsertAdRet("0");
        }
    }

    public void playBanner(String posId, String pos) {
        BannerAd.fetch(posId, "top");
    }

    public void playSplash(ViewGroup container, String upId, Runnable onOver) {
        Log.d(TAG,"playSplash");
        Activity act = AppActivity.get();
        final boolean[] isOver = {false};
        MMAdConfig adConfig = new MMAdConfig();
        adConfig.supportDeeplink = true;
        adConfig.imageHeight = 1920;
        adConfig.imageWidth = 1080;
        adConfig.setSplashActivity(act);
        adConfig.setSplashContainer(container);
        MMAdSplash mAdSplash = new MMAdSplash(act, upId);
        mAdSplash.onCreate();// 必须调用
        mAdSplash.load(adConfig, new MMAdSplash.SplashAdInteractionListener() {
            @Override
            public void onAdSkip() {
//点击跳过
                Log.d(TAG, "onAdSkip");
            }
            @Override
            public void onError(MMAdError error) {
//展示出现异常
                Log.d(TAG, "onError");
                Log.e(TAG, error.errorMessage + " " + error.errorCode);
                if(!isOver[0]) {
                    onOver.run();
                    isOver[0] = true;
                }
            }
            @Override
            public void onAdShow() {
//广告展示
                Log.d(TAG, "onAdShow");
            }
            @Override
            public void onAdClicked() {
//广告点击
                Log.d(TAG, "onAdClicked");
            }
            @Override
            public void onAdDismissed() {
//广告消失
                Log.d(TAG, "onAdDismissed");
                if(!isOver[0]) {
                    onOver.run();
                    isOver[0] = true;
                }
            }
        });
    }

    public void hideBanner() {
        BannerAd.hidedlg();
    }

    public void playTemplate(String posId, boolean force, int gravity, boolean debug, String widthMode, int widthDp) {
        if (lastWaterflower == null) {
            lastWaterflower = new WaterflowerTemplateAd(posId, force, gravity, debug, widthMode, widthDp,
                    AppActivity.get().getTemplateParentLayout(), AppActivity.get().getWaterFlowerLayout());
        }
    }

    public void hideTemplate() {
        if (lastWaterflower != null) {
            lastWaterflower.close();
            lastWaterflower = null;
        }
    }

}
