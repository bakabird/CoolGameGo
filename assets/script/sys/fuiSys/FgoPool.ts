import { GObject, GObjectPool } from "fairygui-ccc370";
import { PoolModule } from "gnfun";

export default class FgoPool {
    private static _poolOfpool = new PoolModule<FgoPool>(() => new FgoPool(), (p) => p.reset(""));

    public static alloc(url: string) {
        const item = this._poolOfpool.alloc();
        item.reset(url);
        return item;
    }

    public static free = this._poolOfpool.free.bind(this._poolOfpool);

    private _pool: GObjectPool;
    private _url: string;

    private constructor() {
        this._pool = new GObjectPool();
    }

    public get() {
        // console.log('get', this._url, this._pool.count);
        return this._pool.getObject(this._url);
    }

    public ret(obj: GObject) {
        // console.log('ret', this._url, this._pool.count);
        this._pool.returnObject(obj)
        obj.removeFromParent();
    }

    public reset(url: string) {
        this._pool.clear();
        this._url = url;
    }
}