@echo off
setlocal ENABLEDELAYEDEXPANSION

REM === 跳到脚本所在目录 ===
cd /d "%~dp0"

REM === 路径与文件 ===
set APP_DIR=app
set VENV_DIR=%APP_DIR%\.venv
set REQS=used-requirements.txt

if not exist "%REQS%" set REQS=%APP_DIR%\requirements.txt

echo [INFO] AppDir=%APP_DIR%
echo [INFO] VenvDir=%VENV_DIR%
echo [INFO] Requirements=%REQS%
echo.

REM === 创建虚拟环境（不污染本机 Python） ===
if not exist "%VENV_DIR%" (
  echo [INIT] Creating venv...
  py -3 -m venv "%VENV_DIR%" 2>nul || python -m venv "%VENV_DIR%"
  if errorlevel 1 (
    echo [ERROR] Failed to create venv. Make sure Python is installed.
    pause
    exit /b 1
  )
)

set PYEXE=%VENV_DIR%\Scripts\python.exe
set PIPEXE=%VENV_DIR%\Scripts\pip.exe

REM === 升级 pip 并安装依赖（仅装到 .venv） ===
echo [PIP] Upgrading pip...
"%PYEXE%" -m pip install --upgrade pip

if exist "%REQS%" (
  echo [PIP] Installing deps from "%REQS%" ...
  "%PIPEXE%" install -r "%REQS%"
) else (
  echo [WARN] No requirements file found. Skipping dependency install.
)

REM === 打开浏览器（后台启动，等2秒） ===
start "" powershell -NoProfile -Command "Start-Sleep 2; Start-Process 'http://127.0.0.1:8000/index.html'"

REM === 前台运行后端：实时打印 + 同步写入 app\app.log ===
pushd "%APP_DIR%"
echo [RUN] Starting backend in foreground. Logs will also be saved to app.log
powershell -NoProfile -Command ^
  "$env:PYTHONIOENCODING='utf-8'; & '%PYEXE%' 'main.py' 2>&1 | Tee-Object -FilePath 'app.log' -Append"
set EXITCODE=%ERRORLEVEL%
popd

echo.
echo [EXIT] Backend exited with code %EXITCODE%
echo (Logs saved at %APP_DIR%\app.log)
echo.
pause
endlocal
