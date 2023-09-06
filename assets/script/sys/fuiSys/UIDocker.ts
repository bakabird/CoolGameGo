import { Tween, tween, TweenEasing } from "cc";
import { GObject } from "fairygui-ccc370";
import { deepClone, isNull, notNull, removeNullKeys } from "gnfun";


export interface UIDockerPropMod {
    x?: number;
    sx?: number;
    rx?: number;

    y?: number;
    sy?: number;
    ry?: number;

    alpha?: number;
    salpha?: number;
    ralpha?: number;

    scaleX?: number;
    rscaleX?: number
    sscaleX?: number;

    scaleY?: number;
    rscaleY?: number;
    sscaleY?: number;
}

export interface UIDockerTweenProp {
    x?: number;
    y?: number;
    alpha?: number;
    scaleX?: number;
    scaleY?: number;
}

export class UIDocker {
    /**
     * 几种基本停靠类型
     */
    public static readonly Dock = {
        /**
         * 左侧停靠
         */
        Left: {
            out: {
                rx: -120,
            },
            back: {
                x: -4,
                sx: -1,
            },
            out_ease: "backOut",
            back_ease: "backIn",
        },
        /**
         * 左侧停靠
         */
        LeftInRightOut: {
            out: {
                rx: -120,
            },
            back: {
                rx: 4,
                sx: 1,
            },
            out_ease: "backOut",
            back_ease: "backIn",
        },
        /**
         * 右侧停靠
         */
        Right: {
            out: {
                rx: 100,
            },
            back: {
                rx: 4,
                sx: 1,
            },
            out_ease: "backOut",
            back_ease: "backIn",
        },
        /**
         * 顶部停靠
         */
        Top: {
            out: {
                ry: -120,
            },
            back: {
                ry: -3,
                sy: -1,
            },
            out_ease: "backOut",
            back_ease: "backIn",
        },
        /**
         * 底部停靠
         */
        Bottom: {
            out: {
                ry: 70,
            },
            back: {
                ry: 4,
                sy: 1,
            },
            out_ease: "backOut",
            back_ease: "backIn",
        },
        /**
         * 渐显
         */
        Fade: {
            out: {
                alpha: 0,
            },
            back: {
                alpha: 0,
            },
            out_ease: "quadOut",
            back_ease: "quadIn",
        },
        /**
         * 缓慢渐显
         */
        FadeSlowly: {
            out: {
                alpha: 0,
            },
            back: {
                alpha: 0,
            },
            out_dur: 1.8,
            back_dur: 0.6,
            out_ease: "quadOut",
            back_ease: "quadIn",
        },
        /**
         * 从中间像气泡一样冒出来
         */
        Bubble: {
            out: {
                sscaleX: 0.7,
                sscaleY: 0.7,
                alpha: 0.2,
            },
            back: {
                scaleX: 0.0,
                scaleY: 0.0,
                alpha: 0,
            },
            out_ease: "elasticOut",
            back_ease: "backIn",
            out_dur: 0.6,
            back_dur: 0.6,
            pivotX: 0.5,
            pivotY: 0.5,
        },
        /**
         * 像幽灵一样浮上来
         */
        GhostUp: {
            out: {
                sscaleX: 0.9,
                sscaleY: 0.9,
                alpha: 0.8,
                ry: 10,
            },
            back: {
                alpha: 0,
                sscaleX: 0.9,
                sscaleY: 0.9,
                ry: 0,
            },
            out_ease: "backOut",
            back_ease: "backIn",
            out_dur: 0.3,
            back_dur: 0.2,
            pivotX: 0.5,
            pivotY: 0.5,
        },
        /**
         * 
         */
        GhostUp2: {
            out: {
                sscaleX: 0.9,
                sscaleY: 0.9,
                alpha: 0.8,
                ry: 10,
            },
            back: {
                alpha: 0,
                sscaleX: 0.9,
                sscaleY: 0.9,
                ry: 0,
            },
            out_ease: "backOut",
            back_ease: "backIn",
            out_dur: 0.2,
            back_dur: 0,
            pivotX: 0.5,
            pivotY: 0.5,
        }
    };

    private static _pool: Array<UIDocker>;
    /**
     * 创建一个docker但是不执行
     * @param target docker目标对象
     * @param dockPars docker参数
     * @param complete docker结束之后的回调，可选
     * @return UIDocker实例
     */
    public static create(target: any, dockPars: any): UIDocker {
        let docker = (UIDocker._pool && UIDocker._pool.length > 0) ? this._pool.pop() : new UIDocker();
        docker._reset(target, dockPars);
        return docker;
    }
    /**
     * 销毁docker
     * @param docker 被销毁的docker，可以传null
     * @return undefined
     */
    public static destroy(docker: UIDocker): undefined {
        if (docker) {
            docker._destroy();
            if (!this._pool) {
                this._pool = [];
            }
            this._pool.push(docker);
        }
        return undefined;
    }
    public static fade_ease: TweenEasing = "quadOut";


    private _target: any;
    private _outProps: any;
    private _backProps: any;
    private _outEase: TweenEasing | ((k: number) => number);
    private _backEase: TweenEasing | ((k: number) => number);
    private _outDur: number;
    private _backDur: number;

    private _tween: Tween<GObject>;
    private _reversing: boolean;
    private _takens: UIDocker[];

    private constructor() {
    }

    private _reset(target: GObject, dockPars: any) {
        this._reversing = undefined;
        this._target = target;
        this._outEase = dockPars.out_ease || "quadOut";
        this._backEase = dockPars.back_ease || "quadIn";
        this._outDur = dockPars.out_dur ?? 0.4
        this._backDur = dockPars.back_dur ?? 0.4
        let out = this._parseProps(target, dockPars.out);
        let back = this._parseProps(target, dockPars.back);
        let props = out || back;
        if (out && back) {
            props = deepClone(props);
            for (let k in back) {
                (props as any)[k] = 0;
            }
        }
        this._outProps = this._getProps(target, props);
        this._backProps = back;
        if (out) {
            this._setProps(target, out);
        }
    }
    /**
     * 停靠出来（展示出来）
     */
    public dockOut(cb?: Function): void {
        this._internal_dock(true, cb);
    }
    /**
     * 停靠回去（隐藏）
     */
    public dockBack(cb?: Function): void {
        this._internal_dock(false, cb);
    }
    /**
    * 停靠
    * @param dockOut 是否停靠出来（显示）
    */
    private _internal_dock(dockOut: boolean, cb?: Function): void {
        if (this._takens) {
            for (let t of this._takens) {
                t._internal_dock(dockOut);
            }
        }
        if (this._reversing === !dockOut) {
            cb?.();
            return;
        }
        this._clearTween();
        this._reversing = !dockOut;
        if (dockOut) {
            if (this._outProps) {
                this._tween = tween(this._target).to(this._outDur, this._outProps, {
                    easing: this._outEase,
                    onComplete: this._handleTweenComplete.bind(this, cb)
                }).start();
            }
            else {
                this._handleTweenComplete(cb);
            }
        }
        else {
            if (this._backProps) {
                this._tween = tween(this._target).to(this._backDur, this._backProps, {
                    easing: this._backEase,
                    onComplete: this._handleTweenComplete.bind(this, cb)
                }).start();
            }
            else {
                this._handleTweenComplete(cb);
            }
        }
    }


    /**
     * 顺带一个docker，跟自己绑定
     * @param docker
     * @return this
     */
    public take(docker: UIDocker): UIDocker {
        if (!docker) {
            return this;
        }
        if (!this._takens) {
            this._takens = new Array<UIDocker>();
        }
        this._takens.push(docker);
        return this;
    }
    /**
     * 删除
     */
    private _destroy(): void {
        if (this._takens) {
            for (let t of this._takens) {
                UIDocker.destroy(t);
            }
            this._takens = undefined;
        }
        this._clearTween();
        this._target = undefined;
        this._reversing = undefined;
        this._outEase = undefined;
        this._backEase = undefined;
    }

    private _handleTweenComplete(cb?: Function): void {
        this._clearTween();
        cb?.();
    }

    private _clearTween(): void {
        if (this._tween) {
            this._tween.stop();
            this._tween = undefined;
        }
    }

    private _getProps(target: GObject, props: UIDockerTweenProp): UIDockerTweenProp {
        if (isNull(props)) {
            return undefined;
        }
        let current: any = {};
        this._notNullAndSet(current, props, target, "x");
        this._notNullAndSet(current, props, target, "y");
        this._notNullAndSet(current, props, target, "alpha");
        this._notNullAndSet(current, props, target, "scaleX");
        this._notNullAndSet(current, props, target, "scaleY");
        return current;
    }

    private _setProps(target: GObject, props: UIDockerTweenProp): void {
        this._notNullAndSet(target, props, props, "x");
        this._notNullAndSet(target, props, props, "y");
        this._notNullAndSet(target, props, props, "alpha");
        this._notNullAndSet(target, props, props, "scaleX");
        this._notNullAndSet(target, props, props, "scaleY");
    }

    private _notNullAndSet(target: GObject, props: UIDockerTweenProp, valSource: UIDockerTweenProp, propName: keyof UIDockerTweenProp) {
        if (notNull(props[propName])) {
            target[propName] = valSource[propName];
        }
    }

    private _parseProps(target: GObject, mod: UIDockerPropMod): UIDockerTweenProp | undefined {
        if (isNull(mod)) return undefined;
        return removeNullKeys({
            x: this._dealPropOfMod(target, mod, "x"),
            y: this._dealPropOfMod(target, mod, "y"),
            alpha: this._dealPropOfMod(target, mod, "alpha"),
            scaleX: this._dealPropOfMod(target, mod, "scaleX"),
            scaleY: this._dealPropOfMod(target, mod, "scaleY"),
        });
    }


    private _dealPropOfMod(target: GObject, mod: UIDockerPropMod, propName: "x" | "y" | "alpha" | "scaleX" | "scaleY") {
        let base = mod[propName];
        const rMod = mod[`r${propName}`];
        const sMod = mod[`s${propName}`];
        if (rMod || sMod) {
            base ??= 0;
            if (rMod) base += target[propName] + rMod;
            if (sMod) base += target[
                propName == "x" ? "width" :
                    propName == "y" ? "width" :
                        propName
            ] * sMod;
        }
        return base;
    }
}
