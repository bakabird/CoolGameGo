import { game } from "cc";
import AdCfg from "./custom/AdCfg";
import Plat from "./Plat";

export type ADCallback = (isSuc: boolean) => void;

export enum GamePlat {
    dev = "dev",
    web = "web",
    wx = "wx",
    vivo = "vivo",
    oppo = "oppo",
    ios = "ios",
    and = "and",
    mi = "mi",
    huawei = "huawei",
    tt = "tt",
}

export enum Channel {
    Taptap = "Taptap",
    Oppo = "Oppo",
    Vivo = "Vivo",
    Mi = "Mi",
    YYB = "YYB",
    Default = "Default",
}

export type Plat_ShareRecordOption = {
    vidPath?: string,
    hashtag_list?: Array<string>,
}

export default class PlatBase {
    private testShowADCount: number = 0;
    private _isInit: boolean = false;

    protected popTip: (tip: string) => void;
    protected isLandsacpe: boolean;


    /**
     * 初始化
     * @param option 初始化参数
     * @param option.popTip 弹出提示
     * @param option.isLandsacpe 是否横屏
     * @returns 
     */
    public init(option: {
        popTip: (tip: string) => void,
        isLandsacpe: boolean,
        /** 是否在对应平台上强制使用PlatWeb */
        usePlatWeb?: {
            [key in GamePlat]?: boolean
        },
    }) {
        if (this._isInit) return;
        if (
            (option.popTip === null || option.popTip === undefined)
            || (option.isLandsacpe === null || option.isLandsacpe === undefined)
        ) {
            console.error("PlatBase.init 参数错误", option); return
        }
        AdCfg.ajustByPlat()
        this.popTip = option.popTip;
        this.isLandsacpe = this.isLandsacpe;
        this._isInit = true;
        // console.error(this.plat)
        if (option.usePlatWeb && option.usePlatWeb[this.plat]) {
            Plat.forceUsePlatWeb = true;
        }
    }

    /** 登录 */
    public login(onLogin: Function, uid: string) {
        onLogin();
    }

    /**
     * 屏幕震动
     * @param type 震动类型
     */
    public vibrate(type: "long" | "short") {
        console.log("vibrate", type);
    }

    /**
     * @deprecated 准备废弃。推荐使用 showReawrdAd。
     * 广告展示
     * @param {ADCallback} onSuc
     * @param {Function} onNotSupport 当前平台不支持广告展示时调用
     * @param {String} posId - 广告位id。如不需要传null即可
     */
    public showAD(onSuc: ADCallback, onNotSupport: () => void, posId: string) {
        this.showRewardAd({
            suc: () => onSuc(true),
            fail: () => onSuc(false),
            notSupport: onNotSupport,
            posId,
        })
    }

    /**
     * 播放激励广告
     * @param arg 参数
     * @param arg.suc 播放成功回调
     * @param arg.fail 播放失败回调 errcode 0：广告播放中途退出 -1：广告加载中
     * @param arg.notSupport 平台不支持回调
     * @param arg.posId 广告位id。建议配置在 AdCfg 中，如不需要传null即可
     */
    public showRewardAd(arg: {
        suc: () => void,
        fail?: (errcode: number) => void,
        notSupport?: () => void,
        posId: string,
    }) {
        this.testShowADCount++;
        if (this.testShowADCount == 1) {
            console.log("【测试】奖励成功")
            arg.suc()
        } else if (this.testShowADCount == 2) {
            console.log("【测试】平台不支持")
            arg.notSupport?.();
        } else if (this.testShowADCount == 3) {
            console.log("【测试】广告播放中途退出")
            arg.fail?.(0)
        } else {
            console.log("【测试】广告加载中")
            arg.fail?.(-1)
            this.testShowADCount = 0;
        }
    }

    /**
     * 播放Banner广告 
     * @param arg - 参数
     * @param arg.posId - 广告位id。建议配置在 AdCfg 中，如不需要传null即可
     * @param arg.pos - 广告位置
     * @param arg.wxArg - [可选] 微信平台参数
     * @param arg.wxArg.width - 广告条宽度,屏幕的百分比,取值范围0-1;
     * @param arg.wxArg.bottom - 广告条距离屏幕底部的高度,单位是像素;
     */
    public showBannerAd(arg: {
        posId: string,
        pos: "top" | "bottom",
        wxArg?: {
            width: number,
            bottom: number,
        }
    }) {
        console.log("PlatBase.showBannerAd posId," + arg.posId + ",pos," + arg.pos);
        console.log("PlatBase.showBannerAd wxArg", arg.wxArg?.width, arg.wxArg?.bottom);
    }

    /**
     * 隐藏当前的Banner广告
     */
    public hideBannerAd() {
        console.log("hideBannerAd");
    }

    /**
     * 播放插屏广告
     * @param arg - 参数
     * @param arg.posId - 广告位id。建议配置在 AdCfg 中，如不需要传null即可
     */
    public showInterstitialAd(arg: {
        posId: string,
    }) {
        console.log("playInterstitialAd ", arg.posId);
    }

    /**
     * 退出游戏
     */
    public endGame() {
        game.end();
    }

    /**
     * 进入游戏中心
     */
    public enterGameCenter() {
        console.log("enterGameCenter")
    }

    /**
     * 设置加载页进度
     * @param {number} pro - progress 0-99 当前进度
     */
    public setLoadingProgress(pro: number) {
        console.log("setLoadingProgress " + pro);
    }

    /**
     * 隐藏游戏加载页面
     */
    public loadingComplete() {
        console.log("loadingComplete");
    }

    /**
     * 确认平台准备完毕
     */
    public checkPlatReady(onReady: (env: "Debug" | "Release") => void) {
        onReady("Release");
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
        console.log("startRecord. do nothings", options)
    }

    /**
     * 暂停录屏
     */
    public pauseRecord() {
        console.log("pauseRecords. do nothings")
    }

    /**
     * 恢复录屏
     */
    public resumeRecord() {
        console.log("resumeRecord. do nothings")
    }

    /**
     * 结束录屏
     */
    public stopRecord(onStop: (res: any) => void) {
        console.log("stopRecord. do nothings");
        onStop("");
    }

    /**
     * 分享录屏
     * @param options 
     * @param {string} options.vidPath - 录屏的地址
     */
    public shareRecord(options: Plat_ShareRecordOption) {
        console.log("shareRecord. do nothings", options)
    }

    public get uma(): UMA {
        return window.uma
    }

    protected get curTime() {
        return Date.now();
    }

    public get plat(): GamePlat {
        return GamePlat.dev;
    }

    public get channel(): Channel {
        return Channel.Default;
    }
}


