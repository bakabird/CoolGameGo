package com.cocos.game;

import android.util.Log;
import android.view.ViewGroup;

import androidx.lifecycle.MutableLiveData;

import com.xiaomi.ad.mediation.MMAdConfig;
import com.xiaomi.ad.mediation.MMAdError;
import com.xiaomi.ad.mediation.template.MMAdTemplate;
import com.xiaomi.ad.mediation.template.MMTemplateAd;

import java.util.List;

public class TemplateAd {

    private static String TAG = "TemplateAd";

    private final MutableLiveData<MMTemplateAd> mAd = new MutableLiveData<>();
    private MMAdTemplate mAdTemplate;
    private ITemplateAdListenner mListenner;
    /**
     * -1 已销毁
     * 0 初始
     * 1 加载中
     * 2 加载完毕
     */
    private int statu = 0;


    public TemplateAd(String posId, ITemplateAdListenner listenner) {
        mAdTemplate = new MMAdTemplate(App.get(), posId);
        mListenner = listenner;
        mAdTemplate.onCreate();
    }

    public void destory() {
        if (mAd != null) {
            MMTemplateAd ad = mAd.getValue();
            if (ad != null) {
                ad.destroy();
            }
        }
    }

    public void loadTemplateAd(ViewGroup mContainer) {
        if(statu != 0) return;
        statu = 1;
        MMAdConfig adConfig = new MMAdConfig();
        adConfig.imageHeight = 1920;
        adConfig.imageWidth = 1080;
        adConfig.setTemplateContainer(mContainer);
        mAdTemplate.load(adConfig, new MMAdTemplate.TemplateAdListener() {
            @Override
            public void onTemplateAdLoaded(List<MMTemplateAd> list) {
                Log.d(TAG, "onTemplateAdLoaded...");
                if (list != null && list.size() > 0) {
                    Log.d(TAG, "onTemplateAdLoaded");
                    mAd.setValue(list.get(0));
                    statu = 2;
                    showAd();
                } else {
//                    mAdError.setValue(new MMAdError(MMAdError.LOAD_NO_AD));
                    statu = 0;
                    mListenner.onError("-2");
                }
            }
            @Override
            public void onTemplateAdLoadError(MMAdError error) {
                Log.e(TAG, "" + error.errorCode + " " + error.externalErrorCode);
//                mAdError.setValue(error);
                statu = 0;
                mListenner.onError(error.externalErrorCode.equals("301007") ? "-2" : "0");
            }
        });
    }


    private void showAd() {
        if(statu != 2) return;
        mAd.getValue().showAd(new MMTemplateAd.TemplateAdInteractionListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "onAdLoaded");
            }
            @Override
            public void onAdRenderFailed() {
                statu = 0;
                Log.d(TAG, "onAdRenderFailed");
//                cancel();
            }

            @Override
            public void onError(MMAdError error) {
                Log.e("TemplateAd", "code = " + error.errorCode + "msg = " + error.errorMessage);
                mListenner.onError("" + error.errorCode);
            }

            @Override
            public void onAdShow() {
                Log.d(TAG, "onAdShow");
                mListenner.onShow();
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "onAdClicked");
                mListenner.onClick();
            }

            @Override
            public void onAdDismissed() {
                statu = 0;
                Log.d(TAG, "onAdDismissed");
                mListenner.onDismiss();
            }
        });
    }
}
