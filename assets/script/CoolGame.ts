import { _decorator, sys } from 'cc';
import { KitBase, PlayBase, SKPGame, SysBase } from 'coolgame-cc';
import Go from './Go';
import { LoaderDlg } from './dlgs/LoaderDlg';
import Xls from '../excel/Xls';
import Plat from './plat/Plat';
import { LoaderPlay } from './play/LoaderPlay';
import { MainDlg } from './dlgs/MainDlg';
import { CountdownRunner } from 'gnfun';
import { DlgKit } from './sys/fuiSys/DlgKit';
import { FUISys } from './sys/fuiSys/FUISys';
import TimeSys from './sys/timeSys/TimeSys';
const { ccclass, property } = _decorator;

@ccclass('CoolGame')
export class CoolGame extends SKPGame {
    protected gamename: String = "CoolGame";
    protected OnAddSys(addSys: <T extends SysBase>(type: new (...args: any[]) => T) => T): void {
        Go.game = this;
        Xls.init();
        Go.tiemSys = addSys(TimeSys);
        Go.fuiSys = addSys(FUISys)
    }
    protected OnAddKit(addKit: <T extends KitBase>(type: new (...args: any[]) => T) => T): void {
        Go.dlgKit = addKit(DlgKit);
    }
    protected OnAddPlay(addPlay: <T extends PlayBase>(type: new (...args: any[]) => T) => T): void {
        addPlay(LoaderPlay)
    }
    protected OnEnter(): void {
        this._initSKP();
        this._load()
    }
    private _initSKP() {
        Go.fuiSys.init(Go.tiemSys, () => Plat.inst.plat, () => Plat.inst.channel);
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

