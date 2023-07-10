import { _decorator, game, sys } from 'cc';
import { KitBase, PlayBase, SKPGame, SysBase } from 'coolgame-cc';
import { types_constructor } from 'coolgame-cc/Define';
import Go from './Go';
import TimeSys from 'coolgame-cc-sys-time';
import { DlgKit, FUISys } from 'coolgame-cc-sys-fui';
import { LoaderDlg } from './dlgs/LoaderDlg';
import Config from '../excel/Config';
import Xls from '../excel/Xls';
import Plat from './plat/Plat';
import CCCHotfix from './CCCHotfix';
import { LoaderPlay } from './play/LoaderPlay';
import { MainDlg } from './dlgs/MainDlg';
import { CountdownRunner } from 'gnfun';
const { ccclass, property } = _decorator;

@ccclass('CoolGame')
export class CoolGame extends SKPGame {
    protected gamename: String = "CoolGame";
    protected OnAddSys(addSys: <T extends SysBase>(type: types_constructor<T>) => T): void {
        Xls.init();
        addSys(TimeSys);
        addSys(FUISys)
    }
    protected OnAddKit(addKit: <T extends KitBase>(type: types_constructor<T>) => T): void {
        addKit(DlgKit);
    }
    protected OnAddPlay(addPlay: <T extends PlayBase>(type: types_constructor<T>) => T): void {
        addPlay(LoaderPlay)
    }
    protected OnEnter(): void {
        this._initSKP();
        this._load()
    }
    private _initSKP() {
        const fuiSys = this.sys(FUISys);
        const timeSys = this.sys(TimeSys);
        const dlgKit = this.kit(DlgKit)
        fuiSys.init(timeSys, () => Plat.inst.plat, () => Plat.inst.channel);
        Go.game = this;
        Go.fuiSys = fuiSys;
        Go.tiemSys = timeSys;
        Go.dlgKit = dlgKit;
    }

    private _load() {
        let countdownEnter = new CountdownRunner(2, this._enter.bind(this));
        let loaderPlay = this.play(LoaderPlay);
        let uid = sys.localStorage.getItem('NOW')
        if (!uid) {
            uid = Date.now().toString() + "_" + (Math.random() * 100 << 0);
            sys.localStorage.setItem('NOW', uid);
        }
        Plat.inst.init({
            popTip: (tip: string) => {
                console.log(tip)
            },
            isLandsacpe: false,
        })
        Plat.inst.setLoadingProgress(0);
        loaderPlay.fetchLoaderDlg().then(() => {
            Plat.inst.setLoadingProgress(33);
            Plat.inst.checkPlatReady(env => {
                loaderPlay.dealHotfix(env).then(() => {
                    Plat.inst.setLoadingProgress(66);
                    Plat.inst.login(() => {
                        loaderPlay.loadMain().then(() => {
                            Plat.inst.loadingComplete();
                            countdownEnter.countdown();
                        }).catch(err => {
                            console.error(err);
                        })
                    }, uid);
                })
            });
        }).catch(err => {
            console.error(err);
        })
        Go.tiemSys.delay(0.666, countdownEnter.bindedCountdown);
    }

    private _enter() {
        Go.dlgKit.fetchDlg(MainDlg);
        LoaderDlg.me.close();
        console.log("打印 Excel 数据：")
        Xls.exampleDatasArray.forEach((v, i) => {
            console.log(`[${i}]`, v);
        })
    }
}

