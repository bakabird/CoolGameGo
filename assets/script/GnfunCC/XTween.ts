import { Tween } from "cc";

export namespace GnfunCC {
    export class XTween<T> extends Tween<T> {
        /**
         * 请在此处提前注册好 迅速完成时 需要进行的事务
         */
        private _onCompleteNow: (() => void) | null = null;
        public onCompleteNow(func: () => void): XTween<T> {
            this._onCompleteNow = func;
            return this;
        }
        /**
         * 迅速完成
         */
        public completeNow(): void {
            this.stop();
            if (this._onCompleteNow) {
                this._onCompleteNow();
                this._onCompleteNow = null;
            } else {
                console.error("u are not sign 'onQuickEnd'")
            }
        };
    }
}

