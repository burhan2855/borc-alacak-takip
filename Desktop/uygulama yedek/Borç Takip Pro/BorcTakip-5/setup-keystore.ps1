#!/usr/bin/env powershell
# BorcTakip Release Signing Kurulumu
# Kullanıcıların kendi şifrelerini girmesi için interactive script

param(
    [switch]$SkipBuild = $false
)

function Write-Title {
    param([string]$Text)
    Write-Host ""
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host " $Text" -ForegroundColor Cyan
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host ""
}

function Write-Success {
    param([string]$Text)
    Write-Host "✅ $Text" -ForegroundColor Green
}

function Write-Error {
    param([string]$Text)
    Write-Host "❌ $Text" -ForegroundColor Red
}

function Write-Info {
    param([string]$Text)
    Write-Host "ℹ️ $Text" -ForegroundColor Yellow
}

function Write-Step {
    param([int]$Number, [string]$Text)
    Write-Host "[$Number/5] $Text" -ForegroundColor Cyan
}

# Başlık
Clear-Host
Write-Title "BorcTakip Release Signing Kurulumu"

Write-Host "Bu script sihayla keystore kurulumunu yapacaksınız."
Write-Host "Kendi şifrelerinizi güvenli şekilde gireceksiniz."
Write-Host ""

# 1. Keystore Kontrol
Write-Step 1 "Keystore Dosyası Kontrol Ediliyor..."

$keystorePath = "release-key.keystore"
$keystoreExists = Test-Path $keystorePath

if ($keystoreExists) {
    Write-Success "release-key.keystore bulundu"
    $fileSize = (Get-Item $keystorePath).Length
    Write-Host "  Dosya Boyutu: $($fileSize / 1KB) KB"
} else {
    Write-Error "release-key.keystore bulunamadı!"
    Write-Host ""
    Write-Host "Seçenekler:"
    Write-Host "  A) Yeni keystore oluştur"
    Write-Host "  B) Mevcut keystore'u kopyala"
    Write-Host ""
    
    $choice = Read-Host "Seçiminiz (A/B)"
    
    if ($choice -eq "A" -or $choice -eq "a") {
        Write-Host ""
        Write-Host "Yeni keystore oluşturuluyor..." -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Keystore bilgilerini giriniz (Enter'a basarak varsayılanları kabul edebilirsiniz):"
        Write-Host ""
        
        $keyCN = Read-Host "Adınız (Common Name) [Burhan]"
        if ([string]::IsNullOrEmpty($keyCN)) { $keyCN = "Burhan" }
        
        $keyOU = Read-Host "Organizasyon Birimi [BorcTakip]"
        if ([string]::IsNullOrEmpty($keyOU)) { $keyOU = "BorcTakip" }
        
        $keyO = Read-Host "Organizasyon Adı [BorcTakip]"
        if ([string]::IsNullOrEmpty($keyO)) { $keyO = "BorcTakip" }
        
        $keyL = Read-Host "Şehir [Turkey]"
        if ([string]::IsNullOrEmpty($keyL)) { $keyL = "Turkey" }
        
        $keyST = Read-Host "Bölge [Turkey]"
        if ([string]::IsNullOrEmpty($keyST)) { $keyST = "Turkey" }
        
        $keyC = Read-Host "Ülke Kodu [TR]"
        if ([string]::IsNullOrEmpty($keyC)) { $keyC = "TR" }
        
        Write-Host ""
        $keystorePass = Read-Host "Keystore Şifresi (minimum 6 karakter)" -AsSecureString
        if ([string]::IsNullOrEmpty($keystorePass)) {
            Write-Error "Şifre boş olamaz!"
            exit 1
        }
        $keystorePassPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToCoTaskMemUnicode($keystorePass))
        
        $keyAlias = Read-Host "Key Alias [release-key]"
        if ([string]::IsNullOrEmpty($keyAlias)) { $keyAlias = "release-key" }
        
        $keyPass = Read-Host "Key Şifresi (Keystore şifresi ile aynı olabilir)" -AsSecureString
        if ([string]::IsNullOrEmpty($keyPass)) { $keyPass = $keystorePass }
        $keyPassPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToCoTaskMemUnicode($keyPass))
        
        # Keystore oluştur
        Write-Host ""
        Write-Host "Keystore oluşturuluyor (RSA 2048, 10000 gün geçerli)..." -ForegroundColor Cyan
        
        $dnameStr = "CN=$keyCN, OU=$keyOU, O=$keyO, L=$keyL, ST=$keyST, C=$keyC"
        
        keytool -genkeypair `
            -alias $keyAlias `
            -keyalg RSA `
            -keysize 2048 `
            -keystore $keystorePath `
            -validity 10000 `
            -keypass $keyPassPlain `
            -storepass $keystorePassPlain `
            -dname $dnameStr 2>&1 | Out-Null
        
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Keystore başarıyla oluşturuldu!"
        } else {
            Write-Error "Keystore oluşturma başarısız!"
            exit 1
        }
    } elseif ($choice -eq "B" -or $choice -eq "b") {
        Write-Host ""
        Write-Host "Lütfen mevcut keystore dosyasını proje root'una kopyalayın."
        Write-Host "Dosya adı: release-key.keystore"
        Write-Host ""
        Read-Host "İşlem tamamlandığında Enter'a basın"
        Write-Host ""
        
        if (-not (Test-Path $keystorePath)) {
            Write-Error "Keystore dosyası hala bulunamadı!"
            exit 1
        }
        Write-Success "Keystore dosyası bulundu!"
    } else {
        Write-Error "Geçersiz seçim!"
        exit 1
    }
}

# 2. local.properties Oluştur
Write-Step 2 "local.properties Dosyası Oluşturuluyor..."

Write-Host ""
Write-Host "Keystore bilgilerini giriniz:" -ForegroundColor Cyan
Write-Host ""

$keyAlias = Read-Host "Key Alias [release-key]"
if ([string]::IsNullOrEmpty($keyAlias)) { $keyAlias = "release-key" }

$keystorePass = Read-Host "Keystore Şifresi" -AsSecureString
if ([string]::IsNullOrEmpty($keystorePass)) {
    Write-Error "Şifre boş olamaz!"
    exit 1
}
$keystorePassPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToCoTaskMemUnicode($keystorePass))

$keyPass = Read-Host "Key Şifresi (Keystore şifresi ile aynı olabilir)" -AsSecureString
if ([string]::IsNullOrEmpty($keyPass)) { $keyPass = $keystorePass }
$keyPassPlain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToCoTaskMemUnicode($keyPass))

$sdkDir = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"

$localPropertiesContent = @"
## This file is automatically generated by Android Studio.
# Do not modify this file -- YOUR CHANGES WILL BE ERASED!
#
# This file should *NOT* be checked into Version Control Systems,
# as it contains information specific to your local configuration.
#
sdk.dir=$sdkDir

# Gemini API Key
GEMINI_API_KEY=AIzaSyAUzi7qz-V1dwomDaVWMO9gNGF4fQng4oM

# Release Signing Configuration
BORC_TAKIP_STORE_FILE=release-key.keystore
BORC_TAKIP_STORE_PASSWORD=$keystorePassPlain
BORC_TAKIP_KEY_ALIAS=$keyAlias
BORC_TAKIP_KEY_PASSWORD=$keyPassPlain
"@

$localPropertiesContent | Out-File -FilePath "local.properties" -Encoding UTF8

Write-Success "local.properties güncellenmiştir"

# 3. Build Test
if (-not $SkipBuild) {
    Write-Step 3 "Lokal Build Test Ediliyor..."
    Write-Host ""
    Write-Host "Komut: ./gradlew :app:assembleDebug" -ForegroundColor Yellow
    Write-Host ""
    
    & .\gradlew.bat :app:assembleDebug
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Build başarısız oldu!"
        Write-Host "Hataları kontrol edin ve tekrar deneyin."
        Read-Host "Enter'a basın"
        exit 1
    }
    Write-Success "Build başarılı!"
} else {
    Write-Step 3 "Build testi atlanıyor..."
}

# 4. Base64 SIGNING_KEY Oluştur
Write-Step 4 "Base64 SIGNING_KEY Oluşturuluyor..."

Write-Host ""
Write-Host "Base64 string oluşturuluyor..." -ForegroundColor Cyan

try {
    $keystoreBytes = [System.IO.File]::ReadAllBytes((Get-Location).Path + "\release-key.keystore")
    $base64String = [System.Convert]::ToBase64String($keystoreBytes)
    
    # Clipboard'a kopyala
    $base64String | Set-Clipboard
    
    Write-Success "Base64 SIGNING_KEY oluşturuldu!"
    Write-Host "✅ Clipboard'a kopyalandı!"
    Write-Host ""
    Write-Host "Base64 String (ilk 50 karakter):"
    Write-Host $base64String.Substring(0, [Math]::Min(50, $base64String.Length)) -ForegroundColor Gray
    Write-Host "..."
    Write-Host ""
    Write-Host "  Toplam uzunluk: $($base64String.Length) karakter"
} catch {
    Write-Error "Base64 oluşturma başarısız!"
    Write-Host $_.Exception.Message
}

# 5. GitHub Talimatları
Write-Step 5 "GitHub Setup Talimatları"

Write-Host ""
Write-Host "GitHub'da şu 4 Secret'i eklemeli siniz:" -ForegroundColor Cyan
Write-Host ""

Write-Host "1. BORC_TAKIP_STORE_PASSWORD" -ForegroundColor Yellow
Write-Host "   Value: *** (girildi)" -ForegroundColor Gray
Write-Host ""

Write-Host "2. BORC_TAKIP_KEY_ALIAS" -ForegroundColor Yellow
Write-Host "   Value: $keyAlias" -ForegroundColor Gray
Write-Host ""

Write-Host "3. BORC_TAKIP_KEY_PASSWORD" -ForegroundColor Yellow
Write-Host "   Value: *** (girildi)" -ForegroundColor Gray
Write-Host ""

Write-Host "4. SIGNING_KEY" -ForegroundColor Yellow
Write-Host "   Value: (Clipboard'dan yapıştır - otomatik kopyalandı)" -ForegroundColor Gray
Write-Host ""

Write-Title "KURULUM TAMAMLANDI!"

Write-Success "Keystore oluşturuldu: release-key.keystore"
Write-Success "local.properties güncellenmiştir"
Write-Success "Build test başarılı"
Write-Success "GitHub Secrets talimatları gösterildi"

Write-Host ""
Write-Host "SONRA YAPACAK:" -ForegroundColor Cyan
Write-Host "  1. GitHub'da 4 Secret ekleyin"
Write-Host "     👉 https://github.com/burhan2855/borctakip/settings/secrets/actions"
Write-Host ""
Write-Host "  2. İlk test commit'i yapın:"
Write-Host "     git push origin develop"
Write-Host ""
Write-Host "  3. GitHub Actions'ta çalışmaları izleyin:"
Write-Host "     👉 https://github.com/burhan2855/borctakip/actions"
Write-Host ""

Read-Host "Tamamlamak için Enter'a basın"
