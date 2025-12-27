@echo off
REM Kasa/Banka Bakiye Sorunu Doğrulama Script'i

echo.
echo =========================================
echo   Kasa/Banka Bakiye Duzeltme Dogrulama
echo =========================================
echo.

REM MainViewModel.kt kontrolü
echo [1/3] MainViewModel.kt kontrol ediliyor...
findstr /I "it.category == \"Kasa Girissi\"" app\src\main\java\com\burhan2855\borctakip\ui\MainViewModel.kt >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo OK MainViewModel.kt - DUZELTILDI
) else (
    echo HATA MainViewModel.kt - DUZELTILMEDI ^(Elle duzeltme yapilmali^)
)
echo.

REM DebtTrackerApp.kt kontrolü
echo [2/3] DebtTrackerApp.kt kontrol ediliyor...
findstr /I "it.category == \"Kasa Girissi\"" app\src\main\java\com\burhan2855\borctakip\ui\DebtTrackerApp.kt >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo OK DebtTrackerApp.kt - DUZELTILDI
) else (
    echo HATA DebtTrackerApp.kt - DUZELTILMEDI ^(Elle duzeltme yapilmali^)
)
echo.

REM TransactionRepository.kt - applyPartialPayment kaldırıldı mı kontrolü
echo [3/3] TransactionRepository.kt kontrol ediliyor...
findstr /I "suspend fun applyPartialPayment" app\src\main\java\com\burhan2855\borctakip\data\TransactionRepository.kt >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo OK TransactionRepository.kt - Eski applyPartialPayment kaldirildi
) else (
    echo HATA TransactionRepository.kt - Eski applyPartialPayment hala var
)
echo.

echo =========================================
echo   Dogrulama Tamamlandi
echo =========================================
echo.
pause
