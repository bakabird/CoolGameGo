import PlatBase, { ADCallback, GamePlat } from "./PlatBase";

export default class PlatMi extends PlatBase {
    private static _instance: PlatMi = null;

    public static getInstance() {
        if (!this._instance) {
            this._instance = new PlatMi();
        }
        return this._instance;
    }

    private _sysInfo: any = null;

    private _onRwdAdSuc: (isSuc: boolean, errcode?: number) => void = null;
    private _rwdAd;
    private _lastRwdPosId: string;
    private _nextShowAd: number = 0;
    // 0:待加载 1:Load中 2:Load完毕
    private _lastRwdState: number = 0;

    private _bannerAd: any = null;//banner
    private _lastBannerPos: string;

    private _insertAd: any = null;

    public get sysInfo() {
        if (this._sysInfo) return this._sysInfo;
        this._sysInfo = qg.getSystemInfoSync();
        return this._sysInfo;
    }

    public endGame() {
        qg.exitApplication({
            success: () => {
                console.log("退出成功！");
            },
            fail: () => {
                console.log("退出失败！");
            }
        });
    }

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
        if (this.curTime < this._nextShowAd) {
            arg?.fail(-1)
            return;
        }
        this._nextShowAd = this.curTime + 3 * 1000;
        if (this._lastRwdState == 1) {
            arg?.fail(-1)
            console.log("showRewardAd cancel, last call not end.")
            return;
        }
        this._onRwdAdSuc = (isSuc: boolean, errcode: number) => {
            if (isSuc) {
                arg.suc()
            } else {
                arg?.fail(errcode)
            }
        };

        if (!this._rwdAd || this._lastRwdPosId != arg.posId) {
            this._rwdAd?.destroy();
            this._rwdAd = null;
            this._lastRwdPosId = arg.posId;
            this._initRewardedAd(arg.posId);
        } else if (this._lastRwdState == 0) {
            this._rwdAd.load();
        } if (this._lastRwdState == 2) {
            this._rwdAd.show();
        }
    }

    private _initRewardedAd(posId: string) {
        const rewardedAd = qg.createRewardedVideoAd({
            adUnitId: posId,
        });
        let count = 0;
        const autoPlayAtFirstLoad = () => {
            if (count == 0) {
                this._rwdAd.show();
                count++;
            }
        }
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
        rewardedAd.onError((errMsg, errCode) => {
            console.log("激励视频广告加载失败 ", errCode, errMsg);
            this._lastRwdState = 0;
            rwdADWaiting();
        });
        rewardedAd.onLoad(() => {
            console.log('激励视频广告加载完成-onload触发');
            this._lastRwdState = 2;
            autoPlayAtFirstLoad();
        })
        rewardedAd.onClose((isEnded) => {
            console.log('视频广告关闭回调')
            if (isEnded) {
                console.log("正常播放结束，可以下发游戏奖励");
                rwdAdSuc();
            } else {
                console.log("播放中途退出，不下发游戏奖励");
                rwdAdFail();
            }
            this._lastRwdState = 0;
            // 平台接口特性：视频播放结束后会自动调 load
        });
        this._lastRwdState = 1;
        this._rwdAd = rewardedAd;
    }

    public showBannerAd(arg: {
        posId: string,
        pos: "top" | "bottom",
        wxArg?: {
            width: number,
            bottom: number,
        }
    }): void {
        if (!this._bannerAd || arg.pos != this._lastBannerPos) {
            this._loadBannerAd(arg.posId, arg.pos);
        } else {
            this._bannerAd.show();
        }
    }

    public hideBannerAd(): void {
        if (this._bannerAd) {
            this._bannerAd.hide();
            console.log("隐藏小米小游戏banner")
        }
    }

    /**
     * 加载banner广告
     */
    private _loadBannerAd(posId: string, pos: "top" | "bottom") {
        if (this._bannerAd) {
            this._bannerAd.destroy();
            this._bannerAd = null;
        }
        if (pos == "top") {
            console.error("top banner 暂不支持");
        }

        const bannerWidth = 385;
        const bannerHeight = 58
        let left = (this.sysInfo.screenWidth - bannerWidth) * 0.5;
        if (this.isLandsacpe) {
            left = this.sysInfo.screenWidth * 0.5
        }
        let bannerStyle: any = {
            left: left,
            top: this.sysInfo.screenHeight - bannerHeight,
            width: bannerWidth
        }

        this._lastBannerPos = pos;
        this._bannerAd = qg.createBannerAd({
            adUnitId: posId,
            style: bannerStyle
        })
        // console.log(qg.getProvider())
        if (this._bannerAd) {
            this._bannerAd.onLoad((res) => {
                console.log(res, 'onload')
            })
            //监听 banner 广告尺寸变化事件
            this._bannerAd.onResize((res) => {
                this._bannerAd.style.left = (this.sysInfo.screenWidth - res.width) * 0.5;
                console.log("===========left" + this._bannerAd.style.left, this._bannerAd.style);
            })
            this._bannerAd.onError((res) => {
                console.log(res, 'onError')
                this._bannerAd = null;
            })
            this._bannerAd.onClose(() => {
                console.log("banner广告关闭")
                this._bannerAd = null;
            })
            if (this._bannerAd) {
                this._bannerAd.show();
                console.log("xiaomi 小游戏Banner show");
            }
        }
    }


    /**
     * 确认平台准备完毕
     */
    public checkPlatReady(onReady: (env: "Debug" | "Release") => void) {
        if (this.sysInfo.platformVersionCode > 1060) {
            const updateManager = qg.getUpdateManager()
            updateManager.onCheckForUpdate(function (res) {
                // 请求完新版本信息的回调
                console.log('onCheckForUpdate', res.hasUpdate)
            })
            updateManager.onUpdateReady(function () {
                // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
                updateManager.applyUpdate()
            })
            updateManager.onUpdateFailed(function () {
                // 新版本下载失败
            })
        }
        onReady("Release")
    }


    public showInterstitialAd(arg: {
        posId: string,
    }) {
        console.log("显示小米小游戏插屏")
        if (this._insertAd) return;
        this._insertAd = qg.createInterstitialAd({
            adUnitId: arg.posId,
        })
        const close = () => {
            this._insertAd.destroy();
            this._insertAd = null;
        }
        this._insertAd.onLoad(() => {
            console.log("插屏广告加载成功")
            this._insertAd.show().then((data) => {
                console.log('小游戏插屏广告show成功', JSON.stringify(data))
                close()
            }, (err) => {
                console.log('小游戏插屏广告show失败', JSON.stringify(err))
                close()
            })
        })
        this._insertAd.onError((err) => {
            console.log("小游戏插屏广告出错:" + err.code + err.msg);
            close()
        });

        this._insertAd.onClose(() => {
            console.log("插屏广告关闭")
            this._insertAd.destroy();
            this._insertAd = null;
            close()
        })
    }

    public get plat(): GamePlat {
        return GamePlat.mi
    }

    public get uma(): UMA {
        return qg.uma
    }
}