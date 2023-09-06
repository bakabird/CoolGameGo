import { GProgressBar, GTextField } from "fairygui-ccc370";
import CCCHotfix from "../CCCHotfix";
import { easyEncode } from "gnfun"
import Plat from "../plat/Plat";
import { DlgBase } from "../sys/fuiSys/DlgBase";

export class LoaderDlg extends DlgBase {
    public static me: LoaderDlg;
    public get dlgRes(): string {
        return "LoaderDlg"
    }
    public get dlgPak(): string {
        return "Loader"
    }
    private _tip: GTextField;
    private _progress: GProgressBar;
    protected OnInit(): void {
        LoaderDlg.me = this;
        this._progress = this.getCom("progress").as<GProgressBar>()
        this._tip = this.getTxt("tip")
        this._progress.value = 0;
        this.addEvt(CCCHotfix.me.e, CCCHotfix.Event.ResVersionUpdate, this._syncVersionTxt)
        this._syncVersionTxt()
    }
    private _syncVersionTxt() {
        this.getTxt("version").text = `${easyEncode(Plat.inst.channel)}.${CCCHotfix.me.localManifestPath.length} ${CCCHotfix.me.resVersion}`
    }
    public onProgress(v: number) {
        this._progress.tweenValue(v, .3);
    }
    public onTip(tip: string) {
        this._tip.text = tip;
    }
    protected OnBgClickClose(): boolean {
        return false;
    }
}
