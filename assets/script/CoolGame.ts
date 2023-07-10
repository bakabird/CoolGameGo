import { _decorator } from 'cc';
import { KitBase, PlayBase, SKPGame, SysBase } from 'coolgame-cc';
import { types_constructor } from 'coolgame-cc/Define';
import Go from './Go';
import TimeSys from 'coolgame-cc-sys-time';
import { DlgKit, FUISys } from 'coolgame-cc-sys-fui';
import { LoaderDlg } from './dlgs/LoaderDlg';
const { ccclass, property } = _decorator;

@ccclass('CoolGame')
export class CoolGame extends SKPGame {
    protected gamename: String = "CoolGame";
    protected OnAddSys(addSys: <T extends SysBase>(type: types_constructor<T>) => T): void {
        addSys(TimeSys);
        addSys(FUISys)
    }
    protected OnAddKit(addKit: <T extends KitBase>(type: types_constructor<T>) => T): void {
        addKit(DlgKit);
    }
    protected OnAddPlay(addPlay: <T extends PlayBase>(type: types_constructor<T>) => T): void {
    }
    protected OnEnter(): void {
        this._initSKP();
        Go.fuiSys.loadPackage("UI/Loader", undefined, () => {
            Go.dlgKit.fetchDlg(LoaderDlg);
        })
    }
    private _initSKP() {
        const fuiSys = this.sys(FUISys);
        const timeSys = this.sys(TimeSys);
        const dlgKit = this.kit(DlgKit)
        fuiSys.init(timeSys);
        Go.game = this;
        Go.fuiSys = fuiSys;
        Go.tiemSys = timeSys;
        Go.dlgKit = dlgKit;
    }
}

