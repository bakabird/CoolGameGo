"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const fs_extra_1 = __importDefault(require("fs-extra"));
const child_process_1 = __importDefault(require("child_process"));
const FsUtil_1 = __importDefault(require("./FsUtil"));
function log(...arg) {
    return console.log(`[UmKit] `, ...arg);
}
class UmKit {
    static get me() {
        if (!this._me) {
            this._me = new UmKit();
        }
        return this._me;
    }
    _mkDir(path) {
        if (fs_extra_1.default.existsSync(path)) {
            var tempstats = fs_extra_1.default.statSync(path);
            if (!tempstats.isDirectory()) {
                fs_extra_1.default.unlinkSync(path);
                fs_extra_1.default.mkdirSync(path);
            }
        }
        else {
            fs_extra_1.default.mkdirSync(path);
        }
    }
    checkPlatform(platform) {
        return platform == "oppo-mini-game"
            || platform == "vivo-mini-game"
            || platform == "wechatgame"
            || platform == "huawei-quick-game"
            || platform == "xiaomi-quick-game"
            || platform == "bytedance-mini-game";
    }
    checkUMEnable(option) {
        return option.packages['plat-helper'].umEnable;
    }
    attach2MiniGame(options, result) {
        const pjoin = Editor.Utils.Path.join;
        const mkDir = this._mkDir;
        const joinPack = (...arg) => {
            return pjoin(__dirname, "../", ...arg);
        };
        let dir = result.paths.dir;
        if (options.platform == "vivo-mini-game") {
            dir = pjoin(dir, "src");
        }
        let gameJsPath = pjoin(dir, "game.js");
        let path = joinPack("static/uma/utils");
        let requirePath = "import uma from \"./utils";
        let uploadUserInfo = false;
        switch (options.platform) {
            case "wechatgame":
                if (options.packages["build-plugin-exportconfig"] && options.packages["build-plugin-exportconfig"].platform == "qq") {
                    path = pjoin(path, "umtrack-qq-game");
                    requirePath = "uma = require(\"/utils/umtrack/lib/uma.min.js\");\n" +
                        "qq.uma = uma;\n";
                    break;
                }
                if (options.packages["build-plugin-exportconfig"] && options.packages["build-plugin-exportconfig"].platform == "kuaishou") {
                    path = pjoin(path, "umtrack-kuaishou");
                    requirePath = requirePath + "/umtrack/lib/uma.min.js\"";
                    break;
                }
                path = pjoin(path, "umtrack-wx-game");
                let require = requirePath;
                requirePath = require + "/umtrack/lib/index.js\"";
                break;
            case "xiaomi-quick-game":
                gameJsPath = pjoin(dir, "main.js");
                path = pjoin(path, "umtrack-quickgame");
                requirePath = "require('./utils/umtrack/lib/uma.min.js')\n" +
                    "var uma = qg.uma";
                break;
            case "oppo-mini-game":
                gameJsPath = pjoin(dir, "main.js");
                ;
                path = pjoin(path, "umtrack-quickgame");
                requirePath = "require('./utils/umtrack/lib/uma.min.js')";
                break;
            case "huawei-quick-game":
                path = pjoin(path, "umtrack-quickgame");
                requirePath = "require('./utils/umtrack/lib/uma.min.js')";
                break;
            case "vivo-mini-game":
                path = pjoin(path, "umtrack-quickgame");
                requirePath = "require('./utils/umtrack/lib/uma.min.js')\n" +
                    "var uma = qg.uma";
                break;
            case "bytedance-mini-game":
                path = pjoin(path, "umtrack-tt-game");
                requirePath = `var uma = require('./utils/umtrack/lib/uma.min')`;
                uploadUserInfo = true;
                break;
            default:
                return;
        }
        let targetPath = pjoin(dir, "utils");
        mkDir(targetPath);
        targetPath = pjoin(targetPath, "umtrack");
        mkDir(targetPath);
        // @ts-ignore
        let cmdStr = process.platform == "darwin" ? `cp -a -f ${path}/* ${targetPath}`
            : 'echo d|xcopy ' + path + ' ' + targetPath + " /s/y";
        log("copy cmd:" + cmdStr);
        child_process_1.default.spawn(process.platform == "darwin" ? "/bin/bash" : "cmd.exe", (process.platform == "darwin" ? ['-c'] : ['/s', '/c']).concat([cmdStr]));
        let content = fs_extra_1.default.readFileSync(gameJsPath, 'utf8').toString();
        content = `${requirePath};
    uma.init({
      appKey: '${options.packages["plat-helper"].umId}',
      useOpenid: ${options.packages["plat-helper"].umUseOpenId}, // default true
      autoGetOpenid: ${options.packages["plat-helper"].umUseOpenId},
      debug: ${options.packages["plat-helper"].umDebug},
      uploadUserInfo: ${uploadUserInfo},
    });\n` + content;
        fs_extra_1.default.writeFileSync(gameJsPath, content);
    }
    libandFilesWalk(enable, libandoirdPath) {
        FsUtil_1.default.walkDirectoryFiles(libandoirdPath, (path) => {
            const file = fs_extra_1.default.readFileSync(path);
            let content = file.toString();
            if (enable) {
                content = content.replace(/#IFUM|#ENDUM/g, "");
            }
            else {
                content = content.replace(/#IFUM[\s\S]*?#ENDUM/g, "");
            }
            fs_extra_1.default.writeFileSync(path, content);
        });
    }
}
exports.default = UmKit;
