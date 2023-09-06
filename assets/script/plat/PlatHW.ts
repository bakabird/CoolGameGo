import PlatBase, { ADCallback, GamePlat } from "./PlatBase";

function hwlog(...args) {
    console.log("hwlog", ...args);
}

export type PlatHWParam = {
    appId: string,
    // 玩家未登录华为帐号或鉴权失败时，是否拉起登录场景。
    // 0：表示如果玩家未登录华为帐号或鉴权失败，不会主动拉起帐号登录场景，适用于单机游戏的登录场景。
    // 1：表示如果玩家未登录华为帐号或鉴权失败，会主动拉起帐号登录场景，适用于网游的登录场景和单机游戏支付前强制登录场景。
    forceLogin: number,
}

export default class PlatHW extends PlatBase {
    private static _instance: PlatHW = null;

    public static getInstance() {
        if (!this._instance) {
            this._instance = new PlatHW();
        }
        return this._instance;
    }

    private _curPlatformVersionCode: number;
    private _rwdAd: any;
    private _nextShowAd: number = 0;
    private _lastRwdPosId: string;
    private _onRwdAdSuc: (isSuc: boolean, errcode?: number) => void = null;

    public showAD(onSuc: ADCallback, onNotSupport: Function, posId: string) {
        this.showRewardAd({
            suc: () => {
                onSuc(true);
            },
            fail: () => {
                onSuc(false);
            },
            notSupport: () => {
                onNotSupport();
            },
            posId,
        })
    }

    public showRewardAd(arg: {
        suc: () => void,
        fail?: (errcode: number) => void,
        notSupport?: () => void,
        posId: string,
    }) {
        hwlog("showRewardAd", arg.posId)
        if (!this._CheckVersion(1075)) {
            arg.notSupport?.();
            return;
        }
        if (this.curTime < this._nextShowAd) {
            arg.fail?.(-1)
            this.popTip("广告拉取过于频繁")
            return;
        }
        this._nextShowAd = this.curTime + 3 * 1000;
        if (this._onRwdAdSuc != null) {
            arg.fail?.(-1)
            hwlog("showAD cancel, last call not end.")
            return;
        }
        this._onRwdAdSuc = (isSuc: boolean, errcode?: number) => {
            if (isSuc) {
                arg.suc()
            } else {
                arg?.fail(errcode)
            }
        };
        if (!this._rwdAd || this._lastRwdPosId != arg.posId) {
            this._initRewardedAd(arg.posId);
        } else {
            this._rwdAd.load();
        }
    }

    /** 登录 */
    public login(onLogin: Function, uid: string) {
        const param = {
            forceLogin: this.huaweiParam.forceLogin,
            appid: this.huaweiParam.appId,
            success: function (data) {
                // 登录成功后，可以存储帐号信息。             
                hwlog("login success", data)
                onLogin(0, data);
            },
            fail: function (data, code) {
                hwlog("login fail", data, code)
                onLogin(code, data)
            }
        }
        if (this._CheckVersion(1070)) {
            qg.gameLoginWithReal(param)
        } else {
            qg.gameLogin(param);
        }
    }

    /**
     * 确认平台准备完毕
     */
    public checkPlatReady(onReady: (env: "Debug" | "Release") => void) {
        qg.getSystemInfo({
            success: (info) => {
                if (info.platformVersionCode) {
                    this._curPlatformVersionCode = info.platformVersionCode;
                } else {
                    this._curPlatformVersionCode = 1056;
                }
            },
            fail: () => {
                this._curPlatformVersionCode = 1056;
            },
            complete() {
                onReady("Release");
            }
        })
        // const update = qg.getUpdateManager()
        // update.onUpdateReady(function (data) {
        //     hwlog('onUpdateReady')
        //     qg.showDialog({
        //         title: '更新提示',
        //         message: '新版本已经准备好，是否重启应用？',
        //         success: function (data) {
        //             update.applyUpdate()//强制进行更新
        //         },
        //     })
        // })
        // update.onCheckForUpdate(function (data) {
        //     hwlog('onCheckForUpdate')
        // })
        // update.onUpdateFailed(function (data) {
        //     hwlog('onUpdateFailed')
        // })
    }


    public showBannerAd(arg: {
        posId: string,
        pos: "top" | "bottom",
        wxArg?: {
            width: number,
            bottom: number,
        }
    }): void {
        hwlog("showBannerAd", arg.pos, arg.pos)
    }

    public hideBannerAd(): void {
        hwlog("hideBannerAd")
    }

    public showInterstitialAd(arg: {
        posId: string,
    }) {
        hwlog("showInterstitialAd", arg)
    }

    public vibrate(type: "long" | "short") {
        if (type == "short") {
            qg.vibrateShort();//（15 ms）
        } else {
            qg.vibrateLong(); //400 ms
        }
    }

    public endGame() {
        qg.exitApplication({
            success: function () {
                hwlog("exitApplication success");
            },
            fail: function () {
                hwlog("exitApplication fail");
            },
            complete: function () {
                hwlog("exitApplication complete");
            }
        });
    }

    private _initRewardedAd(posId: string) {
        if (this._rwdAd) {
            this._rwdAd = null;
        }
        const rewardedAd = qg.createRewardedVideoAd({
            adUnitId: posId,
        });
        const rwdADWaiting = () => {
            this._onRwdAdSuc?.(false, -1);
            this._onRwdAdSuc = null;
        }
        const rwdAdFail = () => {
            this._onRwdAdSuc?.(false, 0);
            this._onRwdAdSuc = null;
        }
        const rwdAdSuc = () => {
            this._onRwdAdSuc?.(true);
            this._onRwdAdSuc = null;
        }
        rewardedAd.offError();
        rewardedAd.offLoad();
        rewardedAd.offClose();
        rewardedAd.onError(err => {
            hwlog("激励视频广告加载失败 " + err.errCode + " " + err.errMsg);
            rwdADWaiting();
        });
        rewardedAd.onLoad(() => {
            hwlog('激励视频广告加载完成-onload触发');
            rewardedAd.show();
        })
        rewardedAd.onClose((res) => {
            hwlog('视频广告关闭回调')
            if (res && res.isEnded) {
                hwlog("正常播放结束，可以下发游戏奖励");
                rwdAdSuc();
            } else {
                hwlog("播放中途退出，不下发游戏奖励");
                rwdAdFail();
            }
        });
        rewardedAd.load();
        this._rwdAd = rewardedAd;
    }

    /**
     * 加载banner广告
     */
    private _loadBannerAd(posId: string, pos: "top" | "bottom") {
        hwlog("_loadBannerAd", posId)
    }

    /**
     * 加载插屏
     */
    private _loadInterstitialAd(posId: string) {
        hwlog("_loadInterstitialAd", posId)
    }

    /**
     * 检查版本 >= version
     * @param version 
     */
    private _CheckVersion(version: number) {
        return this._curPlatformVersionCode >= version;
    }

    public get plat(): GamePlat {
        return GamePlat.huawei
    }

    public get uma(): UMA {
        return qg.uma
    }
}