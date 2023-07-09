import { Asset, EventTarget, native, resources } from "cc";
import { NATIVE } from "cc/env";
import * as SparkMD5 from "spark-md5";

enum HotfixCheckError {
    Non = "",
    NoLocalMainifest = "NoLocalMainifest",
    FailToDownloadMainfest = "FailToDownloadMainfest",
}

type HotfixUpdateState = "progress" | "fail" | "success"

export default class CCCHotfix {
    public static Event = {
        ResVersionUpdate: "ResVersionUpdate",
        Tip: "Tip",
    }

    private static _me: CCCHotfix;
    public static get me(): CCCHotfix {
        if (!this._me) {
            this._me = new CCCHotfix()
        }
        return this._me;
    }

    private _working: boolean
    private _checkUpdateRetryMaxTime: number;
    private _checkUpdateRetryInterval: number;
    private _maxUpdateFailRetryTime: number;
    private _localManifestPath: string;

    private _resVersion: string;
    private _checkUpdateRetryTime: number;
    private _updateFailRetryTime: number;
    private _storagePath: string
    private _e: EventTarget;
    private _am: native.AssetsManager;
    private _checkListener: (need: boolean, error: HotfixCheckError) => void;
    private _updateListener: (state: HotfixUpdateState, progress?: number) => void;

    public get e(): EventTarget {
        return this._e;
    }

    /**
     * 当前热更新资源版本号。
     * - 对应有事件 Event.ResVersionUpdate
     */
    public get resVersion(): string {
        return this._resVersion;
    }

    public get localManifestPath(): string {
        return this._localManifestPath;
    }

    public get working() {
        return this._working && NATIVE && (native ?? false)
    }

    private constructor() {
        this._working = false;
        this._resVersion = ''
        this._localManifestPath = ''
        this._checkUpdateRetryTime = 0;
        this._updateFailRetryTime = 0;

        this._checkListener = null;
        this._updateListener = null;

        this._e = new EventTarget()
    }

    /**
     * 进行初始化
     * @param {boolean} work 改系统是否要工作
     * @param {object} param [可选]参数
     * @param {string} param.storagePath [可选]热更资源存储路径。默认为 "hotfix-assets"。
     * @param {string} param.localManifestPath [可选]本地 mainfest 在 resource 中的路径，不包括拓展名。默认为 "project"（可对应 project.mainfest）。
     * @param {number} param.checkUpdateRetryMaxTime - [可选]检查更新时如果网络错误，最多重试多少次。默认为1。
     * @param {number} param.checkUpdateRetryInterval - [可选]检查更新时如果网络错误，间隔多少秒后重试。默认为3。
     * @param {number} param.maxUpdateFailRetryTime - [可选]热更新时如果部分文件更新失败，将重试多少次。默认为1。
     */
    public init(work: boolean, param?: {
        storagePath?: string,
        localManifestPath?: string,
        checkUpdateRetryMaxTime?: number,
        checkUpdateRetryInterval?: number,
        maxUpdateFailRetryTime?: number,
    }) {
        this._working = work;
        this._localManifestPath = param?.localManifestPath ?? "project";
        this._checkUpdateRetryMaxTime = param?.checkUpdateRetryMaxTime ?? 1;
        this._checkUpdateRetryInterval = param?.checkUpdateRetryInterval ?? 3;
        this._maxUpdateFailRetryTime = param?.maxUpdateFailRetryTime ?? 1;
        this._storagePath = (native.fileUtils ? native.fileUtils.getWritablePath() : '/') + (param?.storagePath ?? 'hotfix-assets');
        console.log("storagePath " + this._storagePath);
    }

    /**
     * 检查是否需要进行更新
     * @return {Promise<{ needUpdate: boolean, error: any }>} 
     *  needUpdate 是否需要进行更新; 
     *  error 检查更新时的错误，如果没有错误则为 null 
     */
    public checkUpdate() {
        return new Promise<{
            needUpdate: boolean,
            error: any,
        }>((rso) => {
            if (!this.working) {
                rso({ needUpdate: false, error: null, })
                return;
            }
            if (!this._am) {
                this._loadLocalManifest().then((manifestUrl) => {
                    this._initAM(manifestUrl);
                    this.checkUpdate().then(ret => rso(ret))
                }).catch(err => {
                    console.log("loadLocalManifest catch err");
                    rso({ needUpdate: false, error: err, })
                })
            } else {
                this._internal_checkUpdate().then((needUpdate) => {
                    rso({ needUpdate: needUpdate, error: null })
                }).catch(err => {
                    rso({ needUpdate: false, error: err })
                })
            }
        })
    }

    /**
     * 实际进行更新
     * @param listener 更新进度回调：state 当前热更状态；progress 当前进度（0-100）
     */
    public doUpdate(listener: (state: HotfixUpdateState, progress?: number) => void) {
        if (this._am) {
            this._updateListener = listener;
            this._am.update();
        }
    }

    // Setup your own version compare handler, versionA and B is versions in string
    // if the return value greater than 0, versionA is greater than B,
    // if the return value equals 0, versionA equals to B,
    // if the return value smaller than 0, versionA is smaller than B.
    private _versionCompareHandle(versionA: string, versionB: string) {
        this._resVersion = versionA
        this._e.emit(CCCHotfix.Event.ResVersionUpdate, versionA)
        console.log("JS Custom Version Compare: version A is " + versionA + ', version B is ' + versionB);
        var vA = versionA.split('.');
        var vB = versionB.split('.');
        for (var i = 0; i < vA.length; ++i) {
            var a = parseInt(vA[i]);
            var b = parseInt(vB[i] || '0');
            if (a === b) {
                continue;
            }
            else {
                return a - b;
            }
        }
        if (vB.length > vA.length) {
            return -1;
        }
        else {
            return 0;
        }
    }

    // Setup the verification callback, but we don't have md5 check function yet, so only print some message
    // Return true if the verification passed, otherwise return false
    private _verifyCallback(path: string, asset: any) {
        // When asset is compressed, we don't need to check its md5, because zip file have been deleted.
        var compressed = asset.compressed;
        // asset.path is relative path and path is absolute.
        var relativePath = asset.path;
        // // The size of asset file, but this value could be absent.
        // var size = asset.size;
        if (compressed) {
            this._eTip("Verification passed : " + relativePath)
            // panel.info.string = "Verification passed : " + relativePath;
            return true;
        } else {
            // Retrieve the correct md5 value.
            var expectedMD5 = asset.md5;
            var filemd5 = SparkMD5['default'].ArrayBuffer.hash(native.fileUtils.getDataFromFile(path));
            if (filemd5 == expectedMD5) {
                this._eTip("Verification passed : " + relativePath + ' (' + expectedMD5 + ')')
                return true
            } else {
                this._eTip("Verification fail : " + relativePath + ' (' + expectedMD5 + ' vs ' + filemd5 + ')')
                return false;
            }
        }
    }

    private _retry() {
        this._eTip('Retry failed Assets...')
        this._am.downloadFailedAssets();
    }

    private _loadLocalManifest() {
        return new Promise<string>((rso, rje) => {
            this._eTip("load manifest from resource " + this._localManifestPath)
            console.log("load manifest from resource " + this._localManifestPath)
            // 读取本地的 localmanifest
            resources.load(this._localManifestPath, Asset, (err, asset) => {
                if (err) {
                    this._eTip("fail to load manifest")
                    console.error("fail to load manifest")
                    console.error(err);
                    rje(err);
                } else {
                    this._eTip("success to load manifest, its nativeUrl: " + asset.nativeUrl)
                    console.log("success to load manifest, its nativeUrl: " + asset.nativeUrl)
                    rso(asset.nativeUrl);
                }
            })
        })
    }

    private _initAM(manifestUrl: string) {
        console.log('Storage path for remote asset : ' + this._storagePath);
        this._am = new native.AssetsManager(manifestUrl, this._storagePath, this._versionCompareHandle.bind(this));
        this._am.setVerifyCallback(this._verifyCallback.bind(this));
        this._am.setEventCallback(this._eventCb.bind(this));
        this._eTip('Hot update is ready, please check or directly update.')
    }

    private _internal_checkUpdate() {
        return new Promise<boolean>((rso, rje) => {
            if (!this._am.getLocalManifest() || !this._am.getLocalManifest().isLoaded()) {
                this._eTip('Failed to load local manifest ...')
                rje('Failed to load local manifest ...')
                return
            }
            this._checkListener = (need, err) => {
                if (need) {
                    rso(true)
                } else {
                    if (err != HotfixCheckError.Non) {
                        if (err == HotfixCheckError.FailToDownloadMainfest) {
                            if (this._checkUpdateRetryMaxTime > this._checkUpdateRetryTime) {
                                setTimeout(() => {
                                    this._checkUpdateRetryTime++;
                                    console.log("fail to download manifest, retry check update, retryTime: " + this._checkUpdateRetryTime)
                                    this._internal_checkUpdate()
                                        .then((bol) => rso(bol))
                                        .catch((err) => rje(err));
                                }, this._checkUpdateRetryInterval * 1000)
                            } else {
                                rje(err);
                            }
                        } else {
                            rje(err);
                        }
                    } else {
                        rso(false);
                    }
                }
            }
            this._eTip('Cheking Update...')
            console.log("HotFix AssetManager.checkUpdate")
            this._am.checkUpdate();
        })
    }

    private _eventCb(event: any) {
        // console.log("HotFix AssetManager.EventCb " + event.getEventCode());
        if (this._checkListener ?? false) {
            this._checkCb(event)
        } else if (this._updateListener ?? false) {
            this._updateCb(event)
        }
    }

    private _checkCb(event: any) {
        const evtCode = event.getEventCode()
        if (evtCode == native.EventAssetsManager.UPDATE_PROGRESSION) {
            this._eTip('Cheking Update Progress...')
            return;
        }
        const _checkListener = this._checkListener;
        this._checkListener = null;
        console.log('HotFix AssetManager.checkUpdate.Callback Code: ' + event.getEventCode());
        switch (event.getEventCode()) {
            case native.EventAssetsManager.ERROR_NO_LOCAL_MANIFEST:
                this._eTip('No local manifest file found, hot update skipped.')
                _checkListener(false, HotfixCheckError.FailToDownloadMainfest)
                break;
            case native.EventAssetsManager.ERROR_DOWNLOAD_MANIFEST:
            case native.EventAssetsManager.ERROR_PARSE_MANIFEST:
                this._eTip('Fail to download manifest file, hot update skipped.')
                _checkListener(false, HotfixCheckError.FailToDownloadMainfest)
                break;
            case native.EventAssetsManager.ALREADY_UP_TO_DATE:
                this._eTip('Already up to date with the latest remote version.')
                _checkListener(false, HotfixCheckError.Non)
                break;
            case native.EventAssetsManager.UPDATE_FINISHED:
                this._eTip('Update finished, seems not change...')
                _checkListener(false, HotfixCheckError.Non)
                break;
            case native.EventAssetsManager.NEW_VERSION_FOUND:
                this._eTip('New version found, please try to update. (' + Math.ceil(this._am.getTotalBytes() / 1024) + 'kb)')
                _checkListener(true, HotfixCheckError.Non)
                break;
            default:
                return;
        }
    }

    private _updateCb(event: any) {
        var needRestart = false;
        var failed = false;
        var retry = false
        switch (event.getEventCode()) {
            case native.EventAssetsManager.ERROR_NO_LOCAL_MANIFEST:
                this._eTip('No local manifest file found, hot update skipped.')
                failed = true;
                break;
            case native.EventAssetsManager.UPDATE_PROGRESSION:
                var msg = event.getMessage();
                if (msg) {
                    // console.log(event.getPercent())
                    // console.log(event.getPercentByFile())
                    this._eTip('Updated file: ' + msg)
                    this._updateListener("progress", event.getPercentByFile())
                }
                break;
            case native.EventAssetsManager.ERROR_DOWNLOAD_MANIFEST:
            case native.EventAssetsManager.ERROR_PARSE_MANIFEST:
                this._eTip('Fail to download manifest file, hot update skipped.')
                failed = true;
                break;
            case native.EventAssetsManager.ALREADY_UP_TO_DATE:
                this._eTip('Already up to date with the latest remote version.')
                failed = true;
                break;
            case native.EventAssetsManager.UPDATE_FINISHED:
                this._eTip('Update finished. ' + event.getMessage());
                needRestart = true;
                break;
            case native.EventAssetsManager.UPDATE_FAILED:
                this._eTip('Update failed. ' + event.getMessage())
                retry = true
                break;
            case native.EventAssetsManager.ERROR_UPDATING:
                this._eTip('Asset update error: ' + event.getAssetId() + ', ' + event.getMessage());
                break;
            case native.EventAssetsManager.ERROR_DECOMPRESS:
                this._eTip(event.getMessage())
                break;
            default:
                break;
        }

        if (retry) {
            if (this._updateFailRetryTime < this._maxUpdateFailRetryTime) {
                this._updateFailRetryTime++;
                this._retry()
            } else {
                failed = true;
            }
        }

        if (failed) {
            this._am.setEventCallback(null!);
            this._updateListener("fail")
            this._updateListener = null;
        }

        if (needRestart) {
            this._am.setEventCallback(null!);
            // Prepend the manifest's search path
            var searchPaths = native.fileUtils.getSearchPaths();
            var newPaths = this._am.getLocalManifest().getSearchPaths();
            console.log(JSON.stringify(newPaths));
            Array.prototype.unshift.apply(searchPaths, newPaths);
            // This value will be retrieved and appended to the default search path during game startup,
            // please refer to samples/js-tests/main.js for detailed usage.
            // !!! Re-add the search paths in main.js is very important, otherwise, new scripts won't take effect.
            localStorage.setItem('HotUpdateSearchPaths', JSON.stringify(searchPaths));
            native.fileUtils.setSearchPaths(searchPaths);
            this._updateListener("success")
            this._updateListener = null;
        }
    }

    private _eTip(tip: string) {
        this._e.emit(CCCHotfix.Event.Tip, tip)
    }
}
