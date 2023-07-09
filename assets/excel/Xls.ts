import datas from './Config';
import {exampleData} from "./ConfigTypeDefind";


export default class Xls {
    public static exampleDatasArray: Array<exampleData>;
public static exampleDatasById: { [key in number]: exampleData };


    public static init() {
                this.exampleDatasArray = this._arrayData("example", datas);
        this.exampleDatasById = datas["example"];

    }

    private static _arrayData(key: string, datas: { [key in string]: any }) {
        let values = [];
        let items = datas[key];
        for (let key1 in items) {
            values.push(items[key1]);
        }
        return values;
    }
}
