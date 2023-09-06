import { KitBase } from "coolgame-cc";
import { GComponent } from "fairygui-ccc370";
import { DlgBase } from "./DlgBase";
import { Tween, Vec2, find, tween } from "cc";
import { FUISys } from "./FUISys";
import { Shake2DModule, arrayRemoveAll, className, getEnumName, isNull } from "gnfun";
import TimeSys from "../timeSys/TimeSys";
import { Call, ctor } from "../../CommonInterface";

export enum DlgLayer {
    Background,
    Base,
    Front,
    Topest,
    GM,
    ItemFly,
    TopMask,
}

export class DlgKit extends KitBase {
    public kitName: string = "DlgKit";

    private _fuiSys: FUISys;
    private _layers: Map<DlgLayer, GComponent>;
    private _dlgList: Array<DlgBase>;
    private _timeSys: TimeSys;
    private _shakeTween: Tween<Shake2DModule>;

    public get timeSys() {
        return this._timeSys;
    }

    public get fuiSys(): FUISys {
        return this._fuiSys;
    }

    public get plat() {
        return this._fuiSys.plat
    }

    public get channle() {
        return this._fuiSys.channel
    }

    protected OnInit(complete: Call): void {
        this._fuiSys = this.sys(FUISys);
        this._timeSys = this.sys(TimeSys);
        this._layers = new Map<DlgLayer, GComponent>();
        this._dlgList = [];
        this._getLayer(DlgLayer.Base); // Base层先实例化出来
        complete()
    }

    protected OnLateInit(complete: Call): void {
        complete()
    }

    protected OnDispose(): void {

    }

    public _getLayer(layer: DlgLayer): GComponent {
        if (this._layers.has(layer)) {
            return this._layers.get(layer);
        } else {
            const layerCom = new GComponent();
            layerCom.node.name = getEnumName(DlgLayer, layer)
            let nearestFrontLayerCom: GComponent;
            // 找到存在且在自己之前的然后插入到后面
            for (let tmpLayer = layer + 1; tmpLayer <= DlgLayer.TopMask; tmpLayer++) {
                if (this._layers.has(tmpLayer)) {
                    nearestFrontLayerCom = this._layers.get(tmpLayer);
                    break;
                }
            }
            if (nearestFrontLayerCom) {
                this._fuiSys.root.addChildAt(layerCom, this._fuiSys.root.getChildIndex(nearestFrontLayerCom));
            } else {
                this._fuiSys.root.addChild(layerCom);
            }
            this._layers.set(layer, layerCom);
            return layerCom;
        }
    }

    public fetchDlg<T extends DlgBase>(dlgCtrlType: ctor<T>): T {
        for (let index = 0; index < this._dlgList.length; index++) {
            const dlg = this._dlgList[index];
            if (!dlg.isClosed && dlg instanceof dlgCtrlType) {
                return dlg;
            }
        }
        var dlgCtrl = new dlgCtrlType();
        var fgo = this._fuiSys.createObject(dlgCtrl.dlgPak, dlgCtrl.dlgRes).asCom;
        this._getLayer(dlgCtrl.dlgLayer).addChild(fgo);
        dlgCtrl.init(fgo, this);
        this._dlgList.push(dlgCtrl);
        console.log("... " + className(dlgCtrl) + "<res:" + dlgCtrl.dlgRes + ">")
        return dlgCtrl;
    }

    public getDlg<T extends DlgBase>(dlgCtrlType: ctor<T>): T {
        for (let index = 0; index < this._dlgList.length; index++) {
            const dlg = this._dlgList[index];
            if (!dlg.isClosed && dlg instanceof dlgCtrlType) {
                return dlg;
            }
        }
    }

    public closeDlg<T extends DlgBase>(dlgCtrlType: ctor<T>) {
        arrayRemoveAll(this._dlgList, dlg => dlg.isClosed)
        for (let index = 0; index < this._dlgList.length; index++) {
            const dlg = this._dlgList[index];
            if (dlg instanceof dlgCtrlType) {
                dlg.close();
            }
        }
    }

    public shake() {
        const node = find("Canvas/Camera")
        const ori = new Vec2(node.position.x, node.position.y)
        const shake2d = new Shake2DModule(3, 1, 2, "smoothHalfCircle", 8, () => ori, (v2) => {
            find("Canvas/Camera").setPosition(v2.x, v2.y)
        });
        this._shakeTween?.stop();
        this._shakeTween = tween(shake2d).to(0.15, { ratio: 1 }).start();
    }

}