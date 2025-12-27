#!/usr/bin/env pwsh
# Gemini API Setup Script for BorcTakip Android App
# PowerShell version with better error handling

[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

function Write-Header {
    param([string]$Text)
    Write-Host "============================================" -ForegroundColor Cyan
    Write-Host $Text -ForegroundColor Cyan -BackgroundColor DarkBlue
    Write-Host "============================================" -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Text)
    Write-Host "✓ $Text" -ForegroundColor Green
}

function Write-Error-Custom {
    param([string]$Text)
    Write-Host "✗ $Text" -ForegroundColor Red
}

Write-Header "Borç Takip - Gemini API Kurulum Yardımcısı"

# 1. Check if local.properties exists
$localPropsPath = ".\local.properties"

if (-not (Test-Path $localPropsPath)) {
    Write-Host "UYARI: local.properties dosyası bulunamadı!" -ForegroundColor Yellow
    Write-Host "Bir tane oluşturuluyor..." -ForegroundColor Yellow
    Write-Host ""
    
    # Find Android SDK path
    $sdkPath = $env:ANDROID_HOME
    if (-not $sdkPath) {
        $sdkPath = "$env:LOCALAPPDATA\Android\Sdk"
    }
    
    # Create local.properties
    $sdkPath = $sdkPath -replace '\\', '\\'
    @"
sdk.dir=$sdkPath
"@ | Out-File -FilePath $localPropsPath -Encoding UTF8
    
    Write-Success "Yapılandırıldı: $sdkPath"
    Write-Host ""
}

# 2. Get API Key from user
Write-Host "Google Cloud Console'den API Anahtarını yapıştırın:" -ForegroundColor Yellow
$apiKey = Read-Host "API Key"

if ([string]::IsNullOrWhiteSpace($apiKey)) {
    Write-Error-Custom "Boş API anahtarı girilemez!"
    exit 1
}

# 3. Update or add API Key to local.properties
$content = Get-Content $localPropsPath -Raw
if ($content -match "GEMINI_API_KEY") {
    $content = $content -replace 'GEMINI_API_KEY=.*', "GEMINI_API_KEY=$apiKey"
    Write-Success "API Key güncellendi"
} else {
    $content += "`nGEMINI_API_KEY=$apiKey"
    Write-Success "API Key eklendi"
}

$content | Out-File -FilePath $localPropsPath -Encoding UTF8

# 4. Verify SHA-1 fingerprint
Write-Host ""
Write-Host "SHA-1 Parmak İzini kontrol et:" -ForegroundColor Yellow
$keystorePath = "$env:USERPROFILE\.android\debug.keystore"

if (Test-Path $keystorePath) {
    try {
        $keytoolOutput = & keytool -list -v -keystore $keystorePath `
            -alias androiddebugkey -storepass android -keypass android
        
        $sha1Line = $keytoolOutput | Select-String "SHA1"
        if ($sha1Line) {
            Write-Host $sha1Line -ForegroundColor Cyan
            Write-Host ""
            Write-Host "Bu SHA-1 parmak izini Google Cloud Console'da kayıtlı olduğundan emin ol!" -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "Keytool bulunamadı veya hata oluştu" -ForegroundColor Yellow
    }
}

# 5. Set JAVA_HOME if not set
if (-not $env:JAVA_HOME) {
    $jdkPaths = @(
        "C:\Program Files\Java\jdk-21",
        "C:\Program Files\Java\jdk-17",
        "C:\Program Files\Java\jdk-11"
    )
    
    foreach ($path in $jdkPaths) {
        if (Test-Path $path) {
            $env:JAVA_HOME = $path
            Write-Success "JAVA_HOME ayarlandı: $path"
            break
        }
    }
}

# 6. Build
Write-Host ""
Write-Header "Derleme yapılıyor..."
Write-Host ""

try {
    if ($IsWindows -or $PSVersionTable.Platform -eq "Win32NT") {
        & .\gradlew.bat :app:assembleDebug
    } else {
        & ./gradlew :app:assembleDebug
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Header "✓ Derleme Başarılı"
        Write-Success "APK dosyası oluşturuldu: app\build\outputs\apk\debug\app-debug.apk"
    } else {
        Write-Host ""
        Write-Header "✗ Derleme Başarısız"
        Write-Error-Custom "Lütfen hatayı kontrol et ve GEMINI_API_SISTEM_REHBERI.md dosyasını oku"
        exit 1
    }
}
catch {
    Write-Host ""
    Write-Header "✗ Derleme Başarısız"
    Write-Error-Custom "Hata: $_"
    exit 1
}

# 7. Final instructions
Write-Host ""
Write-Header "✓ Kurulum Tamamlandı"
Write-Host ""
Write-Host "Sonraki Adımlar:" -ForegroundColor Yellow
Write-Host "  1. Android Studio'da projeyi aç" -ForegroundColor White
Write-Host "  2. GeminiAIScreen.kt dosyasını inceleyerek entegrasyonu gör" -ForegroundColor White
Write-Host "  3. Uygulamayı cihazda test et" -ForegroundColor White
Write-Host ""
Write-Host "Sorun yaşarsan:" -ForegroundColor Yellow
Write-Host "  • GEMINI_API_SISTEM_REHBERI.md dosyasını oku" -ForegroundColor White
Write-Host "  • Google Cloud Console'da API ayarlarını kontrol et" -ForegroundColor White
Write-Host "  • local.properties'deki API anahtarını doğrula" -ForegroundColor White
Write-Host ""
Write-Host "Daha fazla yardım için: https://ai.google.dev/tutorials/kotlin" -ForegroundColor Cyan
Write-Host ""
Read-Host "Çıkmak için Enter'a bas"
