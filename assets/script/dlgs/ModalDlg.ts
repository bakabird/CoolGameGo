import { Action } from "coolgame-cc/Define";
import Go from "../Go";
import { DlgBase, DlgLayer, UIDocker } from "coolgame-cc-sys-fui";


export class ModalDlg extends DlgBase {
    public static pop(option: {
        tip: string;
        yesText?: string;
        onYes: Action;
        yesClose?: boolean;
        noText?: string;
        onNo?: Action;
        noClose?: boolean;
        bgClose?: boolean;
        type?: "confirm" | "alert";
    }) {
        let { tip, yesText, onYes, yesClose, noText, onNo, noClose, bgClose, type } = option;
        yesClose ??= true;
        noClose ??= true;
        bgClose ??= false;
        type ??= "alert";
        Go.dlgKit.fetchDlg(ModalDlg).setData(tip, bgClose, yesText, onYes, yesClose, noText, onNo, noClose, type);
    }

    public get dlgRes(): string {
        return "ModalDlg";
    }

    public get dlgPak(): string {
        return "Loader"
    }

    public get dlgLayer(): DlgLayer {
        return DlgLayer.Topest;
    }

    protected OnInit(): void {
        this.dock(UIDocker.Dock.Bubble);
    }

    protected OnBgClickClose(): boolean {
        return this._bgClose;
    }

    private _bgClose: boolean;

    public setData(tip: string, bgClose: boolean,
        yesText: string, onYes: Action, yesClose: boolean,
        noText: string, onNo: Action, noClose: boolean, type: "confirm" | "alert") {
        this._bgClose = bgClose;
        this.getTxt("tip").text = tip;
        this.getBtn("yes").text = yesText;
        this.getBtn("yes").onClick(() => {
            onYes?.();
            if (yesClose) {
                this.close();
            }
        });
        this.getBtn("no").text = noText;
        this.getBtn("no").onClick(() => {
            onNo?.();
            if (noClose) {
                this.close();
            }
        });
        this.getController("type").setSelectedPage(type)

        // this.addDelay(1, () => {
        //     this.getBtn("yes").fireClick();
        // })
    }

    protected OnGetBgAlpha(): number {
        return 0.3;
    }
}
