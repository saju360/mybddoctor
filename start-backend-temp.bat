@echo off
setlocal
set "ROOT=%~dp0"
set "ROOT=%ROOT:~0,-1%"
cd /d "%ROOT%\backend-api"
"%ROOT%\tools\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run > "%ROOT%\backend-run-live.log" 2>&1
