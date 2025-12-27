@echo off
REM Gemini API Setup Script for BorcTakip Android App
REM Turkish UI support

setlocal enabledelayedexpansion

cls
echo ============================================
echo Borç Takip - Gemini API Kurulum Yardımcısı
echo ============================================
echo.

REM 1. Check if local.properties exists
if not exist "local.properties" (
    echo UYARI: local.properties dosyası bulunamadı!
    echo.
    echo Bir tane oluşturuluyor...
    
    for /f "delims=" %%i in ('where android.bat 2^>nul ^| findstr SDK') do (
        set "android_path=%%i"
    )
    
    if defined android_path (
        for /f "delims=" %%i in ('powershell -Command "Split-Path -Path '!android_path!' -Parent"') do (
            set "sdk_path=%%i"
        )
    ) else (
        set "sdk_path=%LOCALAPPDATA%\Android\Sdk"
    )
    
    echo sdk.dir=!sdk_path! > local.properties
    echo Yapılandırıldı: !sdk_path!
    echo.
)

REM 2. Get API Key from user
echo API Anahtarını yapıştırın (Google Cloud Console'den):
set /p api_key="API Key: "

if "!api_key!"=="" (
    echo HATA: Boş API anahtarı!
    exit /b 1
)

REM 3. Update local.properties
for /f "delims=" %%i in ('findstr /c:"GEMINI_API_KEY" local.properties') do (
    REM API Key already exists, update it
    powershell -Command "((Get-Content local.properties) -replace 'GEMINI_API_KEY=.*', 'GEMINI_API_KEY=!api_key!') | Set-Content local.properties"
    echo API Key güncellendi.
    goto check_build
)

REM Add API Key if not exists
echo GEMINI_API_KEY=!api_key! >> local.properties
echo API Key eklendi.

:check_build
echo.
echo ============================================
echo Derleme yapılıyor...
echo ============================================
echo.

REM Set JAVA_HOME
if not defined JAVA_HOME (
    for /d %%i in ("%PROGRAMFILES%\Java\jdk*") do (
        set "JAVA_HOME=%%i"
        goto java_found
    )
    
    :java_found
    echo JAVA_HOME ayarlandı: !JAVA_HOME!
)

REM Build
call gradlew.bat :app:assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✓ Derleme başarılı!
    echo.
    echo APK: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo ✗ Derleme başarısız!
    echo.
    echo Sorun Giderme:
    echo 1. GEMINI_API_SISTEM_REHBERI.md dosyasını oku
    echo 2. API anahtarının doğru olduğunu kontrol et
    echo 3. SHA-1 parmak izini Google Cloud'da kontrol et
    echo.
    echo Parmak izi kontrol:
    echo keytool -list -v -keystore "%%USERPROFILE%%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | findstr "SHA1"
    exit /b 1
)

echo.
echo.
echo ============================================
echo ✓ Kurulum Tamamlandı
echo ============================================
echo.
echo Sonraki Adımlar:
echo 1. Android Studio'da projeyi aç
echo 2. GeminiAIScreen.kt'yi inceleyerek entegrasyonu gör
echo 3. Uygulamayı cihazda test et
echo.
echo Sorun yaşarsan: GEMINI_API_SISTEM_REHBERI.md oku
echo.
pause
