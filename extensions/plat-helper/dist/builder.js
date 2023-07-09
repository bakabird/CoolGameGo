"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.assetHandlers = exports.configs = exports.checkAndEnable = exports.Channel = exports.unload = exports.load = void 0;
const load = function () {
    console.debug('cocos-build-template load');
};
exports.load = load;
const unload = function () {
    console.debug('cocos-build-template unload');
};
exports.unload = unload;
var Channel;
(function (Channel) {
    Channel["Taptap"] = "Taptap";
    Channel["Oppo"] = "Oppo";
    Channel["Vivo"] = "Vivo";
    Channel["Mi"] = "Mi";
    Channel["YYB"] = "YYB";
    Channel["Default"] = "Default";
})(Channel = exports.Channel || (exports.Channel = {}));
function checkAndEnable(option) {
    return option.platform == "android" && option.packages['plat-helper'].and_enable;
}
exports.checkAndEnable = checkAndEnable;
const verify_rule_map = {
    ruleOppo: {
        message: 'Oppo环境下必填，非Oppo环境下必须为空',
        func(val, option) {
            if (!checkAndEnable(option))
                return true;
            if (typeof val != "string")
                return false;
            const curEnv = option.packages['plat-helper'].and_channel;
            return (curEnv == Channel.Oppo && val != "") ||
                (curEnv != Channel.Oppo && val == "");
        }
    },
    ruleMi: {
        message: 'Mi环境下必填，非Mi环境下必须为空',
        func(val, option) {
            if (!checkAndEnable(option))
                return true;
            if (typeof val != "string")
                return false;
            const curEnv = option.packages['plat-helper'].and_channel;
            return (curEnv == Channel.Mi && val != "") ||
                (curEnv != Channel.Mi && val == "");
        }
    },
    ruleTapTap: {
        message: 'Taptap环境下必填，非Taptap环境下必须为空',
        func(val, option) {
            if (!checkAndEnable(option))
                return true;
            if (typeof val != "string")
                return false;
            const curEnv = option.packages['plat-helper'].and_channel;
            return (curEnv == Channel.Taptap && val != "") ||
                (curEnv != Channel.Taptap && val == "");
        }
    },
    ruleVivo: {
        message: 'Vivo环境下必填，非Vivo环境下必须为空',
        func(val, option) {
            if (!checkAndEnable(option))
                return true;
            if (typeof val != "string")
                return false;
            const curEnv = option.packages['plat-helper'].and_channel;
            return (curEnv == Channel.Vivo && val != "") ||
                (curEnv != Channel.Vivo && val == "");
        }
    },
    ruleYYb: {
        message: '应用宝环境下必填，非应用宝环境下必须为空',
        func(val, option) {
            if (!checkAndEnable(option))
                return true;
            if (typeof val != "string")
                return false;
            const curEnv = option.packages['plat-helper'].and_channel;
            return (curEnv == Channel.YYB && val != "") ||
                (curEnv != Channel.YYB && val == "");
        }
    },
    ruleAndEnable: {
        message: '必须填写',
        func(val, option) {
            if (!checkAndEnable(option))
                return true;
            return typeof val == "string" && val != "";
        }
    },
};
function produceVariable(key, rule) {
    return {
        label: key,
        description: verify_rule_map[rule].message,
        default: '',
        render: {
            ui: "ui-input",
        },
        verifyRules: [rule]
    };
}
// CSJ_APP_ID="5342222"
// CSJ_APP_NAME="ZEROSS"
// TAPTAP_CLIENT_ID="12321"
const plat_option = {
    umEnable: {
        label: '[友盟]启用',
        default: false,
        render: {
            ui: 'ui-checkbox',
        },
    },
    umId: {
        label: '[友盟]友盟id',
        default: "",
        render: {
            ui: 'ui-input',
        },
    },
    umDebug: {
        label: '[友盟]调试模式',
        default: false,
        render: {
            ui: 'ui-checkbox',
        },
    },
    umUseOpenId: {
        label: '[友盟]是否使用openId模式',
        default: false,
        render: {
            ui: 'ui-checkbox',
        },
    },
    BR_BELOW_AND: {
        label: "· · · · · · ·",
        default: "<---下面参数仅在打安卓包时生效--->",
        render: {
            ui: 'ui-input',
            attributes: {
                disabled: true,
            },
        },
    },
    and_enable: {
        label: "[安卓]启用",
        default: false,
        render: {
            ui: "ui-checkbox",
        },
    },
    and_channel: {
        label: "渠道",
        render: {
            ui: "ui-select",
            attributes: {
                placeholder: "选择对应渠道",
            },
            items: [
                Channel.Taptap, Channel.Oppo, Channel.Mi,
                Channel.Vivo, Channel.YYB, Channel.Default,
            ].map(e => ({
                value: e,
                label: e,
            }))
        }
    },
    and_enableAd: {
        label: "开启广告",
        description: "目前仅应用宝可关闭广告。",
        default: true,
        render: {
            ui: "ui-checkbox",
        },
    },
    PROTOCOL_URL: {
        label: "用户协议URL",
        default: '',
        render: {
            ui: "ui-input",
        },
        verifyRules: ["ruleAndEnable"]
    },
    PRIVATE_URL: {
        label: "隐私声明URL",
        default: '',
        render: {
            ui: "ui-input",
        },
        verifyRules: ["ruleAndEnable"]
    },
    /**
     * OPPO 配置
     */
    OPPO_APP_ID: produceVariable("OPPO_APP_ID", "ruleOppo"),
    OPPO_APP_KEY: produceVariable("OPPO_APP_KEY", "ruleOppo"),
    OPPO_APP_SECRET: produceVariable("OPPO_APP_SECRET", "ruleOppo"),
    /**
     * MI 配置
     */
    MI_APP_ID: produceVariable("MI_APP_ID", "ruleMi"),
    MI_APP_KEY: produceVariable("MI_APP_KEY", "ruleMi"),
    MI_APP_NAME: produceVariable("MI_APP_NAME", "ruleMi"),
    /**
     * TAPTAP 配置
     */
    TAPTAP_CLIENT_ID: produceVariable("TAPTAP_CLIENT_ID", "ruleTapTap"),
    CSJ_APP_ID: produceVariable("穿山甲APP_ID(taptap必填)", "ruleTapTap"),
    CSJ_APP_NAME: produceVariable("穿山甲APP_NAME(taptap必填)", "ruleTapTap"),
    CSJ_BANNER_WIDTH: produceVariable("穿山甲Banner宽度(taptap必填)", "ruleTapTap"),
    CSJ_BANNER_HEIGHT: produceVariable("穿山甲Banner高度(taptap必填)", "ruleTapTap"),
    /**
     * VIVO 配置
     */
    VIVO_APP_ID: produceVariable("VIVO_APP_ID", "ruleVivo"),
    VIVO_AD_MEDIA_ID: produceVariable("VIVO_AD_MEDIA_ID", "ruleVivo"),
};
exports.configs = {
    '*': {
        hooks: './hooks',
        options: plat_option,
        verifyRuleMap: verify_rule_map,
    },
};
exports.assetHandlers = './asset-handlers';
