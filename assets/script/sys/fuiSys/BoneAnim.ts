import { sp } from "cc";
import { AlignType, GLoader3D, VertAlignType } from "fairygui-ccc370";
import UIBase from "./UIBase";
import { isNull } from "gnfun";
import TimeSys from "../timeSys/TimeSys";

let gid = 0;

export class BoneAnim {
    private _idleAnim: string;
    private _lod3d: GLoader3D;
    private _owner: UIBase;
    private _timeSys: TimeSys;
    private _ignoreAutoIdle: boolean;
    private _onLastAnimComplete: () => void
    public get lod3d(): GLoader3D {
        return this._lod3d;
    }
    public get skeleton(): sp.Skeleton {
        return this._lod3d.content as sp.Skeleton;
    }
    constructor(lod3d: GLoader3D, owner: UIBase) {
        this._lod3d = lod3d;
        this._owner = owner;
        this._ignoreAutoIdle = false;
        this._timeSys = owner.dlgkit.timeSys;
        this._onLastAnimComplete = null
    }
    public config(option: {
        vAlign?: "b" | "m" | "t";
        align?: "l" | "c" | "r";
        idleAnim: string;
        model: string;
        package?: string,
    }) {
        const vAlign = (!option.vAlign || option.vAlign == "m") ? VertAlignType.Middle :
            (option.vAlign == "b" ? VertAlignType.Bottom : VertAlignType.Top);
        const align = (!option.align || option.align == "c") ? AlignType.Center :
            (option.align == "l" ? AlignType.Left : AlignType.Right);
        this._idleAnim = option.idleAnim;

        if (this._onLastAnimComplete) {
            this._ignoreAutoIdle = true;
            this._onLastAnimComplete()
            this._ignoreAutoIdle = false;
            this._lod3d.animationName = null;
        }
        this._lod3d.verticalAlign = vAlign;
        this._lod3d.align = align;
        this._lod3d.url = this._owner.dlgkit.fuiSys.toUrl(option.model, option.package ?? "Main");
        this.playAnim(option.idleAnim, true);
    }

    public playAnim(anim: string, loop: boolean, onComplete?: () => void, thisArg?: any, option?: {
        timeScale?: number,
        autoIdle?: boolean,
    }) {
        const autoIdle = option?.autoIdle ?? true;
        const skeleton = this.skeleton;
        let timeScale = option?.timeScale ?? 1;
        const id = gid++;
        if (this._onLastAnimComplete) {
            this._ignoreAutoIdle = true;
            this._onLastAnimComplete()
            this._ignoreAutoIdle = false;
            this._lod3d.animationName = null
        }
        this._lod3d.loop = loop;
        this._lod3d.animationName = anim;
        console.log(`[BoneAnim]${id} anim (${anim}) play...`);
        this._timeSys.nextFrame(() => {
            if (this._owner.isClosed) return;
            if (!loop) {
                console.log(`[BoneAnim]${id} anim (${anim}) play..`);
                skeleton.timeScale = timeScale;
                const track = skeleton.getCurrent(0);
                const dealTrackDone = () => {
                    console.log(`[BoneAnim]${id} anim (${anim}) play over, autoIdle? ` + autoIdle);
                    if (!this._owner.isClosed) {
                        if (autoIdle && !this._ignoreAutoIdle) {
                            this._lod3d.loop = true;
                            this._lod3d.animationName = this._idleAnim;
                        }
                        skeleton.timeScale = 1;
                        onComplete?.apply(thisArg);
                    }
                }
                if (track) {
                    let timer = -1;
                    let onLastAnimComplete = () => {
                        if (isNull(timer)) return
                        timer = this._timeSys.delete(timer)
                        this._onLastAnimComplete = null;
                        dealTrackDone()
                    }
                    timer = this._timeSys.delay(track.animation.duration / timeScale, () => {
                        console.log(`[BoneAnim]${id} anim (${anim}) time out`);
                        onLastAnimComplete()
                    })
                    this._onLastAnimComplete = onLastAnimComplete;
                    console.log(`[BoneAnim]${id} anim (${anim}) play.`);
                    skeleton.setTrackCompleteListener(track, this._onLastAnimComplete)
                } else {
                    dealTrackDone()
                }
            }
        })
    }
}
