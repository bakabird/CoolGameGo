@echo off
chcp 65001
call node %~dp0version_md5file.js %1
pause