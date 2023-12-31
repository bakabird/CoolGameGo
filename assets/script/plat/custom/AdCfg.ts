// ！不要删除也不要修改这个注释！ Version 3
import { BYTEDANCE, HUAWEI, NATIVE, OPPO, VIVO, WECHAT, XIAOMI } from "cc/env";
import { sys } from "cc";
import Plat from "../Plat";
import { Channel } from "../PlatBase";

export default {
    rwdAdDefault: "",
    bannerDefault: "",
    interstitialAdHalf: "",
    interstitialAdFull: "",

    /**
     * 在对应平台下的配置
     */
    // 安卓Oppo
    _and_oppo_: {
        rwdAdDefault: "779263",
        bannerDefault: "863207",
        interstitialAdHalf: "866727",
        interstitialAdFull: "867401",
    },
    // 安卓小米
    _and_mi_: {
    },
    // 安卓Vivo
    _and_vivo_: {
    },
    // 安卓Taptap
    _and_taptap_: {
    },
    // 安卓应用宝
    _and_yyb_: {
    },
    // 字节跳动：抖音
    _tt_: {
    },
    // 华为
    _hw_: {
    },
    // 微信小游戏
    _wx_: {
    },
    // vivo小游戏
    _vivo_: {
    },
    // oppo小游戏
    _oppo_: {
        bannerDefault: "870034",
        rwdAdDefault: "791152",
    },
    // 小米小游戏
    _mi_: {
    },
    // ios
    _ios_: {
    },


    /**
     * 根据平台调整配置
     */
    ajustByPlat() {
        let plat = "default";
        if (NATIVE && sys.os === sys.OS.ANDROID) {
            // if (true) {
            switch (Plat.inst.channel) {
                case Channel.Oppo: plat = "and_oppo";
                    break;
                case Channel.Mi: plat = "and_mi";
                    break;
                case Channel.Vivo: plat = "and_vivo";
                    break;
                case Channel.YYB: plat = "and_yyb";
                    break;
                case Channel.Taptap: plat = "and_taptap";
                    break;
            }
        } else if (NATIVE && sys.os === sys.OS.IOS) {
            plat = "ios";
        } else if (WECHAT) {
            plat = "wx";
        } else if (BYTEDANCE) {
            plat = "tt"
        } else if (HUAWEI) {
            plat = "hw"
        } else if (VIVO) {
            plat = "vivo";
        } else if (OPPO) {
            plat = "oppo";
        } else if (XIAOMI) {
            plat = "mi";
        }

        const base = this;
        const platCfg = base[`_${plat}_`];
        if (platCfg) {
            for (const key in platCfg) {
                if (Object.prototype.hasOwnProperty.call(platCfg, key)) {
                    const value = platCfg[key];
                    base[key] = value;
                }
            }
        }
    }
}
