import { GObject } from "fairygui-ccc370";
import UIBase from "./UIBase";
import { DlgKit } from "./DlgKit";

export default abstract class UIWrap extends UIBase {
    public init(fgo: GObject, dlgkit: DlgKit) {
        super.init(fgo, dlgkit);
        this.OnInit();
    }

    public close() {
        if (this.isClosed) {
            console.error("dlg alredy closed.")
            return
        }
        this.OnClose();
        super.close();
    }

    protected OnDisposeSelfFgo(selfFgo: GObject): void {
        // wrap 默认不销毁自身节点，而是随着父节点销毁而销毁
    }

    /**
     * <To-Override>
     * 装饰关闭时调用
     */
    protected OnClose(): void {

    };

    /**
     * <To-override>
     * 装饰初始化时调用
     */
    protected abstract OnInit(): void;
}