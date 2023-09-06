import { GList, GObject, ListItemRenderer } from "fairygui-ccc370";
import ListItem from "./ListItem";
import UIWrap from "./UIWrap";

export default class ListWrap<T extends ListItem> extends UIWrap {
    private _itemDict: Map<string, ListItem>;
    private _itemType: { new(): T };
    private _sourceData: Array<any>;
    private _list: GList;
    protected OnInit(): void {
        this._list = this.fgc.asList;
        this._list.setVirtual();
        this._list.itemRenderer = <ListItemRenderer>this._renderListItem.bind(this);
    }

    public initList(type: { new(): T }, sourceData: Array<any>): void {
        this._itemType = type;
        this._itemDict = new Map<string, ListItem>();
        this.SourceData = sourceData;
    }

    public set SourceData(data: Array<any>) {
        this._sourceData = data;
        this._list.numItems = this._sourceData.length;
    }

    public get SourceData() {
        return this._sourceData;
    }

    public get itemDict() {
        return this._itemDict;
    }

    /**
     * 刷新对应数据位置的格子
     */
    public refreshItemByIndex(index: number): void {
        this._itemDict.forEach((value) => {
            if (value.index === index) {
                value.internalRefresh();
                return false;
            }
        })
    }

    /**刷新全部 */
    public refreshAll() {
        this._itemDict.forEach((value) => {
            value.internalRefresh();
        })
    }

    private _renderListItem(index: number, obj: GObject) {
        let wrap: ListItem;
        if (this._itemDict.has(obj.id)) {
            wrap = this._itemDict.get(obj.id);
        } else {
            wrap = this.wrap(this._itemType, obj);
            this._itemDict.set(obj.id, wrap);
        }
        wrap.refresh(this._sourceData[index], index);
    }

}