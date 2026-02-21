@echo off
cd /d "%~dp0"
if not exist bin mkdir bin
echo Compiling latest version...
javac -d bin -sourcepath src src\main\Main.java src\main\GamePanel.java src\main\KeyHandler.java src\entity\*.java src\tile\*.java src\ui\*.java
IF %ERRORLEVEL% NEQ 0 (
    echo.
    echo ****************************************
    echo * COMPILATION FAILED! CHECK ERRORS ABOVE *
    echo ****************************************
    pause
    exit /b
)
echo Copying resources...
xcopy /s /y /i res bin > nul
echo Launching Game...
start javaw -cp bin main.Main
exit