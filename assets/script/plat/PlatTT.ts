import PlatBase, { ADCallback, GamePlat, Plat_ShareRecordOption } from "./PlatBase";

function ttlog(...args) {
    console.log("ttLog", ...args);
}

export default class PlatTT extends PlatBase {
    private static _instance: PlatTT = null;

    public static getInstance() {
        if (!this._instance) {
            this._instance = new PlatTT();
        }
        return this._instance;
    }

    private _rwdAd;
    private _onRwdAdSuc: (isSuc: boolean, errcode?: number) => void = null;
    private _ttRecord;
    private _ttRecordState: "idle" | "recording" | "paused" | "stoped" = "idle";
    private _onTTRecordStop: (res: any) => void;

    private get ttRecord() {
        if(!this._ttRecord) {
            this._ttRecord = tt.getGameRecorderManager();
            this._ttRecord.onStop((res)=>{
                this._onTTRecordStop?.(res);
            })
        }
        return this._ttRecord;
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
        if (this._onRwdAdSuc != null) {
            arg.fail?.(-1)
            console.log("showAD cancel, last call not end.")
            return;
        }
        if(tt.canIUse("createRewardedVideoAd")) {
            this._onRwdAdSuc = (isSuc: boolean, errcode?: number) => {
                this._onRwdAdSuc = null;
                if (isSuc) {
                    arg.suc()
                } else {
                    arg?.fail(errcode)
                }
            };
            if(!this._rwdAd) {
                this._rwdAd = tt.createRewardedVideoAd({
                    adUnitId: arg.posId,
                });
                this._rwdAd.onClose((res) => {
                    //这里监听广告的close 事件
                    if (res.isEnded) {
                        // do something
                        this._onRwdAdSuc(true)
                    } else {
                        this._onRwdAdSuc(false, 0)
                    }
                });
            }
            this._rwdAd.show().catch(err => {
                ttlog("rwdErr 2", err)
                this._onRwdAdSuc(false, -1);
            }) 
        } else {
            arg.notSupport?.()
        }
    }

    /**
     * 确认平台准备完毕
     */
    public checkPlatReady(onReady: (env: "Debug" | "Release") => void) {
        if (tt.canIUse("getUpdateManager")) {
            const updateManager = tt.getUpdateManager();
            updateManager.onUpdateReady((res) => {
                tt.showModal({
                    title: "更新提示",
                    content: "新版本已经准备好，是否重启小游戏？",
                    success: (res) => {
                        if (res.confirm) {
                            // 新的版本已经下载好，调用 applyUpdate 应用新版本并重启
                            updateManager.applyUpdate();
                        }
                    },
                });
            });
            updateManager.onUpdateFailed((err) => {
                // 新的版本下载失败
                console.log("版本下载失败原因", err);
                tt.showToast({
                    title: "新版本下载失败，请稍后再试",
                    icon: "none",
                });
            });
        }
        if (tt.canIUse("showShareMenu")) {
            tt.showShareMenu({});
        }
        onReady("Release");
    }

    public vibrate(type: "long" | "short") {
        if (type == "short") {
            tt.vibrateShort()
        } else {
            tt.vibrateLong();
        }
    }

    public endGame() {
        tt.exitMiniProgram({
            isFullExit: true
        });
    }

    /**
     * 开始录屏
     */
    public startRecord(options: {
        duration?: number,
        isMarkOpen?: boolean,
        locTop?: number
        locLeft?: number
        frameRate?: number,
    }) {
        if(this._ttRecordState == "idle") {
            ttlog("startRecord");
            this.ttRecord.start(options);
            this._ttRecordState = "recording";
        } else {
            ttlog("startRecord fail, state:", this._ttRecordState)
        }
    }

    /**
     * 暂停录屏
     */
    public pauseRecord() {
        if(this._ttRecordState != "recording") {
            ttlog("pauseRecord fail, state:", this._ttRecordState)
            return;
        }
        ttlog("pauseRecord");
        this.ttRecord.pause()
        this._ttRecordState = "paused";
    }

    /**
     * 恢复录屏
     */
    public resumeRecord() {
        if(this._ttRecordState != "paused") {
            ttlog("resumeRecord fail, state:", this._ttRecordState)
            return;
        }
        ttlog("resumeRecord");
        this.ttRecord.resume()
        this._ttRecordState = "recording";
    }

    /**
     * 结束录屏
     */
    public stopRecord(onStop: (res: any) => void) {
        if(this._ttRecordState != "recording" && this._ttRecordState != "paused") {
            ttlog("stopRecord fail, state:", this._ttRecordState)
            onStop("");
            return;
        }
        this._onTTRecordStop = (res)=> {
            this._onTTRecordStop = null;
            ttlog("stopRecord res:", res);
            onStop(res);
        }
        ttlog("stopRecord");
        this.ttRecord.stop()
        this._ttRecordState = "idle";
    }

    /**
     * 分享录屏
     * @param options 
     */
    public shareRecord(options: Plat_ShareRecordOption) {
        if(options.vidPath) {
            tt.shareAppMessage({
                channel: "video",
                extra: {
                    videoPath: options.vidPath,
                    hashtag_list: options.hashtag_list,
                }
            })
        }
    }

    public get plat(): GamePlat {
        return GamePlat.tt;
    }

    public get uma(): UMA {
        return tt.uma
    }
}
