import { SysBase } from "coolgame-cc";
import { FreeList } from "gnfun";
import { IDisposable } from "../../CommonInterface";

/**
 * 计时器句柄
 */
interface ITimer {
    /**
     * 计时器全局id（唯一）
     */
    readonly id: number;
    /**
     * 当前计时器是否完毕
     */
    readonly complete: boolean;
    /**
     * 计时器的回调间隔，秒
     */
    readonly interval: number;
    /**
     * 计时器的被回调函数
     */
    readonly method: Function;
    /**
     * 被回调者
     */
    readonly caller: any;
    /**
     * 尝试手工触发一次计时器
     */
    trigger(): ITimer;
}

namespace Private {
    export class Timer implements ITimer {
        public id: number;
        public intervalMS: number;
        public loopLeft: number;
        public latestTriggerTime: number;
        public method: Function;
        public caller: any;
        public args: any[];
        public get interval(): number {
            return this.intervalMS * 0.001;
        }
        public get complete(): boolean {
            return this.loopLeft === 0;
        }
        public trigger(): Timer {
            if (!this.args || this.args.length === 0) {
                this.args = [this.id];
            }
            this.method.apply(this.caller, this.args);
            return this;
        }
    }
}

export class TimerModule implements IDisposable {
    private _valid: boolean;
    private _everTimers: Array<number> = null;

    constructor(private _sys: TimeSys) {
        this._valid = true
        this._everTimers = [];
    }

    public Dispose(): void {
        this._everTimers?.forEach(tid => {
            this._sys.delete(tid);
        })
        this._sys = null
        this._valid = false
    }

    public delay(delay: number, func: Function, caller?: any, ...arg: any[]) {
        return this._addTimer(delay, 1, func, caller, ...arg);
    }

    public nextframe(func: Function, caller?: any, ...arg: any[]) {
        return this._addTimer(0, 1, func, caller, ...arg);
    }

    private _addTimer(interval: number, loops: number, func: Function, caller?: any, ...arg: any[]) {
        if (!this._valid) {
            console.warn("module isnot valid cant add timer");
            return;
        }
        const t = this._sys.timer(interval, loops, func, caller, ...arg);
        if (!this._everTimers) {
            this._everTimers = [];
        }
        this._everTimers.push(t);
        return t;
    }

}

/**
 * 定时器服务
 */
export default class TimeSys extends SysBase {
    private _timers: FreeList<Private.Timer>;
    private _pool: Array<Private.Timer>;
    private _nextGlobalID: number;

    public sysName: string = "TimeSys";

    protected OnInit(complete: () => void): void {
        this._nextGlobalID = 1;
        this._pool = new Array<Private.Timer>();
        this._timers = new FreeList<Private.Timer>();
        complete();
    }

    protected OnLateInit(complete: () => void): void {
        complete()
    }

    protected OnDispose(): void {
        this._pool = null;
        this._timers = null;
    }

    protected update(dt: number) {
        const curTime = Date.now();
        this._timers.foreach_safe((t) => {
            if (t.loopLeft > 0) {
                if (t.latestTriggerTime + t.intervalMS <= curTime) {
                    t.latestTriggerTime = curTime;
                    --t.loopLeft;
                    t.trigger();
                }
            } else {
                this._gabage(t);
                this._timers.remove(t);
            }
            return true;
        })
    }

    /**
     * 当前时间，秒
     */
    public static get seconds(): number {
        return Date.now() * 0.001;
    }

    /**
     * 注册并开始一个定时器
     * @param interval 间隔时间，单位秒
     * @param loops 循环次数，小于0表示无限循环
     * @param caller 回调者（用于this, 可以为null）
     * @param func 回调函数
     * @param args 回调参数，选填
     */
    public timer(interval: number, loops: number, func: Function, caller?: any, ...args: any[]): number {
        return this._timer(interval, loops, func, caller, args);
    }

    /**
     * 延迟一定时间调用（一次）
     * @param interval 延迟多久执行，单位秒
     * @param func 
     * @param caller 
     * @param args 
     */
    public delay(interval: number, func: Function, caller?: any, ...args: any[]): number {
        return this._timer(interval, 1, func, caller, args);
    }

    /**
     * 注册一个帧回调计时器
     * @param loops 回调次数，小于0表示无限循环
     * @param caller 
     * @param func 
     * @param args 
     */
    public frame(loops: number, func: Function, caller?: any, ...args: any[]): number {
        return this._timer(0, loops, func, caller, args);
    }

    /**
     * 注册一个延迟一帧执行的计时器
     * @param caller 
     * @param func 
     * @param args 
     */
    public nextFrame(func: Function, caller?: any, ...args: any[]): number {
        return this._timer(0, 1, func, caller, args);
    }

    /**
     * 删除给定计时器
     * @param timerID 计时器id
     * @return undefined，方便链式表达
     */
    public delete(timerID: number): undefined {
        if ((timerID ?? false) && timerID > 0) {
            let t = this._timers.removeFirstIf((t: ITimer) => {
                return t.id === timerID;
            });
            if (t) {
                this._gabage(t);
            }
        }
        return undefined;
    }

    public isTimerComplete(timerID: number): boolean {
        let t = this._getTimer(timerID);
        return t ? t.complete : true;
    }
    public getTimerInterval(timerID: number): number {
        let t = this._getTimer(timerID);
        return t ? t.interval : -1;
    }
    public getTimerCallback(timerID: number): Function {
        let t = this._getTimer(timerID);
        return t ? t.method : undefined;
    }
    public getTimerThisObj(timerID: number): any {
        let t = this._getTimer(timerID);
        return t ? t.caller : undefined;
    }
    public getTimerNextTriggerTime(timerID: number): number {
        let t = this._getTimer(timerID);
        return t ? t.latestTriggerTime + t.interval : 0;
    }
    public getTimerArgs(timerID: number): any[] {
        let t = this._getTimer(timerID);
        return t ? t.args : undefined;
    }
    public triggerTimer(timerID: number): number {
        let t = this._getTimer(timerID);
        if (t) {
            t.trigger();
        }
        return timerID;
    }

    public generateModule(): TimerModule {
        return new TimerModule(this);
    }

    private _getTimer(timerID: number): Private.Timer {
        if ((timerID ?? true) || timerID <= 0) {
            return undefined;
        }
        return this._timers.findFirstIf((t: ITimer) => {
            return t.id === timerID;
        });
    }

    private _timer(interval: number, loops: number, func: Function, caller?: any, args?: any[]): number {
        let t = this._pool.length > 0 ? this._pool.pop() : new Private.Timer();
        t.id = this._nextGlobalID++;
        t.intervalMS = interval * 1000;
        t.loopLeft = loops < 0 ? Number.MAX_VALUE : loops === 0 ? 1 : loops;
        t.latestTriggerTime = Date.now();
        if (args) {
            args.push(t.id);
        }
        t.method = func;
        t.caller = caller;
        t.args = args;
        this._timers.push(t);
        return t.id;
    }
    private _gabage(t: Private.Timer): void {
        t.method = undefined;
        t.caller = undefined;
        t.args = undefined;
        this._pool.push(t);
    }

    public get timerCount() { return this._timers.length }
}