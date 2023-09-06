/**
 * 提供短链接“服务”
 * 1. 快速获取对应控制类 sys/kit/play
 * 2. 规避循环引用编译报错（如果存在逻辑上的循环调用，请自行手动修复）
 */
import type { CoolGame } from "./CoolGame";
import { DlgKit } from "./sys/fuiSys/DlgKit";
import { FUISys } from "./sys/fuiSys/FUISys";
import TimeSys from "./sys/timeSys/TimeSys";

var game: CoolGame;
var fuiSys: FUISys;
var tiemSys: TimeSys;
var dlgKit: DlgKit;

export default {
    game,

    fuiSys,
    tiemSys,

    dlgKit,
}