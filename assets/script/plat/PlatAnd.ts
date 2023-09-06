import { NATIVE } from "cc/env";
import PlatBase, { ADCallback, AndLayoutGravity, Channel, GamePlat } from "./PlatBase";
// 3.6.0 以前将下面两行注释即可
import { native } from "cc";
var jsb = native;

export default class PlatAnd extends PlatBase {

    private static _instance: PlatAnd;

    static getInstance() {
        if (!this._instance) {
            this._instance = new PlatAnd();
        }
        return this._instance;
    }

    private _onShowRwdAdCallback: (isSuc: boolean, errcode?: number) => void;
    private _onShowTemplateAdCallback: (errcode?: number) => void;
    private _onShowInsertAdCallback: (errcode?: number) => void;
    private _onLoginCallback: Function;
    private _onCheckPlatReady: (env: "Debug" | "Release") => void;
    private _uma: UMA;

    public logCatch(...args): void {
        console.log(`[JSB-CATCH]`, ...args);
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
            jsb.jsbBridgeWrapper.addNativeEventListener("TemplateAdRet", (code: string) => {
                var icode = parseInt(code);
                this.logCatch("TemplateAdRet")
                this._onShowTemplateAdCallback?.(icode == 1 ? null : icode);
                this._onShowTemplateAdCallback = null;
            });
            jsb.jsbBridgeWrapper.addNativeEventListener("InsertAdRet", (code: string) => {
                var icode = parseInt(code);
                this.logCatch("InsertAdRet")
                this._onShowInsertAdCallback?.(icode == 1 ? null : icode);
                this._onShowInsertAdCallback = null;
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
                    jsb.jsbBridgeWrapper.dispatchEventToNative('Track', JSON.stringify({
                        eventID: eventId,
                        params: typeof (params) == "string" ? {
                            p2c_val: params
                        } : params,
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

    public showTemplateAd(arg: {
        posId: string,
        onAdClose: (errcode?: number) => void,
        widthMode: "Dip" | "MatchParent" | "WrapContent",
        force: boolean,
        widthDp?: number,
        scale?: number,
        gravity?: AndLayoutGravity,
        debug?: boolean,
    }) {
        if (NATIVE) {
            arg.widthDp ??= 300;
            arg.scale ??= 100;
            arg.gravity ??= AndLayoutGravity.Bottom | AndLayoutGravity.Center;
            arg.debug ??= false;
            arg["andMiArg"] = {
                widthMode: arg.widthMode,
                force: arg.force,
                widthDp: arg.widthDp,
                scale: arg.scale,
                gravity: arg.gravity,
                debug: arg.debug,
            };
            this._onShowTemplateAdCallback = arg.onAdClose;
            jsb.jsbBridgeWrapper.dispatchEventToNative('ShowTemplateAd', JSON.stringify(arg));
        }
    }

    public hideTemplateAd() {
        if (NATIVE) {
            this._onShowTemplateAdCallback = null;
            jsb.jsbBridgeWrapper.dispatchEventToNative('HideTemplateAd');
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
        onAdClose?: (errcode?: number) => void
    }) {
        if (NATIVE) {
            this._onShowInsertAdCallback = arg.onAdClose;
            jsb.jsbBridgeWrapper.dispatchEventToNative('ShowInterstitialAd', arg.posId);
        } else {
            super.showInterstitialAd(arg);
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
        return GamePlat.and
    }

    public get uma(): UMA {
        return this._uma
    }

    public get channel(): Channel {
        return window.channel as Channel;
    }

    public get packageVersion(): number {
        return window.packageVersion ?? 1;
    }
}
