"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const fs = __importStar(require("fs-extra"));
const path_1 = __importDefault(require("path"));
const vue_1 = require("vue");
const CfgUtil_1 = __importDefault(require("../../CfgUtil"));
const join = path_1.default.join;
const joinPack = (...arg) => {
    return join(__dirname, '../../../', ...arg);
};
const panelDataMap = new WeakMap();
console.log("default panel init");
/**
 * @zh 如果希望兼容 3.3 之前的版本可以使用下方的代码
 * @en You can add the code below if you want compatibility with versions prior to 3.3
 */
// Editor.Panel.define = Editor.Panel.define || function(options: any) { return options }
module.exports = Editor.Panel.define({
    listeners: {
        show() {
            console.log('show');
        },
        hide() {
            console.log('hide');
        }
    },
    template: fs.readFileSync(joinPack("static/template/default/index.html"), 'utf-8'),
    style: fs.readFileSync(joinPack('static/style/default/index.css'), 'utf-8'),
    $: {
        app: '#app',
        logTextArea: '#logTextArea'
    },
    methods: {},
    ready() {
        let logCtrl = this.$.logTextArea;
        let logListScrollToBottom = function () {
            setTimeout(function () {
                logCtrl.scrollTop = logCtrl.scrollHeight;
            }, 10);
        };
        const app = (0, vue_1.createApp)({});
        app.config.compilerOptions.isCustomElement = (tag) => tag.startsWith('ui-');
        app.component('MyApp', {
            template: fs.readFileSync(joinPack('static/template/vue/app.html'), 'utf-8'),
            setup() {
                const tsDir = (0, vue_1.ref)(0);
                // 返回值会暴露给模板和其他的选项式 API 钩子
                return {
                    tsDir,
                };
            },
            data() {
                return {
                    logView: "",
                    platTSDirPath: "project://",
                };
            },
            watch: {
                logView(val, oldVal) {
                    logCtrl.value = val;
                }
            },
            computed: {
                rawPlatTSDirPath() {
                    if (this.platTSDirPath) {
                        return Editor.UI.File.resolveToRaw(this.platTSDirPath);
                    }
                    else {
                        return null;
                    }
                },
            },
            mounted() {
                this._initPluginCfg();
            },
            methods: {
                _initPluginCfg() {
                    CfgUtil_1.default.initCfg((data) => {
                        this.tsDir.protocol = "project";
                        if (data) {
                            this._addLog("confirm config path: " + JSON.stringify(data));
                            this.platTSDirPath = data.platTSDirPath;
                            this.tsDir.value = this.platTSDirPath;
                        }
                    });
                },
                _addLog(str) {
                    let time = new Date();
                    this.logView += "[" + time.toLocaleString() + "]: " + str + "\n";
                    logListScrollToBottom();
                },
                onConfirmPlatPath(dir) {
                    this._addLog("confirm config path: " + dir);
                    this.platTSDirPath = dir;
                    CfgUtil_1.default.saveCfgByData({ platTSDirPath: this.platTSDirPath });
                },
                onClickCreateAdCfgTS() {
                    this._addLog("onClickCreateAdCfgTS");
                    if (this.rawPlatTSDirPath) {
                        if (fs.existsSync(this.rawPlatTSDirPath)) {
                            const path = join(this.rawPlatTSDirPath, "custom/AdCfg.ts");
                            if (!fs.existsSync(path)) {
                                fs.copyFileSync(joinPack("static/asset/AdCfg.ts.txt"), path);
                                Editor.Message.send("asset-db", "refresh-asset", 'db://assets/');
                                this._addLog("AdCfg.ts 创建成功！");
                            }
                            else {
                                this._addLog("AdCfg.ts 创建失败！err:" + 2);
                            }
                        }
                        else {
                            this._addLog("AdCfg.ts 创建失败！err:" + 1);
                        }
                    }
                    else {
                        this._addLog("AdCfg.ts 创建失败！err:" + 0);
                    }
                },
                onClickUpdateAdCfgTS() {
                    var _a, _b;
                    this._addLog("onClickUpdateAdCfgTS");
                    if (this.rawPlatTSDirPath) {
                        if (fs.existsSync(this.rawPlatTSDirPath)) {
                            const path = join(this.rawPlatTSDirPath, "custom/AdCfg.ts");
                            if (fs.existsSync(path)) {
                                let content = fs.readFileSync(path, "utf-8");
                                // get the version
                                const matchRlt = content.match(/！不要删除也不要修改这个注释！ Version (\d*)/);
                                const version = parseInt((_b = (_a = matchRlt === null || matchRlt === void 0 ? void 0 : matchRlt[1]) === null || _a === void 0 ? void 0 : _a.trim()) !== null && _b !== void 0 ? _b : "0");
                                const pack = { content };
                                const latestVersion = this._updateAdCfg(version, pack);
                                if (latestVersion < 0) {
                                    this._addLog("AdCfg.ts 更新失败！请手动更新！");
                                    this._addLog("AdCfg.ts 更新失败！请手动更新！");
                                    this._addLog("AdCfg.ts 更新失败！请手动更新！");
                                    this._addLog("AdCfg.ts 更新失败！请手动更新！");
                                    this._addLog("AdCfg.ts 更新失败！请手动更新！");
                                    this._addLog("AdCfg.ts 更新失败！请手动更新！");
                                }
                                else {
                                    if (version == 0) {
                                        pack.content = `// ！不要删除也不要修改这个注释！ Version ${latestVersion}\n` + pack.content;
                                    }
                                    else {
                                        pack.content.replace(/！不要删除也不要修改这个注释！ Version (\d*)/, `！不要删除也不要修改这个注释！ Version ${latestVersion}`);
                                    }
                                    fs.writeFileSync(path, pack.content, "utf-8");
                                    Editor.Message.send("asset-db", "refresh-asset", 'db://assets/');
                                    this._addLog(`AdCfg.ts 成功更新到版本！${latestVersion}`);
                                }
                            }
                            else {
                                this._addLog("AdCfg.ts 更新失败！err: AdCfg.ts 文件不存在");
                            }
                        }
                        else {
                            this._addLog("AdCfg.ts 更新失败！err: platTSDirPath 对应文件夹不存在");
                        }
                    }
                    else {
                        this._addLog("AdCfg.ts 更新失败！err: 尚未设置 platTSDirPath");
                    }
                },
                _updateAdCfg(curVersion, contentPack) {
                    let tmpNum = 0;
                    const lastestVersion = 2;
                    let lines = contentPack.content.split("\n");
                    if (curVersion < 1) {
                        // 0 -> 1
                        // 找到 微信小游戏
                        tmpNum = lines.findIndex(line => line.includes("微信小游戏"));
                        if (tmpNum < 0)
                            return -1;
                        // 塞入 
                        lines.splice(tmpNum, 0, "    // 安卓应用宝", "    _and_yyb_: {", "    },");
                        // 找到 "and_taptap"
                        tmpNum = lines.findIndex(line => line.includes(`"and_taptap"`));
                        if (tmpNum < 0)
                            return -1;
                        // 塞入 
                        lines.splice(tmpNum, 0, `                case GameEnv.YYB: plat = "and_yyb";`, "                    break;");
                    }
                    if (curVersion < 2) {
                        // 1 -> 2
                        // 更新引用
                        lines.splice(1, 3, `import { NATIVE, OPPO, VIVO, WECHAT, XIAOMI } from "cc/env";`, `import { sys } from "cc";`, `import Plat from "../Plat";`, `import { Channel } from "../PlatBase";`);
                        // 调整 GameEnv 到 Channel
                        let content = lines.join("\n");
                        content = content.replace(/PlatCfg.env/g, "Plat.inst.channel");
                        content = content.replace(/GameEnv.Oppo/g, "Channel.Oppo");
                        content = content.replace(/GameEnv.Mi/g, "Channel.Mi");
                        content = content.replace(/GameEnv.Vivo/g, "Channel.Vivo");
                        content = content.replace(/GameEnv.YYB/g, "Channel.YYB");
                        content = content.replace(/GameEnv.Taptap/g, "Channel.Taptap");
                        lines = content.split("\n");
                    }
                    contentPack.content = lines.join("\n");
                    // remove Version
                    return lastestVersion;
                },
            },
        });
        app.mount(this.$.app);
        panelDataMap.set(this, app);
    },
    beforeClose() { },
    close() {
        const app = panelDataMap.get(this);
        if (app) {
            app.unmount();
        }
    }
});
