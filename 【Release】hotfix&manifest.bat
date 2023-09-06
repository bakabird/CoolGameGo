chcp 65001
@echo off
echo 【Release】热更新 Manifest 生成器
call node version_get.js


set /p version="请输入版本号(以 1.3.2 为格式):"
set /p upload="是否上传资源到远程(Y/N)? "
if /i "%upload%"=="Y" (
    set /p confirm="确认上传资源到【【【线上】】】吗？这将更改【【【线上】】】的内容！！！(Y/N)? "
)

@REM 这里修改一下你放热更资源的远程地址
set remote=https://www.bttx199.com/Product/gameasset/path2project/native/release/
@REM 这里修改一下热更资源路径
set respath=build\hotfix\data
set destpath=remote-assets\
set demark=%destpath%当前manifest是DEBUG
set remark=%destpath%当前manifest是RELEASE
call node version_generator.js -v %version% -u %remote% -s %respath% -d %destpath%
if exist %demark% (
    del %demark%
)
if exist %remark% (
    del %remark%
)
echo. > %remark%


if /i "%upload%"=="Y" (
    if /i "%confirm%"=="Y" (
        call node version_publish.js -r
        echo "发布完毕，再次获取版本信息。"
        call node version_get.js
        pause
    ) else (
        echo 程序结束
    )
) else (
    echo 程序结束
)

