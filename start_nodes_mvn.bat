@echo off
setlocal enabledelayedexpansion

rem Base port number
set BASE_PORT=8000

rem Starting 40 instances of the Node class
for /L %%i in (1,1,40) do (
    set /A port=BASE_PORT + %%i
    echo Starting Node instance on port !port!
    start "Node %%i" cmd /c mvn exec:java -Dexec.mainClass="org.rdfkad.Node" -Dexec.args="!port!"
    timeout /t 1 > nul
)

echo Started 40 Node instances in detached mode.
endlocal