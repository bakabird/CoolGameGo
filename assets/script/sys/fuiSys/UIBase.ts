import { EventTarget, Node, Tween, tween } from "cc";
import { Controller, GButton, GComponent, GGroup, GLabel, GList, GLoader, GLoader3D, GObject, GTextField, Transition } from "fairygui-ccc370";
import UIWrap from "./UIWrap";
import { DlgKit } from "./DlgKit";
import { arrayRemoveAll, isNull } from "gnfun";
import { GnfunCC } from "../../GnfunCC/XTween";

export default abstract class UIBase {
    private _isClosed: boolean = false;
    private _fgo: GObject = null;
    private _node: Node = null;
    private _wrapList: Array<UIWrap> = null;
    private _everOnClickObj: Set<GObject> = null;
    private _everEvtListens: Array<[EventTarget, string, (...arg: any) => void, any?]> = null;
    private _everFgoListens: Array<[GObject, string, (...arg: any) => void, any?]> = null;
    private _everTimers: Array<number> = null;
    private _everTweenTarget: Set<any> = null;

    private _dlgkit: DlgKit = null;

    public get dlgkit(): DlgKit {
        return this._dlgkit;
    }

    public get isClosed(): boolean {
        return this._isClosed;
    }

    public get fgc() {
        return this._fgo.asCom;
    }

    public get node() {
        return this._node;
    }

    public init(fgo: GObject, dlgkit: DlgKit) {
        if (this._fgo != null) return;

        this._fgo = fgo;
        this._node = fgo.node;
        this._dlgkit = dlgkit;
    }

    public show() {
        if (this.isClosed) return;
        this._fgo.visible = true;
    }

    public hide() {
        if (this.isClosed) return;
        this._fgo.visible = false;
    }

    /**
     * 提到最上层
     */
    public moveToFront(): void {
        this._fgo.parent.setChildIndex(this._fgo, this._fgo.parent.numChildren - 1);
    }

    public close() {
        if (this.isClosed) return;
        this._everEvtListens?.forEach(([e, event, listener, thisArg]) => {
            e?.off(event, listener, thisArg);
        })
        this._everEvtListens = null;
        this._everFgoListens?.forEach(([fgo, event, listener, thisArg]) => {
            fgo && !fgo.isDisposed && fgo.off(event, listener, thisArg);
        })
        this._everFgoListens = null;
        this._everOnClickObj?.forEach(obj => {
            if (obj && !obj.isDisposed) {
                obj.clearClick();
            }
        })
        this._everOnClickObj = null;
        this._wrapList?.forEach((w) => {
            if (!w.isClosed) {
                w.close();
            }
        });
        this._wrapList = null;
        this._everTimers?.forEach(tid => {
            this._dlgkit.timeSys.delete(tid);
        })
        this._everTimers = null;
        this._everTweenTarget?.forEach(tar => {
            // console.log("stop tar ", tar, tar.uuid);
            Tween.stopAllByTarget(tar);
        })
        this._everTweenTarget = null;
        this.OnDisposeSelfFgo(this._fgo);
        this._fgo = null;
        this._node = null;
        this._isClosed = true;
    }

    protected wrap<T extends UIWrap>(type: { new(): T }, fgo: GObject | string): T {
        if (typeof fgo == "string") {
            fgo = this.getChild(fgo);
        }
        if (!this._wrapList) {
            this._wrapList = [];
        }
        var wrap = new type();
        wrap.init(fgo, this._dlgkit);
        this._wrapList.push(wrap);
        return wrap;
    }

    protected unwrap<T extends UIWrap>(wrap: T) {
        if (!this._wrapList) return;
        const idx = this._wrapList.indexOf(wrap);
        if (idx > -1) {
            this._wrapList.splice(idx, 1);
            wrap.close();
            console.log("unwrap suc " + wrap);
        } else {
            console.error("this wrap not in list");
        }
    }

    protected getWrap<T extends UIWrap>(type: { new(): T }): T {
        if (!this._wrapList) return null;
        arrayRemoveAll(this._wrapList, wrap => wrap.isClosed)
        for (let index = 0; index < this._wrapList.length; index++) {
            const wrap = this._wrapList[index];
            if (wrap instanceof type) return wrap;
        }
        return null;
    }

    protected getWraps<T extends UIWrap>(type: { new(): T }): T[] {
        if (!this._wrapList) return [];
        arrayRemoveAll(this._wrapList, wrap => wrap.isClosed)
        return this._wrapList.filter(wrap => wrap instanceof type) as T[];
    }

    protected getChild(childName: string): GObject {
        if (childName == "") return this.fgc
        else return this.fgc.getChild(childName);
    }

    protected getChildInGroup(childName: string, groupName: string): GObject {
        return this.fgc.getChildInGroup(childName, this.getGroup(groupName));
    }

    protected ajustPlat(childName: string, ctrlName: string = "plat") {
        const plat = this._dlgkit.plat
        if (isNull(plat)) return;
        const c = this.getChild(childName)
        if (c) {
            const ctrl = c.asCom.getController(ctrlName)
            if (ctrl) {
                let index = 0;
                for (let i = 1; i < ctrl.pageCount; i++) {
                    const name = ctrl.getPageName(i)
                    if (name.includes(plat)) {
                        index = i
                        break
                    }
                }
                ctrl.setSelectedIndex(index)
            }
        }
    }

    protected ajustChannel(childName: string, ctrlName: string = "channel") {
        const channel = this._dlgkit.channle;
        if (isNull(channel)) return;
        const c = this.getChild(childName)
        if (c) {
            const ctrl = c.asCom.getController(ctrlName)
            if (ctrl) {
                let index = 0;
                for (let i = 1; i < ctrl.pageCount; i++) {
                    const name = ctrl.getPageName(i)
                    if (name.includes(channel)) {
                        index = i
                        break
                    }
                }
                ctrl.setSelectedIndex(index)
            }
        }
    }

    //#region getter for child

    protected getCom(childName: string): GComponent {
        return this.getChild(childName).asCom;
    }

    protected getBtn(childName: string): GButton {
        return this.getChild(childName).asBtn;
    }

    protected getGraph(childName: string) {
        return this.getChild(childName).asGraph;
    }

    protected getLoader(childName: string): GLoader {
        return this.getChild(childName).asLoader;
    }

    protected getLoader3D(childName: string): GLoader3D {
        return this.getChild(childName).asLoader3D;
    }

    protected getTxt(childName: string): GTextField {
        return this.getChild(childName).asTextField;
    }

    protected getLabel(childName: string): GLabel {
        return this.getChild(childName).asLabel
    }

    protected getList(childName: string): GList {
        return this.getChild(childName).asList
    }

    protected getGroup(childName: string): GGroup {
        return this.getChild(childName).asGroup;
    }

    protected getTransition(name: string): Transition {
        return this.fgc.getTransition(name)
    }

    protected getController(name: string): Controller {
        return this.fgc.getController(name);
    }

    //#endregion

    //#region addXXX

    /**
     * 添加自释放的按钮绑定
     * @param objName 节点名称。为空表示当前节点本身 | 节点
     * @param listener 
     */
    protected addBtnEvt(objName: string | GObject, listener: (enableBtn: Function) => void, thisArg?: any) {
        if (this._everOnClickObj == null) {
            this._everOnClickObj = new Set<GObject>();
        }
        thisArg ??= this
        if (typeof objName == "string") {
            objName = this.getChild(objName);
        }
        const btn = objName;
        btn.onClick(() => {
            const oriG = btn.grayed
            btn.enabled = false;
            btn.grayed = false;
            listener.call(this, () => {
                btn.enabled = true;
                btn.grayed = oriG
            })
        }, this);
        this._everOnClickObj.add(btn);
    }

    protected addTween<T>(target: T): Tween<T> {
        if (this._everTweenTarget == null) {
            this._everTweenTarget = new Set();
        }
        // console.log("add tween ", target.uuid)
        this._everTweenTarget.add(target);
        return tween(target)
    }

    protected addXTween<T>(target: T): GnfunCC.XTween<T> {
        if (this._everTweenTarget == null) {
            this._everTweenTarget = new Set();
        }
        this._everTweenTarget.add(target);
        return new GnfunCC.XTween(target)
    }

    protected addEvt(e: EventTarget, event: string, listener: (...arg: any) => void, thisArg?: any) {
        if (!e) return;
        if (this._everEvtListens == null) {
            this._everEvtListens = [];
        }
        thisArg ??= this;
        e.on(event, listener, thisArg);
        this._everEvtListens.push([e, event, listener, thisArg])
    }

    protected addFgoEvt(fgo: GObject, event: string, listener: (...arg: any) => void, thisArg?: any) {
        if (!fgo) return;
        if (this._everFgoListens == null) {
            this._everFgoListens = [];
        }
        thisArg ??= this;
        fgo.on(event, listener, thisArg);
        this._everFgoListens.push([fgo, event, listener, thisArg]);
    }

    protected addDelay(delay: number, func: Function, caller?: any, ...arg: Array<any>) {
        caller ??= this;
        return this._addTimer(delay, 1, func, caller, ...arg);
    }

    protected addNextFrame(func: Function, caller?: any, ...arg: Array<any>) {
        caller ??= this;
        return this._addTimer(0, 1, func, caller, ...arg);
    }

    protected addInterval(interval: number, loop: number, func: Function, caller?: any, ...arg: Array<any>) {
        caller ??= this;
        return this._addTimer(interval, loop, func, caller, ...arg);
    }

    protected delTimer(timerId: number): null {
        this._dlgkit.timeSys.delete(timerId);
        return null;
    }

    private _addTimer(interval: number, loops: number, func: Function, caller?: any, ...arg: Array<any>) {
        if (this._isClosed) {
            console.warn("ui is closed cant add timer");
            return;
        }
        const t = this._dlgkit.timeSys.timer(interval, loops, func, caller, ...arg);;
        if (!this._everTimers) {
            this._everTimers = [];
        }
        this._everTimers.push(t);
        return t;
    }

    //#endregion

    /**
     * <can-override>
     * 在销毁 Fgo 时被调用
     */
    protected OnDisposeSelfFgo(selfFgo: GObject) {
        selfFgo.dispose();
    }
}