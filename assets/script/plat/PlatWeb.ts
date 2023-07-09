import PlatBase, { GamePlat } from "./PlatBase";


export default class PlatWeb extends PlatBase {

    private static _instance;

    public static getInstance() {
        if (!this._instance) {
            this._instance = new PlatWeb();
        }
        return this._instance;
    }

    public get uma() {
        return {
            trackEvent(eventId, params) {
                console.log('[uma]trackEvent', eventId, params)
            },
        } as UMA
    }


    public get plat() {
        return GamePlat.web;
    }
}