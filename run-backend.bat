@echo off
setlocal

set "ROOT=%~dp0"
set "ROOT=%ROOT:~0,-1%"
set "MAVEN_CMD=%ROOT%\tools\apache-maven-3.9.9\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
  echo [ERROR] Maven not found at: %MAVEN_CMD%
  pause
  exit /b 1
)

cd /d "%ROOT%\backend-api"

echo Starting backend on http://localhost:8080 ...
call "%MAVEN_CMD%" spring-boot:run

endlocal
