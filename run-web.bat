@echo off
setlocal

set "ROOT=%~dp0"
set "ROOT=%ROOT:~0,-1%"
cd /d "%ROOT%\admin-web"

where npm >nul 2>nul
if errorlevel 1 (
  echo [ERROR] npm not found in PATH. Install Node.js LTS first.
  pause
  exit /b 1
)

if not exist "node_modules" (
  echo Installing web dependencies...
  call npm install
  if errorlevel 1 (
    echo [ERROR] npm install failed.
    pause
    exit /b 1
  )
)

echo Starting admin web on http://127.0.0.1:5173 ...
call npm run dev -- --host 127.0.0.1 --port 5173

endlocal
