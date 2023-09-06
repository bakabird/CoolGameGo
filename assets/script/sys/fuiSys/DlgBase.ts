import { Color } from "cc";
import { GComponent, GGraph, GLoader, GObject, LoaderFillType } from "fairygui-ccc370";
import { DlgKit, DlgLayer } from "./DlgKit";
import UIBase from "./UIBase";
import { UIDocker } from "./UIDocker";
import { deepClone } from "gnfun";

enum DockCompleteDo {
    None = 0,
    Hide = 1,
    Close = 2,
}

export abstract class DlgBase extends UIBase {
    public static readonly SystemCtrl = {
        /**
         * 关闭按钮
         */
        CloseButton: 'sys_closeButton',
        /**
         * 黑色半透明遮罩。如果 dlg 中没有设置，则会自动创建一个。
         */
        BlackGraph: 'sys_black',
    }

    private _bg: GObject = null;
    //停靠
    private _docker: UIDocker;
    private _isClosing: boolean;
    private _internal_isShow: boolean;

    public abstract get dlgRes(): string;

    public get dlgPak(): string {
        return "Main"
    };

    public get dlgLayer(): DlgLayer {
        return DlgLayer.Base;
    }

    public get isClosing() {
        return this._isClosing;
    }

    public init(fgc: GComponent, dlgkit: DlgKit) {
        super.init(fgc, dlgkit);
        this._isClosing = false;
        this._internal_isShow = true;
        this.node.name = this.dlgRes;
        this._initFullScreen();
        this._initBlackGraph();
        this._initCloseButton();
        this._initPlatAjust();
        this._initChannelAjust();
        this.OnInit();
    }

    public show() {
        if (this._internal_isShow) return;
        if (this.isClosed || this.isClosing) return;
        super.show();
        this._bg && (this._bg.visible = true);
        this._dockOut();
        this._internal_isShow = true;
    }

    public hide() {
        if (!this._internal_isShow) return;
        if (this.isClosed || this.isClosing) return;
        this._dockBack(DockCompleteDo.Hide);
        this._internal_isShow = false;
    }

    private _internal_hide() {
        super.hide();
        this._bg && (this._bg.visible = false);
    }

    private _initCloseButton() {
        let closeBtn = this.getChild(DlgBase.SystemCtrl.CloseButton);
        if (closeBtn) {
            closeBtn.onClick(this.close, this);
        }
    }

    private _initPlatAjust() {
        this.ajustPlat("")
    }

    private _initChannelAjust() {
        this.ajustChannel("")
    }

    private _initFullScreen() {
        if (this.OnGetFullScreen()) {
            this.fgc.makeFullScreen()
        }
    }

    private _initBlackGraph() {
        let blackGraph: GObject = this.getChild(DlgBase.SystemCtrl.BlackGraph);
        if (!blackGraph) {
            const bgAlpha = Math.floor(this.OnGetBgAlpha() * 255);
            if (bgAlpha > 0) {
                const bgType = this.OnGetBgType();
                if (bgType == "loader") {
                    let bg = new GLoader();
                    let w = this.fgc.width;
                    let h = this.fgc.height;
                    bg.setPosition(-w, -h);
                    bg.setSize(w * 3, h * 3);
                    bg.fill = LoaderFillType.ScaleFree;
                    bg.color = new Color(0, 0, 0, bgAlpha);
                    bg.url = this.OnGetBgLoaderResUrl();
                    bg.touchable = true;
                    let rootInParentIndex = this.fgc.parent.getChildIndex(this.fgc);
                    this.fgc.parent.addChildAt(bg, rootInParentIndex);
                    this.fgc.opaque = false;
                    blackGraph = bg;
                } else {
                    let bg = new GGraph();
                    let w = this.fgc.width;
                    let h = this.fgc.height;
                    let blackColor = new Color(0, 0, 0, bgAlpha);
                    bg.setPosition(-w, -h);
                    bg.setSize(w * 3, h * 3);
                    bg.drawRect(0, blackColor, blackColor);
                    let rootInParentIndex = this.fgc.parent.getChildIndex(this.fgc);
                    this.fgc.parent.addChildAt(bg, rootInParentIndex);
                    this.fgc.opaque = false;
                    blackGraph = bg;
                }
            }
        }
        if (blackGraph) {
            blackGraph.onClick(() => {
                if (this.OnBgClickClose()) {
                    this.close();
                }
            }, this);
            this._bg = blackGraph;
        }
    }

    public close() {
        if (this.isClosed || this.isClosing) {
            console.warn("dlg alredy closed.")
            return
        }
        this._isClosing = true;
        this.fgc.touchable = false;
        this._dockBack(DockCompleteDo.Close);
    }

    /**
    * 启用停靠
    * @param target 停靠执行的ui对象，名字或者对象实例，可选，默认DlgBase.SystemCtrl.Docker
    * @param bg 停靠界面背景对象执行alpha渐变的对象，可选，默认DlgBase.SystemCtrl.Black
    * @param dockPars target停靠参数，默认UIDocker.Dock.Left
    * @param bgDockPars bg停靠参数，默认UIDocker.Dock.Fade
    */
    public dock(dockPars?: any, bgDockPars?: any): void {
        this._docker = UIDocker.destroy(this._docker);
        dockPars || (dockPars = UIDocker.Dock.Left);
        bgDockPars || (bgDockPars = UIDocker.Dock.Fade);
        let docker: UIDocker = UIDocker.create(this.fgc, dockPars);
        if (this._bg) {
            const bgPars = deepClone(bgDockPars)
            bgPars.out_dur = dockPars.out_dur;
            bgPars.back_dur = dockPars.back_dur;
            let bgdocker = UIDocker.create(this._bg, bgPars);
            docker.take(bgdocker);
        }
        dockPars.pivotX && (this.fgc.pivotX = dockPars.pivotX);
        dockPars.pivotY && (this.fgc.pivotY = dockPars.pivotY);
        this._docker = docker;
        this._dockOut();
    }

    /**
     * 提到最上层
     */
    public moveToFront(): void {
        if (this._bg) {
            this._bg.parent.setChildIndex(this._bg, this._bg.parent.numChildren - 1);
        }
        super.moveToFront();
    }

    private _handleDockOver(reverse: boolean, completeDo: DockCompleteDo): void {
        if (reverse) {
            if (completeDo === DockCompleteDo.Hide) {
                this._internal_hide();
            }
            else if (completeDo === DockCompleteDo.Close) {
                this._internal_close();
            }
        }
        else {
            this.OnDockOut();
            this.fgc.touchable = true;
        }
    }

    /**
     * 停靠出来并显示（内部调用）
     */
    public _dockOut(): void {
        if (this._docker) {
            this._docker.dockOut(this._handleDockOver.bind(this, false, DockCompleteDo.None));
        }
        else {
            this._handleDockOver(false, DockCompleteDo.None);
        }
    }

    private _dockBack(completeDo: DockCompleteDo): void {
        if (this._docker) {
            this._docker.dockBack(this._handleDockOver.bind(this, true, completeDo));
        }
        else {
            this._handleDockOver(true, completeDo);
        }
    }

    private _internal_close() {
        // console.log(this.dlgRes, "_internal_close");
        this.OnClose();
        super.close();
        if (this._bg) {
            this._bg.dispose();
            this._bg = null;
        }
    }

    /**
     * <To-Override>
     * 弹窗关闭时调用
     */
    protected OnClose(): void {

    };

    /**
     * <To-Override>
     * 窗口进入的停靠动画结束
     */
    protected OnDockOut() {

    }

    /**
     * 点击背景是否关闭
     */
    protected OnBgClickClose(): boolean {
        return true;
    }

    /**
     * 窗口是否要全屏化
     * @returns 
     */
    protected OnGetFullScreen(): boolean {
        return true;
    }

    /**
     * 获取背景透明度，0表示不需要背景
    */
    protected OnGetBgAlpha(): number {
        return 0.7;
    }

    /**
     * 背景透明度大于0时有效。背景类型
     * @returns "loader" | "graph"
     */
    protected OnGetBgType(): "loader" | "graph" {
        return "loader";
    }

    /**
     * 背景类型为 "loader" 时起作用
     * @returns 背景Loader资源路径
     */
    protected OnGetBgLoaderResUrl(): string {
        return "ui://9fdeszvrl5pz1c";
    }

    /**
     * <To-override>
     * 弹窗初始化时调用
     */
    protected abstract OnInit(): void;


}