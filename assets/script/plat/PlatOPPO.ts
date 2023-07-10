import PlatBase, { ADCallback, GamePlat } from "./PlatBase";

export default class PlatOPPO extends PlatBase {
    private static _instance: PlatOPPO = null;

    public static getInstance() {
        if (!this._instance) {
            console.log("using PlatOPPO")
            this._instance = new PlatOPPO();
        }
        return this._instance;
    }

    private _rwdAd;
    private _onRwdAdSuc: (isSuc: boolean, errcode?: number) => void;
    private _nextShowAd: number = 0;
    private _lastRwdPosId: string;
    private _bannerAd;
    private _lastBannerPos: string;
    private _insertAd;
    private _deviceInfo: any;

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
            arg.fail?.(-1)
            this.popTip("广告拉取过于频繁")
            return;
        }
        this._nextShowAd = this.curTime + 3 * 1000;
        if (this._onRwdAdSuc != null) {
            arg.fail?.(-1)
            console.log("showAD cancel, last call not end.")
            return;
        }
        this._onRwdAdSuc = (isSuc: boolean, errcode?: number) => {
            if (isSuc) {
                arg.suc()
            } else {
                arg?.fail(errcode)
            }
        };
        if (!this._rwdAd || arg.posId != this._lastRwdPosId) {
            this._initRewardedAd(arg.posId);
            this._lastRwdPosId = arg.posId;
        }
        this._rwdAd.load();
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
            this._lastRwdPosId = arg.posId;
        } else {
            this._bannerAd.show();
        }
    }

    public hideBannerAd(): void {
        if (this._bannerAd) {
            this._bannerAd.hide();
        }
    }

    public showInterstitialAd(arg: {
        posId: string,
    }) {
        console.log("暂不支持")
        return;
        this._loadInterstitialAd(arg.posId);
    }

    public vibrate(type: "long" | "short") {
        if (type == "short") {
            qg.vibrateShort({ success(res) { }, fail(res) { } });//（20 ms）
        } else {
            qg.vibrateLong({ success(res) { }, fail(res) { } });  //400 ms
        }
    }

    /**
     * 确认平台准备完毕
     */
    public checkPlatReady(onReady: (env: "Debug" | "Release") => void) {
        qg.getSystemInfo({
            success: data => {
                this._deviceInfo = {
                    brand: data.brand, model: data.model, pixelRatio: data.pixelRatio,
                    screenWidth: data.screenWidth, screenHeight: data.screenHeight,
                    windowWidth: data.windowWidth, windowHeight: data.windowHeight, statusBarHeight: data.statusBarHeight,
                    language: data.language, version: '', system: data.system, platform: data.platformVersionName, fontSizeSetting: 0, SDKVersion: '', benchmarkLevel: 0, platformVersionCode: data.platformVersionCode,
                };
                console.log('sysinfo:', JSON.stringify(data));
                if (this._deviceInfo.platformVersionCode >= 1094) {
                    this._dealUpdate();
                }
            },
            fail: err => {
                console.error('getsysinfo failed:', err);
            },
            complete: () => {
                onReady("Release")
            }
        });
    }

    private _dealUpdate() {
        const updateManager = qg.getUpdateManager();
        //检测是否有可更新版本
        updateManager.onCheckForUpdate(function (res) {
            // 请求完新版本信息的回调
            if (res.hasUpdate) {
                // updateManager.applyRpkUpdate();
            } else {
                console.log("PlatOPPO 没有可更新版本");
                // qg.showToast({
                //     title: "没有可更新版本",
                //     icon: "none",
                //     duration: 2000,
                // });
            }
        });
        updateManager.onUpdateReady(function () {
            qg.showModal({
                title: "更新提示",
                content: "新版本已经准备好，是否重启应用？",
                success: function (res) {
                    if (res.confirm) {
                        // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
                        updateManager.applyUpdate();
                    }
                },
            });
        });
    }

    public endGame() {
        qg.exitApplication({});
    }

    public setLoadingProgress(pro: number): void {
        qg.setLoadingProgress({
            progress: pro,
        });
        if (qg.getSystemInfoSync().platformVersionCode < 1076) {
            console.log("新的数据上报接口 API 仅支持平台版本号大于 1076 的快应用");
            return;
        }
        qg.reportMonitor("load_res_begin");
    }

    public loadingComplete(): void {
        qg.loadingComplete({
            complete: (res) => {
                if (qg.getSystemInfoSync().platformVersionCode < 1076) {
                    console.log("新的数据上报接口 API 仅支持平台版本号大于 1076 的快应用");
                    return;
                }
                qg.reportMonitor("load_res_complete");
                qg.reportMonitor("game_scene");
            },
        });
    }

    private _initRewardedAd(posId: string) {
        if (this._rwdAd) this._rwdAd.destroy();
        const rewardedAd = qg.createRewardedVideoAd({
            adUnitId: posId,
        });
        const rwdADWaiting = () => {
            this._onRwdAdSuc?.(false);
            this._onRwdAdSuc = null;
        }
        const rwdAdFail = () => {
            this._onRwdAdSuc?.(false);
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
        rewardedAd.onLoad(() => {
            console.log('激励视频广告加载完成-onload触发');
            rewardedAd.show();
        })
        rewardedAd.onClose((res) => {
            console.log('视频广告关闭回调')
            if (res == undefined) {
                console.log("正常播放结束，可以下发游戏奖励");
                rwdAdSuc();
            } else {
                console.log('用户点击了关闭广告按钮')
                if (res.isEnded) {
                    console.log('用户看完了')
                    rwdAdSuc();
                } else {
                    console.log("播放中途退出，不下发游戏奖励");
                    rwdAdFail();
                }
            }
        });
        this._rwdAd = rewardedAd;
    }

    /**
     * 加载banner广告
     */
    private _loadBannerAd(posId: string, pos: "top" | "bottom") {
        if (pos == "top") {
            console.error("top banner 暂不支持");
        }
        if (this._bannerAd) {
            this._bannerAd.destroy();
            this._bannerAd = null;
        }
        this._bannerAd = qg.createBannerAd({
            adUnitId: posId,
        })
        console.log(qg.getProvider())
        if (this._bannerAd) {
            this._bannerAd.onLoad((data) => {
                console.log(data, 'onload')
            })
            //监听 banner 广告尺寸变化事件
            this._bannerAd.onResize((data) => {
                console.log(data.width + "|" + data.height, 'onResize')
            })
            this._bannerAd.onError((data) => {
                console.log(data, 'onError')
                this._bannerAd = null;
            })
            this._bannerAd.show().then(() => {
                console.log(`banner 广告show成功,realWidth:${this._bannerAd.style.realWidth}, realHeight: ${this._bannerAd.style.realHeight}`)
            }, (err) => {
                console.log("banner 广告show失败:" + JSON.stringify(err));
            })
        }
    }

    /**
     * 加载插屏
     */
    private _loadInterstitialAd(posId: string) {
        if (this._insertAd) return;
        this._insertAd = qg.createInterstitialAd({
            adUnitId: posId,
        })
        this._insertAd.onLoad(() => {
            console.log("插屏广告加载成功")

            this._insertAd.show().then((data) => {
                console.log('OPPO 小游戏插屏广告show成功', JSON.stringify(data))
                //OPPO 配置当前插屏显示成功后销毁banner
                this.hideBannerAd();
            }, (err) => {
                console.log('OPPO 小游戏插屏广告show失败', JSON.stringify(err))
            })
        })

        let clearCallBack = () => {
            this._insertAd.offShow();
            this._insertAd.offError();
            this._insertAd.offLoad();
        }
        this._insertAd.onError(((err) => {
            console.log("OPPO 小游戏插屏广告出错:" + err.code + err.msg);
            this._insertAd.offError();
            clearCallBack();
            this._insertAd.destroy();
            this._insertAd = null;
        }).bind(this));

        this._insertAd.onClose(() => {
            console.log("插屏广告关闭")
            this._insertAd.destroy();
            this._insertAd = null;
        })
    }

    public get plat() {
        return GamePlat.oppo
    }
}