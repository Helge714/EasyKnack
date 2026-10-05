@echo off
setlocal
title EasyKnack Installer Build
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build-installer.ps1" %*
set "buildExit=%ERRORLEVEL%"
echo.
if not "%buildExit%"=="0" echo Build failed. See the error and log path above.
pause
exit /b %buildExit%
