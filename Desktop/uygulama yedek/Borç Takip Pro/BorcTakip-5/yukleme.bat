@echo off
REM Android SDK PATH'e ekle
set PATH=C:\Users\burha\AppData\Local\Android\Sdk\platform-tools;C:\Android\Sdk\platform-tools;%PATH%

REM ADB cihazlarını listele
echo Bağlı cihazlar ve emülatörler:
adb devices

REM APK yolu
set APK_PATH=C:\Users\burha\Desktop\uygulama yedek\Borç Takip Pro\BorcTakip-5\app\build\outputs\apk\debug\app-debug.apk

echo.
echo APK dosyası kontrol ediliyor...
if exist "%APK_PATH%" (
    echo ✓ APK bulundu: %APK_PATH%
    echo.
    echo APK yükleniyor...
    adb install -r "%APK_PATH%"
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ✓ APK başarıyla yüklendi!
        echo.
        echo Uygulamayı başlat: adb shell am start -n com.burhan2855.borctakip/.MainActivity
        pause
    ) else (
        echo.
        echo ✗ APK yükleme başarısız oldu
        echo Kontrol et:
        echo - Emülatör çalışıyor mu?
        echo - USB hata ayıklaması etkin mi?
        pause
    )
) else (
    echo ✗ APK dosyası bulunamadı: %APK_PATH%
    echo Build klasörünü kontrol et
    pause
)
