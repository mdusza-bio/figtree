@echo off
REM ============================================================
REM  MyFigTree MD - uruchomienie programu
REM  Kliknij dwa razy, zeby otworzyc zbudowana wersje.
REM ============================================================

set "JAVA_HOME=%USERPROFILE%\tools\jdk8u502-b07"

cd /d "%~dp0"

if not exist "dist\figtree.jar" (
    echo.
    echo BLAD: nie znaleziono dist\figtree.jar
    echo Uruchom najpierw build.bat, zeby zbudowac program.
    echo.
    pause
    exit /b 1
)

start "" "%JAVA_HOME%\bin\javaw.exe" -jar "%~dp0dist\figtree.jar"
