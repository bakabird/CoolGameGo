import { EventKeyboard, EventTarget, EventTouch, Input, input, KeyCode } from "cc";
import { SysBase } from "coolgame-cc"
import { Call } from "../../CommonInterface";
import { _CoolgameCCInteral } from "coolgame-cc/Define";
import { FUISys } from "../fuiSys/FUISys";
import { Event } from "fairygui-ccc370";

export default class InputSys extends SysBase {
    public static Event = {
        TouchEnd: 'TouchEnd',
    }

    private _keyClickMap: Map<KeyCode, boolean>;
    private _fuiSys: FUISys;
    private _e: EventTarget;

    public sysName: string = "InputSys";

    public get event(): EventTarget {
        return this._e;
    }

    protected OnInit(complete: Call): void {
        this._e = new EventTarget();
        complete()
    }

    protected OnLateInit(complete: () => void, sys: <T extends SysBase>(type: _CoolgameCCInteral.types_constructor<T>) => T): void {
        this._fuiSys = sys(FUISys);
        if (this._fuiSys) {
            this._fuiSys.root.on(Event.TOUCH_END, this._onTouchEnd, this)
        } else {
            input.on(Input.EventType.TOUCH_END, this._onTouchEnd, this);
        }
        complete();
    }

    protected OnDispose(): void {
        this._fuiSys?.root.off(Event.TOUCH_END, this._onTouchEnd, this)
        input.off(Input.EventType.KEY_DOWN, this._onKeyDown, this)
        input.off(Input.EventType.KEY_UP, this._onKeyUp, this)
        input.off(Input.EventType.TOUCH_END, this._onTouchEnd, this);
    }

    //#region  Touch

    private _onTouchEnd(e: any) {
        this._e.emit(InputSys.Event.TouchEnd, e);
    }

    //#endregion

    private _onKeyDown(e: EventKeyboard) {
        if (!this._keyClickMap) return
        if (this._keyClickMap.has(e.keyCode)) {
            // console.log(e.keyCode, "down")
            this._keyClickMap.set(e.keyCode, true);
        }
    }

    private _onKeyUp(e: EventKeyboard) {
        if (!this._keyClickMap) return;
        if (this._keyClickMap.has(e.keyCode)) {
            // console.log(e.keyCode, "up");
            this._keyClickMap.set(e.keyCode, false);
        }
    }

    public listenKey(keycode: KeyCode) {
        if (!this._keyClickMap) {
            this._keyClickMap = new Map<KeyCode, boolean>
            input.on(Input.EventType.KEY_DOWN, this._onKeyDown, this)
            input.on(Input.EventType.KEY_UP, this._onKeyUp, this)
        }
        this._keyClickMap.set(keycode, false);
    }

    public getKeyDown(keycode: KeyCode) {
        if (!this._keyClickMap.has(keycode)) {
            console.warn("did not listen keycode " + keycode);
            return;
        }
        // console.log(keycode, this._keyClickMap.get(keycode));
        return this._keyClickMap.get(keycode);
    }

}