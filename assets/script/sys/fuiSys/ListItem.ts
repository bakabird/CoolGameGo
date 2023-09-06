import UIWrap from "./UIWrap";

export default abstract class ListItem extends UIWrap {
    protected curData: any;
    private _index: number;

    public getCurData() {
        return this.curData
    }

    public refresh(data: any, index: number) {
        this.curData = data;
        this._index = index;
        this.OnRefresh(this.curData, this._index);
    }

    public internalRefresh() {
        this.OnRefresh(this.curData, this._index);
    }

    public get index(): number {
        return this._index;
    }

    protected abstract OnRefresh(data: any, index: number): void;
} 