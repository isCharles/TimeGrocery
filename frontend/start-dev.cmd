@echo off
setlocal
cd /d "%~dp0"

echo Starting Time Grocery Shop frontend...
echo URL: http://localhost:5173/
echo.

call npm.cmd run dev -- --host 0.0.0.0 --port 5173

echo.
echo Dev server exited. Press any key to close this window.
pause >nul
