import PlatBase, { ADCallback, GamePlat } from "./PlatBase";

function hwlog(...args) {
    console.log("hwlog", ...args);
}

export default class PlatHW extends PlatBase {
    private static _instance: PlatHW = null;

    public static getInstance() {
        if (!this._instance) {
            this._instance = new PlatHW();
        }
        return this._instance;
    }

    private _rwdAd;
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
    }

    /**
     * 确认平台准备完毕
     */
    public checkPlatReady(onReady: (env: "Debug" | "Release") => void) {
        // const update = qg.getUpdateManager()
        // update.onUpdateReady(function (data) {
        //     console.log('onUpdateReady')
        //     qg.showDialog({
        //         title: '更新提示',
        //         message: '新版本已经准备好，是否重启应用？',
        //         success: function (data) {
        //             update.applyUpdate()//强制进行更新
        //         },
        //     })
        // })
        // update.onCheckForUpdate(function (data) {
        //     console.log('onCheckForUpdate')
        // })
        // update.onUpdateFailed(function (data) {
        //     console.log('onUpdateFailed')
        // })
        onReady("Release");
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
        qg.exitApplication();
    }

    private _initRewardedAd(posId: string) {
        if (this._rwdAd) {
            this._rwdAd = null;
        }
        const rewardedAd = qg.createRewardedVideoAd({
            posId,
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
        rewardedAd.onError(err => {
            console.log("激励视频广告加载失败", err);
            rwdADWaiting();
        });
        rewardedAd.onLoad((res) => {
            console.log('激励视频广告加载完成-onload触发', JSON.stringify(res));
            rewardedAd.show().then(() => {
                console.log('激励视频广告展示完成');
            }).catch((err) => {
                console.log('激励视频广告展示失败', JSON.stringify(err));
                rwdADWaiting();
            })
        })
        const func = (res) => {
            console.log('视频广告关闭回调')
            if (res && res.isEnded) {
                console.log("正常播放结束，可以下发游戏奖励");
                rwdAdSuc();
            } else {
                console.log("播放中途退出，不下发游戏奖励");
                rwdAdFail();
            }
        }
        rewardedAd.onClose(func);
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

    public get plat(): GamePlat {
        return GamePlat.huawei
    }

    public get uma(): UMA {
        return qg.uma
    }
}