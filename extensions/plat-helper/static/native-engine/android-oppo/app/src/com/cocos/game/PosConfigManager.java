package com.cocos.game;

import android.content.Context;
import android.content.SharedPreferences;

public class PosConfigManager {
    private static final String SF_CONFIG = "mob_pos_config";
    private static final String KEY_BANNER = "banner";
    private static final String KEY_HOT_SPLASH = "hotSplash";
    private static final String KEY_INTERSTITIAL_PORT = "interstitial_port";
    private static final String KEY_INTERSTITIAL_LAND = "interstitial_land";
    private static final String KEY_INTERSTITIAL_VIDEO_PORT = "interstitial_video_port";
    private static final String KEY_INTERSTITIAL_VIDEO_LAND = "interstitial_video_land";
    private static final String KEY_NATIVE_512 = "native_512";
    private static final String KEY_NATIVE_640 = "native_640";
    private static final String KEY_NATIVE_320 = "native_320";
    private static final String KEY_NATIVE_320_LIST = "native_320_list";
    private static final String KEY_NATIVE_ADVANCE_512 = "native_advance_512";
    private static final String KEY_NATIVE_ADVANCE_640 = "native_advance_640";
    private static final String KEY_NATIVE_ADVANCE_320 = "native_advance_320";
    private static final String KEY_NATIVE_ADVANCE_320_LIST = "native_advance_320_list";
    private static final String KEY_NATIVE_ADVANCE_VIDEO = "native_advance_video";
    private static final String KEY_NATIVE_ADVANCE_VIDEO_LIST = "native_advance_video_list";
    private static final String KEY_NATIVE_ADVANCE_VIDEO_LIST_INTERVAL = "native_advance_video_list_interval";
    private static final String KEY_NATIVE_ADVANCE_MIX = "native_advance_mix";
    private static final String KEY_NATIVE_ADVANCE_VERTICAL = "native_advance_vertical";
    private static final String KEY_NATIVE_TEMPLATE_640 = "native_template_640";
    private static final String KEY_NATIVE_TEMPLATE_320 = "native_template_320";
    private static final String KEY_NATIVE_TEMPLATE_320_LIST = "native_template_320_list";
    private static final String KEY_NATIVE_REWARD = "native_reward";
    private static final String KEY_NATIVE_REWARD_RECYCLE = "native_Reward_recycle";
    private static final String KEY_REWARD_VIDEO = "reward_video";
    private static final String KEY_PORT_SPLASH = "port_splash";
    private static final String KEY_LAND_SPLASH = "land_splash";

    private static PosConfigManager mInstance;

    public static PosConfigManager getInstance() {
        if (null != mInstance) {
            return mInstance;
        }
        synchronized (PosConfigManager.class) {
            if (null == mInstance) {
                mInstance = new PosConfigManager();
            }
            return mInstance;
        }
    }

    private SharedPreferences mSF;

    private PosConfigManager() {
    }

    public void init(Context context) {
        if (null != mSF) {
            return;
        }
        mSF = context.getSharedPreferences(SF_CONFIG, Context.MODE_PRIVATE);
    }

}
