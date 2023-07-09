"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
// let fs = require('fire-fs');
const fs_extra_1 = __importDefault(require("fs-extra"));
// let path = require('fire-path');
const path_1 = __importDefault(require("path"));
// let electron = require('electron');
const electron_1 = __importDefault(require("electron"));
class CfgUtil {
    static initCfg(cb) {
        let configFilePath = this._getAppCfgPath();
        let b = fs_extra_1.default.existsSync(configFilePath);
        if (b) {
            console.log("cfg path: " + configFilePath);
            fs_extra_1.default.readFile(configFilePath, 'utf-8', (err, data) => {
                if (!err) {
                    let saveData = JSON.parse(data.toString());
                    this.cfgData = saveData;
                    if (cb) {
                        cb(saveData);
                    }
                }
            });
        }
        else {
            if (cb) {
                cb(null);
            }
        }
    }
    static saveCfgByData(data) {
        Object.keys(data).forEach(v => {
            this.cfgData[v] = data[v];
        });
        this._save();
    }
    static _save() {
        let savePath = this._getAppCfgPath();
        fs_extra_1.default.writeFileSync(savePath, JSON.stringify(this.cfgData));
    }
    static _getAppCfgPath() {
        let userDataPath = null;
        if (electron_1.default.remote) {
            userDataPath = electron_1.default.remote.app.getPath('userData');
        }
        else {
            userDataPath = electron_1.default.app.getPath('userData');
        }
        let tar = Editor.App.path;
        tar = tar.replace(/\\/g, '-');
        tar = tar.replace(/:/g, '-');
        tar = tar.replace(/\//g, '-');
        return path_1.default.join(userDataPath, "plat-helper-" + tar + ".json");
    }
}
exports.default = CfgUtil;
CfgUtil.cfgData = {
    platTSDirPath: "project://", // ts脚本根路径
};
// module.exports = {
//     cfgData: {
//         excelRootPath: null,// excel根路径
//     },
//     initCfg(cb) {
//         let configFilePath = this._getAppCfgPath();
//         let b = fs.existsSync(configFilePath);
//         if (b) {
//             console.log("cfg path: " + configFilePath);
//             fs.readFile(configFilePath, 'utf-8', function (err, data) {
//                 if (!err) {
//                     let saveData = JSON.parse(data.toString());
//                     this.cfgData = saveData;
//                     if (cb) {
//                         cb(saveData);
//                     }
//                 }
//             }.bind(this));
//         } else {
//             if (cb) {
//                 cb(null);
//             }
//         }
//     },
//     saveCfgByData(data) {
//         Object.keys(data).forEach(v => {
//             this.cfgData[v] = data[v];
//         });
//         this._save();
//     },
//     _save() {
//         let savePath = this._getAppCfgPath();
//         fs.writeFileSync(savePath, JSON.stringify(this.cfgData));
//     },
//     _getAppCfgPath() {
//         let userDataPath = null;
//         if (electron.remote) {
//             userDataPath = electron.remote.app.getPath('userData');
//         } else {
//             userDataPath = electron.app.getPath('userData');
//         }
//         let tar = Editor.libraryPath;
//         tar = tar.replace(/\\/g, '-');
//         tar = tar.replace(/:/g, '-');
//         tar = tar.replace(/\//g, '-');
//         return path.join(userDataPath, "excel-fucker-" + tar + ".json");
//     },
// };
