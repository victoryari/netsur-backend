@echo off
REM Detiene el servidor Ktor anterior (si hay) y lo vuelve a levantar en segundo plano.
REM La salida completa queda en server.log
setlocal

set ROOT=C:\laragon\www\ticket
set BIN=%ROOT%\server\build\install\ticket-server\bin\ticket-server.bat
set LOG=%ROOT%\server-%RANDOM%.log

echo Deteniendo servidor anterior...
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { $_.CommandLine -like '*ticket-server*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force -ErrorAction SilentlyContinue }"

REM `ping` en lugar de `timeout`: este ultimo falla con la entrada redirigida.
ping -n 3 127.0.0.1 >nul

if exist "%LOG%" del /q "%LOG%"

echo Iniciando servidor Ktor...
start "ticket-server" /min cmd /c call "%BIN%" ^> "%LOG%" 2^>^&1

echo Esperando 15 s...
ping -n 16 127.0.0.1 >nul

echo.
echo --- Estado ---
curl -s -m 8 http://localhost:8080/health
echo.
echo Log: %LOG%
endlocal
