import { _decorator } from 'cc';
import { KitBase, PlayBase, SKPGame, SysBase } from 'coolgame-cc';
import { types_constructor } from 'coolgame-cc/Define';
import Go from './Go';
import TimeSys from 'coolgame-cc-sys-time';
const { ccclass, property } = _decorator;

@ccclass('CoolGame')
export class CoolGame extends SKPGame {
    protected gamename: String = "CoolGame";
    protected OnAddSys(addSys: <T extends SysBase>(type: types_constructor<T>) => T): void {
        addSys(TimeSys);
    }
    protected OnAddKit(addKit: <T extends KitBase>(type: types_constructor<T>) => T): void {
    }
    protected OnAddPlay(addPlay: <T extends PlayBase>(type: types_constructor<T>) => T): void {
    }
    protected OnEnter(): void {
        Go.game = this;
        Go.tiemSys = this.sys(TimeSys);

        Go.tiemSys.delay(1, () => {
            console.log("timeSys test...")
        })
    }
}

