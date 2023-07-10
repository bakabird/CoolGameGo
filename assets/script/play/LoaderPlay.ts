import { PlayBase } from "coolgame-cc";
import { Action } from "coolgame-cc/Define";
import Go from "../Go";
import { LoaderDlg } from "../dlgs/LoaderDlg";
import CCCHotfix from "../CCCHotfix";
import { game } from "cc";
import { ModalDlg } from "../dlgs/ModalDlg";

export class LoaderPlay extends PlayBase {
    playName: string = "LoaderPlay";
    protected OnInit(complete: Action): void {
        complete()
    }
    protected OnLateInit(complete: Action): void {
        complete()
    }
    protected OnDispose(): void {
    }

    public fetchLoaderDlg() {
        return new Promise<void>((rso, rje) => {
            try {
                Go.fuiSys.loadPackage("UI/Loader", undefined, () => {
                    Go.dlgKit.fetchDlg(LoaderDlg);
                    rso();
                });
            } catch (err) {
                rje(err);
            }
        })
    }

    public dealHotfix(env: "Debug" | "Release") {
        CCCHotfix.me.init(false, {
            localManifestPath: env == "Debug" ? "debug/project" : "project",
        });
        // 接收最新的热更进度提示信息。
        CCCHotfix.me.e.on(CCCHotfix.Event.Tip, (tip) => {
            console.log(tip);
            // // 在对应的 UI 中显示提示
            LoaderDlg.me.onTip(tip)
        })
        return new Promise<void>((rso) => {
            // 检查更新
            CCCHotfix.me.checkUpdate().then(({ needUpdate, error }) => {
                // 响应检查更新回调
                console.log("checkUpdateRet " + needUpdate + " " + error)
                if (needUpdate) {
                    // 需要更新
                    console.log("Start to update");
                    // 开始更新
                    CCCHotfix.me.doUpdate((state, progress) => {
                        // 响应更新回调
                        if (state == "progress") {
                            // 进度更新
                            // progress 范围为 0 ~ 100
                            LoaderDlg.me.onProgress(progress);
                            console.log('版本更新进度: ' + progress);
                            return;
                        }
                        if (state == "fail") {
                            console.log('版本更新失败...')
                            // 补充版本更新失败逻辑...
                            this._modelExit(`版本更新失败，尝试重启游戏或\n截图联系客服。`)
                        } else if (state == "success") {
                            console.log("版本更新成功")
                            LoaderDlg.me.onProgress(100);
                            LoaderDlg.me.onTip(`版本更新成功！即将软重启。`)
                            setTimeout(() => {
                                game.restart();
                            }, 666)
                        }
                    })
                } else if (error) {
                    // 检查更新时碰到错误
                    console.error('版本更新检查失败', error)
                    // 补充版本检查更新失败逻辑...
                    this._modelExit(`版本检测失败，尝试重启游戏或\n截图联系客服。${error}`)
                } else {
                    LoaderDlg.me.onProgress(100);
                    LoaderDlg.me.onTip(`已是最新版本！`)
                    console.log("已是最新版本！")
                    // 补充游戏后续逻辑
                    rso();
                }
            })
        })
    }

    public loadMain() {
        return new Promise<void>((rso, rje) => {
            try {
                Go.fuiSys.loadPackage("UI/Main", undefined, () => {
                    rso();
                });
            } catch (err) {
                rje(err);
            }
        })
    }

    private _modelExit(tip: string) {
        ModalDlg.pop({
            tip,
            onYes: () => {
                game.end();
            },
            bgClose: false,
            yesText: "退出游戏",
        })
    }
}