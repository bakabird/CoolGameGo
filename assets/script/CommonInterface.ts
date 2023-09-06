/**常见通用 interface 定义 */
export interface IDisposable {
    Dispose(): void;
}

export type Call = () => void;

export type ctor<T> = { new(): T };