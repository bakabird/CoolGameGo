package com.cocos.game;

import android.util.Log;
import android.util.Pair;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AdKit {
    private static AdKit _me;

    public static AdKit get() {
        if (_me == null) {
            _me = new AdKit();
        }
        return _me;
    }

    private final String TAG = "AdKit";

    private int gid;
    private AppActivity appAct;
    private boolean hasTryReqPermission = false;
    private String mEverShowPermissionTip = "";
    private HashMap<String, Pair<Integer,RewardAdHandler>> _rwdAdMap;
    private HashMap<String, Pair<Integer,InsertAdHandler>> _insAdMap;

    private AdKit() {
        gid = 0;
        appAct = AppActivity.get();
        _rwdAdMap = new HashMap<String, Pair<Integer,RewardAdHandler>>();
        _insAdMap = new HashMap<String, Pair<Integer,InsertAdHandler>>();
    }


    public void playRwdAd(String posId) {
        Log.d(TAG, "playRwdAd " + posId);
        HashMap<String, Pair<Integer,RewardAdHandler>> map = _rwdAdMap;
        if (!map.containsKey(posId)) {
            map.put(posId, new Pair<Integer, RewardAdHandler>(gid++, new RewardAdHandler(posId)));
            Log.d(TAG, "Rwd new： " + posId);
            if(map.size() > 2) {
                int min = map.size();
                String toRmv = "";
                for (Iterator<Map.Entry<String, Pair<Integer, RewardAdHandler>>> it = map.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, Pair<Integer, RewardAdHandler>> entry = it.next();
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
        map.get(posId).second.playAd();
    }

    public void playInsertAd(String posId) {
        Log.d(TAG, "playInsertAd " + posId);
        HashMap<String, Pair<Integer,InsertAdHandler>> map = _insAdMap;
        if (!map.containsKey(posId)) {
            map.put(posId, new Pair<Integer, InsertAdHandler>(gid++, new InsertAdHandler(posId)));
            Log.d(TAG, "Rwd new： " + posId);
            if(map.size() > 2) {
                int min = map.size();
                String toRmv = "";
                for (Iterator<Map.Entry<String, Pair<Integer, InsertAdHandler>>> it = map.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<String, Pair<Integer, InsertAdHandler>> entry = it.next();
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
        BannerAd.fetch(posId, "top");
    }

    public void hideBanner() {
        BannerAd.hidedlg();
    }
}
