import { sys } from "cc";
import { BYTEDANCE, DEV, HUAWEI, NATIVE, OPPO, VIVO, WECHAT, XIAOMI } from "cc/env";
import PlatAnd from "./PlatAnd";
import PlatBase from "./PlatBase";
import PlatIOS from "./PlatIOS";
import PlatMi from "./PlatMi";
import PlatOPPO from "./PlatOPPO";
import PlatVIVO from "./PlatVIVO";
import PlatWX from "./PlatWX";
import PlatWeb from "./PlatWeb";
import PlatHW from "./PlatHW";
import PlatTT from "./PlatTT";

export default class Plat {
    public static forceUsePlatWeb: boolean = false
    public static get inst(): PlatBase {
        if (Plat.forceUsePlatWeb) return PlatWeb.getInstance();
        if (DEV) return PlatWeb.getInstance();
        if (WECHAT) {
            return PlatWX.getInstance();
        } else if (NATIVE && sys.os === sys.OS.ANDROID) {
            return PlatAnd.getInstance();
        } else if (NATIVE && sys.os === sys.OS.IOS) {
            return PlatIOS.getInstance();
        } else if (VIVO) {
            return PlatVIVO.getInstance();
        } else if (XIAOMI) {
            return PlatMi.getInstance();
        } else if (OPPO) {
            return PlatOPPO.getInstance();
        } else if (HUAWEI) {
            return PlatHW.getInstance();
        } else if (BYTEDANCE) {
            return PlatTT.getInstance();
        } else {
            return PlatWeb.getInstance();
        }
    }
    public static get isAndroid() {
        return NATIVE && sys.os === sys.OS.ANDROID
    }
    public static get isIos() {
        return NATIVE && sys.os === sys.OS.IOS
    }

}