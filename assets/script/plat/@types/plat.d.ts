interface UMA {
    /**
     * params为object类型时，属性值仅支持字符串和数值两种类型;
     * 请在App.onLaunch之后调用事件。
     * 参数eventId和事件标签都不能为null，也不能为空字符串"", 参数eventId长度不能超过128个字符，参数label长度不能超过256个字符。
     * 以下保留字段，不能作为event id 及key的名称：
     * id、ts、du、token、device_name、device_model 、device_brand、country、city、channel、province、appkey、app_version、access、launch、pre_app_version、terminate、no_first_pay、is_newpayer、first_pay_at、first_pay_level、first_pay_source、first_pay_user_level、first_pay_version、page、path、openid、unionid、scene
     * @param eventId 
     * @param params 
     */
    trackEvent(eventId: string, params: object | string)
    stage: {
        /**
         * 关卡开始
         * @param arg 参数
         * @param arg.stageId 关卡ID
         * @param arg.stageName 关卡名称
         */
        onStart(arg: {
            stageId: string,
            stageName: string,
        }): void;
        /**
         * 关卡结束
         * @param arg 参数
         * @param arg.stageId 关卡ID
         * @param arg.stageName 关卡名称
         * @param arg.event 关卡结束结果
         * @param arg._um_sdu 关卡耗时(毫秒)。友盟sdk会在根据本地关卡缓存数据，计算关卡的耗时自动上报；如果希望自己统计耗时，也可以自行统计上报，上报格式为数值型，单位为毫秒
         */
        onEnd(arg: {
            stageId: string
            stageName: string
            event: string
            _um_sdu?: number
        });
        /**
         * 关卡中的自定义事件
         * @param arg 参数
         * @param arg.stageId 关卡ID
         * @param arg.stageName 关卡名称
         * @param arg.event 自定义事件名称
         * @param arg.params 自定义事件参数
         * @param arg.params.itemName 道具名称
         * @param arg.params.itemId 道具ID
         * @param arg.params.itemCount 道具数量
         * @param arg.params.itemMoney 道具金额
         * @param arg.params.desc 自定义事件描述
         */
        onRunning(arg: {
            stageId: string,
            stageName: string,
            event: string,
            params: {
                itemName: string,
                itemId?: string,
                itemCount?: number,
                itemMoney?: number,
                desc?: string,
            }
        });
    },
    level: {
        /**
         * 初始化上报
         * @param levelId 等级id
         * @param levelName 等级名称
         */
        onInitLevel(levelId: string, levelName: number);
        /**
         * 升级
         * @param levelId 等级id
         * @param levelName 等级名称
         */
        onSetLevel(levelId: string, levelName: number);
    },
}
declare let qg: {
    uma: UMA
    [key: string]: any
}
declare let wx: {
    uma: UMA
    [key: string]: any
}
declare let tt: {
    uma: UMA
    [key: string]: any
}
declare interface Window {
    uma: UMA,
    channel: string,
}