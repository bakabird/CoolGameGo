import { UITransform, Vec2, Widget } from "cc";
import { SysBase } from "coolgame-cc";
import * as fgui from "fairygui-ccc370";
import FgoPool from "./FgoPool";
import TimeSys from "../timeSys/TimeSys";
import { Call } from "../../CommonInterface";

export class FUISys extends SysBase {
    public sysName: string = "FUISys";

    private _getPlat: () => string;
    private _getChannel: () => string;
    private _fgoPool: fgui.GObjectPool;
    private _timeSys: TimeSys;

    public get timeSys(): TimeSys {
        return this._timeSys;
    }

    public get fgoPool(): fgui.GObjectPool {
        return this._fgoPool;
    }

    public get plat(): string {
        return this._getPlat()
    }

    public get channel(): string {
        return this._getChannel()
    }

    public get root() {
        return fgui.GRoot.inst;
    }

    protected OnInit(complete: Call): void {
        fgui.GRoot.create();
        this._fgoPool = new fgui.GObjectPool();
        complete()
    }

    protected OnLateInit(complete: Call): void {
        complete();
    }

    protected OnDispose(): void {
    }

    public init(timeSys: TimeSys, getPlat?: () => string, getChannel?: () => string) {
        this._timeSys = timeSys;
        this._getPlat = getPlat ?? (() => "default");
        this._getChannel = getChannel ?? (() => "default");
    }

    public loadPackage(packPath: string, onProgress?: (finish: number, total: number) => void, onLoaded?: (error: any) => void) {
        fgui.UIPackage.loadPackage(packPath, onProgress, onLoaded);
    }

    public createObject(pak: string, name: string) {
        return fgui.UIPackage.createObject(pak, name);
    }

    public toUrl(pak: string, name: string) {
        return fgui.UIPackage.getItemURL(pak, name);
    }

    public toUrlByArr(pair: string[]) {
        return fgui.UIPackage.getItemURL(pair[0], pair[1]);
    }

    public allocPool(url: string) {
        return FgoPool.alloc(url);
    }

    public freePool(pool: FgoPool) {
        FgoPool.free(pool);
    }

    public centerOfGlobal(fgo: fgui.GObject): Vec2 {
        const pivotX = fgo.pivotAsAnchor ? 0.5 - fgo.pivotX : 0.5;
        const pivotY = fgo.pivotAsAnchor ? 0.5 - fgo.pivotY : 0.5;
        const localCenter = new Vec2(fgo.width * pivotX, fgo.height * pivotY);
        return fgo.localToGlobal(localCenter.x, localCenter.y);
    }
}