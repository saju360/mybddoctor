@echo off
setlocal

set "ROOT=%~dp0"
set "ROOT=%ROOT:~0,-1%"
set "APK=%ROOT%\android-app\app\build\outputs\apk\debug\app-debug.apk"

cd /d "%ROOT%\android-app"

if not exist "gradlew.bat" (
  echo [ERROR] gradlew.bat not found in android-app folder.
  pause
  exit /b 1
)

echo Building debug APK...
call .\gradlew.bat assembleDebug
if errorlevel 1 (
  echo [ERROR] Android build failed.
  pause
  exit /b 1
)

echo.
echo Build successful.
echo APK: %APK%
if exist "%APK%" (
  dir "%APK%"
)

pause
endlocal
