import { easyEncode } from "gnfun";
import CCCHotfix from "../CCCHotfix";
import Plat from "../plat/Plat";
import { DlgBase } from "../sys/fuiSys/DlgBase";

export class MainDlg extends DlgBase {
    get dlgRes(): string {
        return "MainDlg"
    }
    protected OnInit(): void {
        this._syncVersionTxt()
    }
    private _syncVersionTxt() {
        this.getTxt("version").text = `${easyEncode(Plat.inst.channel)}.${CCCHotfix.me.localManifestPath.length} ${CCCHotfix.me.resVersion}`
    }
    protected OnBgClickClose(): boolean {
        return false;
    }
}