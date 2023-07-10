import PlatBase, { ADCallback, GamePlat } from "./PlatBase";


export default class PlatWX extends PlatBase {

    private static _instance: PlatWX;

    static getInstance() {
        if (!this._instance) {
            this._instance = new PlatWX();
        }
        return this._instance;
    }

    private _sysInfo: any = null;
    private _nextShowAd: number = 0;
    private _videoAd: any = null;
    private _lastRwdPosId: string = null;
    private _onRwdAdSuc: () => void;
    private _onRwdAdFail: (errcode: number) => void;

    public get sysInfo() {
        if (this._sysInfo) return this._sysInfo;
        this._sysInfo = wx.getSystemInfoSync();
        return this._sysInfo;
    }

    public login(onLogin: Function, uid: string) {
        onLogin();
        // 获取微信界面大小
        let screenWidth = this.sysInfo.screenWidth;
        let screenHeight = this.sysInfo.screenHeight;
        wx.login({
            success(res) {
                if (res.code) {
                    let code = res.code;
                    console.log("登陆成功,获取到code", code);
                } else {
                    console.log('登录失败！' + res.errMsg)
                }
                var button = wx.createUserInfoButton(
                    {
                        type: 'text',
                        text: '',
                        style: {
                            left: 0,
                            top: 0,
                            width: screenWidth,
                            height: screenHeight,
                            lineHeight: 40,
                            backgroundColor: '#00000000',
                            color: '#ffffff',
                            textAlign: 'center'
                        }
                    })
                button.onTap((res) => {
                    if (res.errMsg == "getUserInfo:ok") {
                        console.log("授权用户信息")
                        //获取到用户信息
                        wx.getUserInfo({
                            lang: "zh_CN",
                            success: function (res) {
                                let userInfo = res.userInfo
                                console.log(userInfo)
                            },
                            fail: function () {
                                console.log("获取失败");
                                return false;
                            }
                        })
                        //清除微信授权按钮
                        button.destroy()
                    }
                    else {
                        //清除微信授权按钮
                        button.destroy()
                        console.log("授权失败")
                    }
                })
            },
        })

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
        if (!this.isOverMinVersion("2.0.4")) {
            console.log("当前版本不支持视频广告!");
            arg.notSupport?.();
            return;
        }
        if (this.curTime < this._nextShowAd) {
            arg.fail?.(-1)
            console.log("广告拉取过于频繁")
            return;
        }
        this._nextShowAd = this.curTime + 3 * 1000;
        if (this._onRwdAdSuc != null) {
            arg.fail?.(-1)
            console.log("showRwdAd cancel, last call not end.")
            return;
        }
        this._onRwdAdSuc = arg.suc;
        this._onRwdAdFail = arg.fail;
        if (!this._videoAd || this._lastRwdPosId != arg.posId) {
            this._lastRwdPosId = arg.posId;
            this._initRewardedAd(arg.posId);
        }
        this._videoAd.show();
    }

    public vibrate(type: "long" | "short") {
        console.log("vibrate", type);
        if (type == "short") {
            //使手机发生较短时间的振动（15 ms）。仅在 iPhone 7 / 7 Plus 以上及 Android 机型生效
            wx.vibrateShort({ success(res) { }, fail(res) { } });
        } else {
            //@ts-ignore
            wx.vibrateLong({ success(res) { }, fail(res) { } });  //400 ms
        }
    }

    private _initRewardedAd(posId: string) {
        let autoplay = true;
        const rewardedAd = wx.createRewardedVideoAd({
            adUnitId: posId,
        });
        const rwdADFail = () => {
            this._onRwdAdFail?.(-1);
            this._onRwdAdSuc = this._onRwdAdFail = null;
        }
        const rwdAdCancel = () => {
            this._onRwdAdFail?.(0)
            this._onRwdAdSuc = this._onRwdAdFail = null;
        }
        const rwdAdSuc = () => {
            this._onRwdAdSuc()
            this._onRwdAdSuc = this._onRwdAdFail = null;
        }
        rewardedAd.onError(err => {
            console.log("激励视频广告加载失败", err);
            rwdADFail();
            this._videoAd = null;
        });
        rewardedAd.onLoad(() => {
            console.log('激励视频广告加载完成-onload触发');
            if (autoplay) {
                rewardedAd.show()
                autoplay = false;
            }
        })
        rewardedAd.onClose((res) => {
            console.log('视频广告关闭回调')
            // 用户点击了【关闭广告】按钮
            // 小于 2.1.0 的基础库版本，res 是一个 undefined
            if (res && res.isEnded || res === undefined) {
                // 正常播放结束，可以下发游戏奖励
                console.log("正常播放结束，可以下发游戏奖励");
                rwdAdSuc();
            } else {
                // 播放中途退出，不下发游戏奖励
                console.log("播放中途退出，不下发游戏奖励");
                rwdAdCancel();
            }
        });
        this._videoAd = rewardedAd;
    }

    public isOverMinVersion(minVersion: string) {
        let curVersion: string = this.sysInfo.SDKVersion;
        return this._compareVersion(curVersion, minVersion) >= 0;
    }

    /**
     * 确认平台准备完毕
     */
    public checkPlatReady(onReady: (env: "Debug" | "Release") => void) {
        wx.showShareMenu({
            withShareTicket: true,
            menus: ['shareAppMessage', 'shareTimeline']
        })
        if (this.isOverMinVersion("1.9.90")) {
            const updateManager = wx.getUpdateManager()
            updateManager.onCheckForUpdate(function (res) {
                // 请求完新版本信息的回调
                console.log(res.hasUpdate)
            })
            updateManager.onUpdateReady(function () {
                wx.showModal({
                    title: '更新提示',
                    content: '新版本已经准备好，是否重启应用？',
                    success: function (res) {
                        if (res.confirm) {
                            // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
                            updateManager.applyUpdate()
                        }
                    }
                })
            })
            updateManager.onUpdateFailed(function () {
                // 新版本下载失败
            })
        }
        onReady("Release")
    }

    private _compareVersion(v1, v2) {
        v1 = v1.split('.')
        v2 = v2.split('.')
        const len = Math.max(v1.length, v2.length)
        while (v1.length < len) {
            v1.push('0')
        }
        while (v2.length < len) {
            v2.push('0')
        }
        for (let i = 0; i < len; i++) {
            const num1 = parseInt(v1[i])
            const num2 = parseInt(v2[i])

            if (num1 > num2) {
                return 1
            } else if (num1 < num2) {
                return -1
            }
        }
        return 0
    }

    public get plat() {
        return GamePlat.wx
    }

    public get uma(): UMA {
        return wx.uma
    }
}