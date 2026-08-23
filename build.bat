@echo off
REM ============================================================
REM  MyFigTree MD - budowanie programu
REM  Kliknij dwa razy, zeby skompilowac zmiany w kodzie.
REM ============================================================

REM JDK i Ant sa rozpakowane lokalnie (nie sa zainstalowane w systemie),
REM wiec trzeba na nie wskazac recznie.
set "JAVA_HOME=%USERPROFILE%\tools\jdk8u502-b07"
set "ANT_HOME=%USERPROFILE%\tools\apache-ant-1.10.15"

cd /d "%~dp0"

if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo.
    echo BLAD: nie znaleziono kompilatora Javy w:
    echo   %JAVA_HOME%
    echo.
    pause
    exit /b 1
)

if not exist "%ANT_HOME%\bin\ant.bat" (
    echo.
    echo BLAD: nie znaleziono Anta w:
    echo   %ANT_HOME%
    echo.
    pause
    exit /b 1
)

echo Buduje MyFigTree MD...
echo.

call "%ANT_HOME%\bin\ant.bat"

if errorlevel 1 (
    echo.
    echo ======================================================
    echo  BUDOWANIE NIE POWIODLO SIE
    echo  Poszukaj wyzej linii ze slowem "error" - jest tam
    echo  nazwa pliku i numer linii z bledem.
    echo ======================================================
    echo.
    pause
    exit /b 1
)

echo.
echo ======================================================
echo  GOTOWE. Program zbudowany:
echo    %~dp0dist\figtree.jar
echo.
echo  Uruchom go klikajac run.bat
echo ======================================================
echo.
pause
