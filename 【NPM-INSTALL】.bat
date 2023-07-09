@echo off
chcp 65001
echo 当前所在文件夹：%CD%
rmdir /s /q node_modules
call npm i
cd extensions
for /D %%i in (".\*") do (
    cd %%i
    echo 当前所在文件夹：%CD%%%i
    rmdir /s /q node_modules
    call npm i
    cd ..
)
pause