import PlatBase, { ADCallback, GamePlat } from "./PlatBase";

export default class PlatVIVO extends PlatBase {
    private static _instance: PlatVIVO = null;

    public static getInstance() {
        if (!this._instance) {
            this._instance = new PlatVIVO();
        }
        return this._instance;
    }

    private _rwdAd;
    private _onRwdAdSuc: (isSuc: boolean, errcode?: number) => void = null;
    private _nextShowAd: number = 0;
    private _lastRwdPosId: string;

    private _bannerAd;
    private _lastBannerPos: string;

    private _insertAd;

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
        if (!this._rwdAd || this._lastRwdPosId != arg.posId) {
            this._initRewardedAd(arg.posId);
        } else {
            this._rwdAd.load();
        }
    }

    /**
     * 确认平台准备完毕
     */
    public checkPlatReady(onReady: (env: "Debug" | "Release") => void) {
        const update = qg.getUpdateManager()
        update.onUpdateReady(function (data) {
            console.log('onUpdateReady')
            qg.showDialog({
                title: '更新提示',
                message: '新版本已经准备好，是否重启应用？',
                success: function (data) {
                    update.applyUpdate()//强制进行更新
                },
            })
        })
        update.onCheckForUpdate(function (data) {
            console.log('onCheckForUpdate')
        })
        update.onUpdateFailed(function (data) {
            console.log('onUpdateFailed')
        })
        onReady("Release")
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
            var adhide = this._bannerAd.hide();
            adhide && adhide.then(() => {
                console.log("banner广告隐藏成功");
            }).catch(err => {
                console.log("banner广告隐藏失败", JSON.stringify(err));
                var addestroy = this._bannerAd.destroy();
                addestroy && addestroy.then(() => {
                    console.log("banner广告销毁成功");
                }).catch(err => {
                    console.log("banner广告销毁失败", JSON.stringify(err));
                });
                this._bannerAd = null;
            });
        }
    }

    public showInterstitialAd(arg: {
        posId: string,
    }) {
        this._loadInterstitialAd(arg.posId);
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
        if (this._bannerAd) {
            this._bannerAd.destroy();
            this._bannerAd = null;
        }
        const adProp = { adUnitId: posId, }
        if (pos == "bottom") {
            // style内无需设置任何字段，banner会在屏幕底部居中显示，
            // 没有style字段，banner会在上边显示
            adProp["style"] = {}
        }
        this._bannerAd = qg.createBannerAd(adProp);
        console.log(qg.getProvider())
        if (this._bannerAd) {
            this._bannerAd.onLoad((data) => {
                console.log(data, 'VIVO=====onload')
            })
            //监听 banner 广告尺寸变化事件
            this._bannerAd.onResize((data) => {
                console.log(data.width + "|" + data.height, 'onResize');
            })
            this._bannerAd.onError((data) => {
                console.log("VIVO 广告条加载失败! code : " + data.errCode + "; msg : " + data.errMsg);
            })
            this._bannerAd.show().then(() => {
                console.log(`VIVO小游戏banner 广告show成功,realWidth:${this._bannerAd.style.realWidth}, realHeight: ${this._bannerAd.style.realHeight}`)
            }, (err) => {
                console.log("VIVO小游戏banner 广告show失败:" + JSON.stringify(err));
                switch (err.code) {
                    case 30003:
                        console.log("新用户7天内不能曝光Banner，请将手机时间调整为7天后，退出游戏重新进入")
                        break;
                    case 30009:
                        console.log("10秒内调用广告次数超过1次，10秒后再调用")
                        break;
                    case 30002:
                        console.log("加载广告失败，重新加载广告")
                        break;
                    default:
                        // 参考 https://minigame.vivo.com.cn/documents/#/lesson/open-ability/ad?id=广告错误码信息 对错误码做分类处理
                        console.log("banner广告展示失败")
                        break;
                }
            })
        }
    }

    /**
     * 加载插屏
     */
    private _loadInterstitialAd(posId: string) {
        this._insertAd = qg.createInterstitialAd({
            adUnitId: posId,
        })
        this._insertAd.onLoad(() => {
            console.log("VIVO小游戏插屏广告加载成功")

            this._insertAd.show().then((data) => {
                console.log('VIVO小游戏插屏广告show成功', JSON.stringify(data))
            }, (err) => {
                console.log('VIVO小游戏插屏广告show失败', JSON.stringify(err));
                switch (err.code) {
                    case 30003:
                        console.log("新用户7天内不能曝光Banner，请将手机时间调整为7天后，退出游戏重新进入")
                        break;
                    case 30009:
                        console.log("10秒内调用广告次数超过1次，10秒后再调用")
                        break;
                    case 30002:
                        console.log("加载广告失败，重新加载广告")
                        break;
                    default:
                        // 参考 https://minigame.vivo.com.cn/documents/#/lesson/open-ability/ad?id=广告错误码信息 对错误码做分类处理
                        console.log("banner广告展示失败")
                        break;
                }
            })
        })

        this._insertAd.onClose(() => {
            console.log("VIVO小游戏插屏广告关闭")
            this._insertAd.destroy();
            this._insertAd = null;
        })
    }

    public get plat(): GamePlat {
        return GamePlat.vivo
    }

    public get uma(): UMA {
        return qg.uma
    }
}