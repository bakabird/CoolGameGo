"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.onAfterMake = exports.onBeforeMake = exports.unload = exports.onAfterBuild = exports.onAfterCompressSettings = exports.onBeforeCompressSettings = exports.onBeforeBuild = exports.load = exports.throwError = void 0;
const fs_extra_1 = require("fs-extra");
const builder_1 = require("./builder");
// @ts-ignore
const package_json_1 = __importDefault(require("../package.json"));
const path_1 = __importDefault(require("path"));
const fs_extra_2 = __importDefault(require("fs-extra"));
const CfgUtil_1 = __importDefault(require("./CfgUtil"));
const UmKit_1 = __importDefault(require("./UmKit"));
let rawPlatTSDir = "";
const PACKAGE_NAME = 'plat-helper';
const Cifum = "#IFUM";
const Cendum = "#ENDUM";
const joinPack = (...arg) => {
    return path_1.default.join(__dirname, "../", ...arg);
};
function log(...arg) {
    return console.log(`[${PACKAGE_NAME}] `, ...arg);
}
exports.throwError = true;
const load = async function () {
    console.log(`[${PACKAGE_NAME}] Load cocos plugin example in builder.`);
    console.log("packageJSON.DEBUG_WORD=" + package_json_1.default.DEBUG_WORD);
    CfgUtil_1.default.initCfg(data => {
        rawPlatTSDir = Editor.UI.File.resolveToRaw(data.platTSDirPath);
        console.log("rawPlatTSDir", rawPlatTSDir);
    });
    // allAssets = await Editor.Message.request('asset-db', 'query-assets');
};
exports.load = load;
const onBeforeBuild = async function (options) {
    if (!(0, builder_1.checkAndEnable)(options))
        return;
    console.log(options);
    // revisePlatCfg(options);
};
exports.onBeforeBuild = onBeforeBuild;
const onBeforeCompressSettings = async function (options, result) {
};
exports.onBeforeCompressSettings = onBeforeCompressSettings;
const onAfterCompressSettings = async function (options, result) {
};
exports.onAfterCompressSettings = onAfterCompressSettings;
const onAfterBuild = async function (options, result) {
    if (UmKit_1.default.me.checkPlatform(options.platform) && UmKit_1.default.me.checkUMEnable(options)) {
        UmKit_1.default.me.attach2MiniGame(options, result);
    }
    if ((0, builder_1.checkAndEnable)(options)) {
        attachASProj(options);
        attachLiband(options);
        attachPlatVariable(options);
        attachChannel2Mainjs(options, result);
        reviseGradleProp(options, result);
        reviseGradleVersion(options);
    }
};
exports.onAfterBuild = onAfterBuild;
const unload = async function () {
};
exports.unload = unload;
const onBeforeMake = async function (root, options) {
};
exports.onBeforeMake = onBeforeMake;
const onAfterMake = async function (root, options) {
};
exports.onAfterMake = onAfterMake;
function getOrientation(options) {
    const orientationOption = options.packages.android.orientation;
    return (orientationOption.landscapeLeft && orientationOption.landscapeRight) ? "sensorLandscape" :
        (orientationOption.landscapeLeft ? "reverseLandscape" : (orientationOption.landscapeRight ? "landscape" : "portrait"));
}
function revisePlatCfg(options) {
    if (rawPlatTSDir == "")
        return;
    const { and_channel: channel } = options.packages['plat-helper'];
    const GameCfgPath = Editor.Utils.Path.join(rawPlatTSDir, "./PlatCfg.ts");
    const cfgCnt = (0, fs_extra_1.readFileSync)(GameCfgPath, "utf-8");
    const cfgLines = cfgCnt.split("\n");
    for (let index = 0; index < cfgLines.length; index++) {
        const line = cfgLines[index];
        if (line.indexOf("env:") > -1) {
            cfgLines[index] = `env: GameEnv.${channel},`;
        }
    }
    (0, fs_extra_1.writeFileSync)(GameCfgPath, cfgLines.join("\n"));
    log("已修改 PlatCfg.ts " + `env ${channel}`);
}
function attachASProj(options) {
    if (options.platform != "android")
        return;
    const { and_channel: channel } = options.packages['plat-helper'];
    const pjoin = Editor.Utils.Path.join;
    const projPath = pjoin(options.buildPath.replace("project://", Editor.Project.path + "\\"), options.outputName, "proj");
    const propPath = pjoin(projPath, "gradle.properties");
    const propCnt = (0, fs_extra_1.readFileSync)(propPath, "utf-8");
    const ASProjPath = makesureASProj(options);
    let oldNDIR = "";
    let newNDIR = "";
    let propLines = propCnt.split("\n");
    for (let index = 0; index < propLines.length; index++) {
        const line = propLines[index];
        if (line.indexOf("NATIVE_DIR") > -1) {
            oldNDIR = line;
            propLines[index] = `NATIVE_DIR=${ASProjPath}`;
            // propLines[index] = propLines[index].replace("engine/android", "engine/" + options.outputName);
            newNDIR = propLines[index];
            break;
        }
    }
    (0, fs_extra_1.writeFileSync)(propPath, propLines.join("\n"));
    log("已将 AS工程 链接代码重定向 " + `${oldNDIR} -> ${newNDIR}`);
}
/**
 * 确保对应环境的平台安卓代码存在
 * @return {string} 对应平台安卓代码项目的路径
 */
function makesureASProj(options) {
    const pjoin = Editor.Utils.Path.join;
    const EnvAsProjPath = pjoin(Editor.Project.path, "native/engine/" + options.outputName).replace(/\\/g, "/");
    const enAndTempalte = joinPack("static/native-engine/" + options.outputName);
    if (!fs_extra_2.default.existsSync(EnvAsProjPath)) {
        if (fs_extra_2.default.existsSync(enAndTempalte)) {
            const baseAndPath = pjoin(Editor.Project.path, "native/engine/android").replace(/\\/g, "/");
            if (fs_extra_2.default.existsSync(baseAndPath)) {
                fs_extra_2.default.copySync(baseAndPath, EnvAsProjPath);
                // delete app dir;
                fs_extra_2.default.rmdirSync(pjoin(EnvAsProjPath, "app"), { recursive: true });
                // repalce by template
                fs_extra_2.default.copySync(pjoin(enAndTempalte, "app"), pjoin(EnvAsProjPath, "app"));
            }
            else {
                console.error("baseAnd not found in " + baseAndPath);
            }
        }
        else {
            console.error("没有找到对应的安卓工程模板，请检查是否存在 " + enAndTempalte);
        }
    }
    if (fs_extra_2.default.existsSync(EnvAsProjPath)) {
        const manifestPath = pjoin(EnvAsProjPath, "app/AndroidManifest.xml");
        const manifestCnt = (0, fs_extra_1.readFileSync)(manifestPath, "utf-8");
        (0, fs_extra_1.writeFileSync)(manifestPath, ajustMainfest(options, manifestCnt), "utf-8");
    }
    return EnvAsProjPath;
}
function ajustMainfest(options, content) {
    const { and_channel: channel } = options.packages['plat-helper'];
    if (channel == builder_1.Channel.Mi) {
        return ajustMainfestMi(options, content);
    }
    else if (channel == builder_1.Channel.Oppo) {
        return ajustMainfestOppo(options, content);
    }
    else if (channel == builder_1.Channel.Vivo) {
        return ajustMainfestVivo(options, content);
    }
    else if (channel == builder_1.Channel.Taptap) {
        return ajustMainfestTaptap(options, content);
    }
    else if (channel == builder_1.Channel.YYB) {
        return ajustMainfestYYB(options, content);
    }
    else {
        return content;
    }
}
function ajustMainfestVivo(options, content) {
    const orientation = getOrientation(options);
    content = content.replace(/android:screenOrientation=".*"/g, `android:screenOrientation="${orientation}"`);
    return content;
}
function ajustMainfestOppo(options, content) {
    const orientation = getOrientation(options);
    content = content.replace(/<meta-data android:name="app_key" .*/g, `<meta-data android:name="app_key" android:value="${options.packages['plat-helper'].OPPO_APP_KEY}" />`);
    content = content.replace(/android:screenOrientation=".*"/g, `android:screenOrientation="${orientation}"`);
    return content;
}
function ajustMainfestMi(options, content) {
    const orientation = getOrientation(options);
    content = content.replace(/<meta-data android:name="miGameAppId" .*/g, `<meta-data android:name="miGameAppId" android:value="mi_${options.packages['plat-helper'].MI_APP_ID}" />`);
    content = content.replace(/<meta-data android:name="miGameAppKey" .*/g, `<meta-data android:name="miGameAppKey" android:value="mi_${options.packages['plat-helper'].MI_APP_KEY}" />`);
    content = content.replace(/android:screenOrientation=".*"/g, `android:screenOrientation="${orientation}"`);
    return content;
}
function ajustMainfestTaptap(options, content) {
    const orientation = getOrientation(options);
    content = content.replace(/android:screenOrientation=".*"/g, `android:screenOrientation="${orientation}"`);
    return content;
}
function ajustMainfestYYB(options, content) {
    const orientation = getOrientation(options);
    content = content.replace(/android:screenOrientation=".*"/g, `android:screenOrientation="${orientation}"`);
    return content;
}
/**
 * 确保该 options 对应 orientation/umeng 的 liband 目录存在。如果没有将创建
 * @return {string} 对应的 liband 的路径
 */
function makesureLiband(options) {
    const umengEnable = UmKit_1.default.me.checkUMEnable(options);
    const orientation = getOrientation(options);
    const orientationLibandDir = `libandroid-${orientation}-um${umengEnable ? 1 : 0}`;
    const pjoin = Editor.Utils.Path.join;
    const libandoirdRootDir = pjoin(Editor.Project.path, "native/engine/libandroid").replace(/\\/g, "/");
    const libandoirdPath = pjoin(Editor.Project.path, "native/engine/libandroid/" + orientationLibandDir).replace(/\\/g, "/");
    console.log("libandoirdPath ", libandoirdPath);
    fs_extra_2.default.ensureDirSync(libandoirdRootDir);
    if (!fs_extra_2.default.existsSync(libandoirdPath)) {
        const baseLibandoirdPath = joinPack("static/libandroid");
        console.log("baseLibandoirdPath ", baseLibandoirdPath);
        // copy
        fs_extra_2.default.copySync(baseLibandoirdPath, libandoirdPath);
        UmKit_1.default.me.libandFilesWalk(umengEnable, libandoirdPath);
        // revise the Androimainfest.xml
        const mainfest = pjoin(libandoirdPath, "./AndroidManifest.xml");
        console.log("mainfest ", mainfest);
        let content = fs_extra_2.default.readFileSync(mainfest, "utf-8");
        content = content.replace(/android:screenOrientation="portrait"/g, `android:screenOrientation="${orientation}"`);
        fs_extra_2.default.writeFileSync(mainfest, content, "utf-8");
    }
    return libandoirdPath;
}
/**
 * 确保 liband 的引入
 * @param options
 * @returns
 */
function attachLiband(options) {
    if (options.platform != "android")
        return;
    const { and_channel: channel } = options.packages['plat-helper'];
    const pjoin = Editor.Utils.Path.join;
    const projPath = pjoin(options.buildPath.replace("project://", Editor.Project.path + "\\"), options.outputName, "proj");
    // const libandoirdPath = pjoin(Editor.Project.path, "native/engine/libandroid").replace(/\\/g, "/");
    const libandoirdPath = makesureLiband(options);
    const propPath = pjoin(projPath, "gradle.properties");
    const settingsPath = pjoin(projPath, "settings.gradle");
    const propCnt = (0, fs_extra_1.readFileSync)(propPath, "utf-8");
    const qhhzLibandoird = `LIB_DIR=${libandoirdPath}`;
    const settingsGradle = `include ':libcocos',':libservice', ':libandroid', ':LinesXFree'
project(':libcocos').projectDir     = new File(COCOS_ENGINE_PATH,'cocos/platform/android/libcocos2dx')
project(':libandroid').projectDir = new File(LIB_DIR)
project(':LinesXFree').projectDir    = new File(NATIVE_DIR, 'app')
if(PROP_ENABLE_INSTANT_APP == "true" || PROP_ENABLE_INSTANT_APP == "yes") {
    include ':instantapp'
    project(':instantapp').projectDir   = new File(NATIVE_DIR, 'instantapp')
}

rootProject.name = "LinesXFree"
    `;
    let propLines = propCnt.split("\n");
    for (let index = 0; index < propLines.length; index++) {
        const line = propLines[index];
        if (line.indexOf("LIB_DIR") > -1) {
            propLines.splice(index, 1);
            index--;
        }
    }
    propLines.push(qhhzLibandoird);
    (0, fs_extra_1.writeFileSync)(propPath, propLines.join("\n"));
    (0, fs_extra_1.writeFileSync)(settingsPath, settingsGradle);
    log("已为 AS工程 增加 qhhz.libandoird 库 ");
}
// 增加平台对应的变量
function attachPlatVariable(options) {
    if (options.platform != "android")
        return;
    const { and_channel: channel } = options.packages['plat-helper'];
    const pjoin = Editor.Utils.Path.join;
    const projPath = pjoin(options.buildPath.replace("project://", Editor.Project.path + "\\"), options.outputName, "proj");
    const propPath = pjoin(projPath, "gradle.properties");
    const propCnt = (0, fs_extra_1.readFileSync)(propPath, "utf-8");
    const checkVariable = [
        "OPPO_APP_ID", "OPPO_APP_KEY", "OPPO_APP_SECRET",
        "MI_APP_ID", "MI_APP_KEY", "MI_APP_NAME",
        "TAPTAP_CLIENT_ID", "CSJ_APP_ID", "CSJ_APP_NAME", "CSJ_BANNER_HEIGHT", "CSJ_BANNER_WIDTH",
        "VIVO_APP_ID", "VIVO_AD_MEDIA_ID",
    ];
    let propLines = propCnt.split("\n");
    for (let index = 0; index < propLines.length; index++) {
        const line = propLines[index];
        const includeCheck = checkVariable.find((vKey) => line.indexOf(vKey) > -1);
        if (includeCheck) {
            propLines.splice(index, 1);
            index--;
        }
    }
    checkVariable.forEach(vKey => {
        const vVal = options.packages['plat-helper'][vKey];
        if (vVal && vVal != "") {
            propLines.push(`${vKey}="${vVal}"`);
            log("+ " + `${vKey}="${vVal}"`);
        }
    });
    (0, fs_extra_1.writeFileSync)(propPath, propLines.join("\n"));
}
function tmpHub() {
    // const orientationOption = options.packages.android.orientation
    // screenOrientation (attr) enum 
    // [behind=3, fullSensor=10, fullUser=13, 
    // landscape=0, locked=14, nosensor=5, portrait=1, 
    // reverseLandscape=8, reversePortrait=9, sensor=4, 
    // sensorLandscape=6, sensorPortrait=7, unspecified=4294967295, 
    // user=2, userLandscape=11, userPortrait=12].
    // const orientationVariable = "PROP_ORIENTATION=".concat(
    //     (orientationOption.landscapeLeft && orientationOption.landscapeRight) ? "6" :
    //         (orientationOption.landscapeLeft ? "8" : (orientationOption.landscapeRight ? "0" : "1")));
    // "PROP_ORIENTATION", 
    // propLines.push(orientationVariable);
    // log("+" + orientationVariable);
}
function reviseGradleProp(options, result) {
    if (options.platform != "android")
        return;
    const { and_channel: and_channel, PROTOCOL_URL, PRIVATE_URL, and_enableAd: enableAd } = options.packages['plat-helper'];
    const pjoin = Editor.Utils.Path.join;
    const projPath = pjoin(options.buildPath.replace("project://", Editor.Project.path + "\\"), options.outputName, "proj");
    const propPath = pjoin(projPath, "gradle.properties");
    const propCnt = (0, fs_extra_1.readFileSync)(propPath, "utf-8");
    const useAndroidX = "android.useAndroidX=true";
    const enableJetifier = "android.enableJetifier=true";
    const protocolUrl = `PROTOCOL_URL="${PROTOCOL_URL}"`;
    const privateUrl = `PRIVATE_URL="${PRIVATE_URL}"`;
    const designWidth = `DESIGN_HEIGHT=${result.settings.screen.designResolution.height}`;
    const designHeight = `DESIGN_WIDTH=${result.settings.screen.designResolution.width}`;
    const channel = `CHANNEL=${and_channel}`;
    const enableAdProp = `ENABLE_AD=${enableAd ? 1 : 0}`;
    const compileJavaCommend = `#业务编码字符集, 注意这是指定源码解码的字符集[编译器]`;
    const compileJava = `compileJava.options.encoding="UTF-8"`;
    const compileTestJavaCommend = `#测试编码字符集, 注意这是指定源码解码的字符集[编译器]`;
    const compileTestJava = `compileTestJava.options.encoding="UTF-8"`;
    const lineRemoveDetects = [
        "android.useAndroidX", "android.enableJetifier",
        "PROTOCOL_URL", "PRIVATE_URL",
        "ENABLE_AD",
        "DESIGN_HEIGHT", "DESIGN_WIDTH", "CHANNEL",
        "#业务编码字符集", "compileJava.options.encoding",
        "#测试编码字符集", "compileTestJava.options.encoding",
    ];
    let propLines = propCnt.split("\n");
    for (let index = 0; index < propLines.length; index++) {
        const line = propLines[index];
        // 统一
        if (lineRemoveDetects.find(v => line.indexOf(v) > -1)) {
            propLines.splice(index, 1);
            index--;
        }
    }
    // if ([GameEnv.Mi].includes(env)) {
    //     const lineIndex = propLines.findIndex(v => v.indexOf("PROP_MIN_SDK_VERSION") > -1);
    //     if (lineIndex > -1) {
    //         propLines.splice(lineIndex, 1, `PROP_MIN_SDK_VERSION=23`);
    //         log("已将 AS工程 min_sdk_version 设置为 23 ");
    //     }
    // }
    if ([builder_1.Channel.Oppo, builder_1.Channel.Mi].includes(and_channel)) {
        propLines.push(useAndroidX);
        log("已为 AS工程 开启 useAndroidX ");
    }
    if ([builder_1.Channel.Oppo, builder_1.Channel.Mi].includes(and_channel)) {
        propLines.push(enableJetifier);
        log("已为 AS工程 开启 enableJetifier");
    }
    if ([builder_1.Channel.Mi, builder_1.Channel.Taptap].includes(and_channel)) {
        propLines.push(compileJavaCommend);
        propLines.push(compileJava);
        propLines.push(compileTestJavaCommend);
        propLines.push(compileTestJava);
        log("已为 AS工程 设置编码字符集");
    }
    propLines.push(enableAdProp);
    log("+" + enableAdProp);
    propLines.push(protocolUrl);
    log("+" + protocolUrl);
    propLines.push(privateUrl);
    log("+" + privateUrl);
    propLines.push(designHeight);
    log("+" + designHeight);
    propLines.push(designWidth);
    log("+" + designWidth);
    propLines.push(channel);
    log("+" + channel);
    (0, fs_extra_1.writeFileSync)(propPath, propLines.join("\n"));
}
function reviseGradleVersion(options) {
    const pjoin = Editor.Utils.Path.join;
    const projPath = pjoin(options.buildPath.replace("project://", Editor.Project.path + "\\"), options.outputName, "proj");
    const buildGradlePath = pjoin(projPath, "build.gradle");
    const buildGradleSourcePath = joinPack("static/asset/build.gradle.txt");
    const gradleWrapperPath = pjoin(projPath, "gradle/wrapper/gradle-wrapper.properties");
    const gradleWrapperCnt = (0, fs_extra_1.readFileSync)(gradleWrapperPath, "utf-8");
    let lines = gradleWrapperCnt.split("\n");
    for (let index = 0; index < lines.length; index++) {
        const line = lines[index];
        if (line.indexOf("distributionUrl") > -1) {
            lines.splice(index, 1);
            index--;
        }
    }
    lines.push(`distributionUrl = https\://services.gradle.org/distributions/gradle-7.6.1-all.zip`);
    (0, fs_extra_1.writeFileSync)(gradleWrapperPath, lines.join("\n"));
    (0, fs_extra_1.writeFileSync)(buildGradlePath, (0, fs_extra_1.readFileSync)(buildGradleSourcePath, "utf-8"), "utf-8");
    log("已为 AS工程 设置 gradle 版本");
}
function attachChannel2Mainjs(options, result) {
    const { and_channel: channel } = options.packages['plat-helper'];
    const pjoin = Editor.Utils.Path.join;
    const inject_script = `window.channel = "${channel}";\n
    `;
    var url = pjoin(result.dest, 'data', 'main.js');
    if (!fs_extra_2.default.existsSync(url)) {
        url = pjoin(result.dest, 'assets', 'main.js');
    }
    const data = fs_extra_2.default.readFileSync(url, "utf-8");
    const newStr = inject_script + data;
    fs_extra_2.default.writeFileSync(url, newStr, { encoding: "utf-8" });
    console.warn("(window.channel) updated in built main.js for hot update");
}
