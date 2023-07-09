"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const fs_extra_1 = __importDefault(require("fs-extra"));
class FsUtil {
    static walkDirectoryFiles(dir, walk) {
        let files = fs_extra_1.default.readdirSync(dir);
        files.forEach(item => {
            let filepath1 = dir + '\\' + item;
            let stat = fs_extra_1.default.statSync(filepath1);
            if (stat.isFile()) {
                console.log(filepath1);
                walk(filepath1);
            }
            else {
                FsUtil.walkDirectoryFiles(filepath1, walk);
            }
        });
    }
}
exports.default = FsUtil;
