CoolGameGo

（自用）使用 CoolGame 框架开发 CocosCreator 的脚手架。

# 0x00 几个 npm库

```
npm i coolgame-cc
npm i fairygui-ccc370
npm i gulp
npm i gulp-tinypng-with-cache-rdd-mod
npm i spark-md5
npm i gnfun
```

# 0x01 几个插件

1. hot-update 热更新插件。
2. plat-helper 平台打包插件。
3. excel2ts-cc370 excel转ts 插件。

# 0x02 FairyGUI

1. FGUIProject 中是 FUI 项目。
2. fairyGUI 打包后会输出到 UI_ori 中。
3. 需要点击 压缩图片.bat 来压缩图片并移动到 resource/UI(可以在 gulpfile.ja 里修改) 中。
4. 代码中主要通过 `FUISys` 和 `DlgKit` 来操作UI。

# 0x03 Excel 打包

1. Excel配置 放在 excels目录 中。
2. 在 IDE 中通过 Panel > excel2ts-cc370 打开窗口。
3. 点击 “生成” 来将配置打包成 ts。
4. 代码中通过 `Xls` 来获取这些配置。

# 0x04 平台适配器

1. 通过 `Plat.inst` 来调用平台接口。
2. `Plat.inst.plat` 来获取当前平台。
3. `Plat.inst.channel` 来获取当前渠道（安卓）。
4. AdCfg.ts 中配置各个 平台/渠道 下的广告位。

# 0x05 热更新

1. 代码中主要通过 `CCCHotfix` 来进行热更新。
2. 热更新的 manifest 主要通过项目根目录下的 version_generator.js 生成。