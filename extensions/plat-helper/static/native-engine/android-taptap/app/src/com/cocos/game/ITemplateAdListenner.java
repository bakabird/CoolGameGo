package com.cocos.game;

public interface ITemplateAdListenner {
    /**
     *
     * @param err "-2"没广告
     */
    void onError(String err);
    void onShow();
    void onClick();
    void onDismiss();
}
