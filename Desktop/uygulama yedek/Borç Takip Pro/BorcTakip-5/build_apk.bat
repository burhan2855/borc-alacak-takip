@echo off
REM BorcTakip APK Build Script for Windows

echo.
echo ========================================
echo  BorcTakip Android APK Build
echo ========================================
echo.

REM Current directory
cd /d "%~dp0"

REM Check if gradlew exists
if not exist "gradlew.bat" (
    echo ERROR: gradlew.bat not found!
    echo Please run this script from project root directory
    pause
    exit /b 1
)

echo [1/3] Cleaning project...
call gradlew.bat clean
if %ERRORLEVEL% NEQ 0 (
    echo Clean failed!
    pause
    exit /b 1
)

echo.
echo [2/3] Building APK...
call gradlew.bat :app:assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Build completed successfully!
echo.
echo APK location: app\build\outputs\apk\debug\app-debug.apk
echo.
echo To install on device:
echo   adb uninstall com.burhan2855.borctakip
echo   adb install -r app\build\outputs\apk\debug\app-debug.apk
echo.
pause
