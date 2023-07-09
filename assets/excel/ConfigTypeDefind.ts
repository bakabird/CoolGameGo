export interface exampleData {
    /** ID */
    id: number;
    /** 名称 */
    name: string;
    /** 排序 */
    order: number;
    /** 子ID */
    include: Array<number>;
};