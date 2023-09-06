import { NATIVE } from "cc/env";
import PlatBase, { ADCallback, GamePlat } from "./PlatBase";
// 3.6.0 以前将下面两行注释即可
import { native } from "cc";
var jsb = native;

export default class PlatIOS extends PlatBase {
    private static _instance;

    public static getInstance() {
        if (!this._instance) {
            this._instance = new PlatIOS();
        }
        return this._instance;
    }

    private _onShowRwdAdCallback: (isSuc: boolean, errcode?: number) => void;
    private _onLoginCallback: Function;
    private _onCheckPlatReady: (env: "Debug" | "Release") => void;
    private _uma: UMA;

    public logCatch(...args): void {
        console.log(`[JSB-IOS-CATCH]`, ...args);
    }

    constructor() {
        super();
        if (NATIVE) {
            jsb.jsbBridgeWrapper.removeAllListeners();
            jsb.jsbBridgeWrapper.addNativeEventListener("ShowAdRet", (code: string) => {
                var icode = parseInt(code);
                this.logCatch("ShowAdRet")
                this._onShowRwdAdCallback?.(icode == 1, icode == 0 ? 0 : -1);
                this._onShowRwdAdCallback = null;
            });
            jsb.jsbBridgeWrapper.addNativeEventListener("AntiAddictionRet", (code: string) => {
                this.logCatch("AntiAddictionRet")
                this._onLoginCallback()
                this._onLoginCallback = null;
            });
            jsb.jsbBridgeWrapper.addNativeEventListener("CheckPlatReadyRet", (env: "Debug" | "Release") => {
                this.logCatch("CheckPlatReadyRet")
                this._onCheckPlatReady(env)
                this._onCheckPlatReady = null;
            });
            this._uma = {
                trackEvent(eventId, params: string | object) {
                    if (typeof (params) == "string") {
                        params = { p2c_val: params }
                    } else {
                        for (let key in params) {
                            params[key] = params[key].toString();
                        }
                    }
                    jsb.jsbBridgeWrapper.dispatchEventToNative('Track', JSON.stringify({
                        eventID: eventId,
                        params,
                    }));
                }
            } as UMA;
        } else {
            this._uma = {
                trackEvent(eventId, params) {
                    console.log('[uma]trackEvent', eventId, params)
                }
            } as UMA;
        }
    }

    public login(onLogin: Function, uid: string): void {
        if (NATIVE) {
            if (this._onLoginCallback)
                return;
            this._onLoginCallback = onLogin;
            jsb.jsbBridgeWrapper.dispatchEventToNative('AntiAddiction', uid);
        } else {
            super.login(onLogin, uid);
        }
    }

    showAD(onSuc: ADCallback, onNotSupport: Function, posId: string) {
        this.showRewardAd({
            suc: () => onSuc(true),
            fail: () => onSuc(false),
            notSupport: () => onNotSupport(),
            posId,
        })
    }

    public showBannerAd(arg: {
        posId: string,
        pos: "top" | "bottom"
    }) {
        if (NATIVE) {
            jsb.jsbBridgeWrapper.dispatchEventToNative('ShowBannerAd', JSON.stringify(arg));
        }
    }


    public hideBannerAd() {
        if (NATIVE) {
            jsb.jsbBridgeWrapper.dispatchEventToNative('HideBannerAd');
        }
    }


    public showRewardAd(arg: {
        suc: () => void,
        fail?: (errcode: number) => void,
        notSupport?: () => void
        posId: string,
    }) {
        if (this._onShowRwdAdCallback)
            return;
        if (NATIVE) {
            const onAdCallback = (isSuc, errcode?: number) => {
                if (isSuc) {
                    arg.suc();
                } else {
                    arg.fail?.(errcode);
                }
            }
            this._onShowRwdAdCallback = onAdCallback;
            jsb.jsbBridgeWrapper.dispatchEventToNative('ShowAd', arg.posId);
        } else {
            super.showRewardAd(arg);
        }
    }

    public showInterstitialAd(arg: {
        posId: string,
    }) {
        if (NATIVE) {
            jsb.jsbBridgeWrapper.dispatchEventToNative('ShowInterstitialAd', arg.posId);
        }
    }

    endGame() {
        if (NATIVE) {
            jsb.jsbBridgeWrapper.dispatchEventToNative('EndGame', "");
        } else {
            super.endGame();
        }
    }

    enterGameCenter(): void {
        if (NATIVE) {
            jsb.jsbBridgeWrapper.dispatchEventToNative('EnterGameCenter', "");
        } else {
            super.enterGameCenter();
        }
    }

    public checkPlatReady(onReady: (env: "Debug" | "Release") => void) {
        if (NATIVE) {
            if (this._onCheckPlatReady)
                return;
            this._onCheckPlatReady = onReady;
            jsb.jsbBridgeWrapper.dispatchEventToNative('CheckPlatReady', "");
        } else {
            super.checkPlatReady(onReady);
        }
    }

    public get plat(): GamePlat {
        return GamePlat.ios
    }

    public get uma(): UMA {
        return this._uma
    }

    public get packageVersion(): number {
        return window.packageVersion;
    }
}