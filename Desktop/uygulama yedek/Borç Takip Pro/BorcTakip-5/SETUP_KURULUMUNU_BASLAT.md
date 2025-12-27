# 🚀 GitHub Actions Kurulumunu Başlatın

**Durum:** ✅ Otomatik setup script'leri hazır!

---

## 🎯 Hızlı Başlangıç

### Windows PowerShell'de (Tavsiye edilir)

```powershell
# PowerShell'i açın (Admin değilse de çalışır)
# Proje root klasöründe:

.\setup-keystore.ps1
```

**Not:** PowerShell'de execution policy hatası alırsanız:
```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\setup-keystore.ps1
```

### Windows CMD'de

```cmd
# CMD'i açın
# Proje root klasöründe:

setup-keystore.bat
```

---

## 📋 Script Ne Yapacak?

### Adım 1: Keystore Kontrol
- release-key.keystore var mı kontrol eder
- Eğer yoksa:
  - **A)** Yeni keystore oluşturur (sizin sertifika bilgilerinizle)
  - **B)** Mevcut keystore'u kopyalamanızı söyler

### Adım 2: Şifre Girişi
Kullanıcı interaktif olarak şunları girer:
- Keystore Şifresi (güvenli input - görünmez)
- Key Alias (varsayılan: release-key)
- Key Şifresi (güvenli input - görünmez)

### Adım 3: local.properties Oluştur
Script otomatik olarak local.properties'i doldurur:
```properties
BORC_TAKIP_STORE_FILE=release-key.keystore
BORC_TAKIP_STORE_PASSWORD=**gizli**
BORC_TAKIP_KEY_ALIAS=release-key
BORC_TAKIP_KEY_PASSWORD=**gizli**
```

### Adım 4: Build Test
```bash
./gradlew :app:assembleDebug
```

Başarılıysa devam et, başarısızsa düzelt ve tekrar çalıştır.

### Adım 5: Base64 SIGNING_KEY Oluştur
- Base64 string otomatik oluşturulur
- Clipboard'a kopyalanır
- Hazırlanıp GitHub'a paste etmek için

### Adım 6: GitHub Secrets Talimatları
Script otomatik olarak GitHub'a eklenecek Secret'leri gösterir:
- BORC_TAKIP_STORE_PASSWORD
- BORC_TAKIP_KEY_ALIAS
- BORC_TAKIP_KEY_PASSWORD
- SIGNING_KEY

---

## 🔐 Güvenlik

✅ **Şifreler güvenli şekilde giriliyor:**
- PowerShell'de `AsSecureString` ile gizli input
- Hiçbir yere yazılmıyor (sadece dosyaya)
- local.properties .gitignore'da gizli

✅ **Keystore dosyası korunuyor:**
- .gitignore'da `*.keystore` gizli
- Hiçbir yere commit edilmiyor
- Lokal makinede saklı

---

## 🛠️ Eğer Sorun Yaşarsanız

### PowerShell Execution Policy
```powershell
# Tek seferlik geçiş için:
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\setup-keystore.ps1

# Kalıcı olarak (Admin gerekli):
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Keytool Bulunamadı
```
Çözüm: Java JDK kurulu olduğundan emin olun
Windows PATH'inde JDK\bin olmalı
```

### Build Başarısız
```bash
# Clean build yap:
./gradlew clean :app:assembleDebug
```

### Base64 String Oluşturulamadı
```powershell
# Manual olarak oluştur:
$base64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes("release-key.keystore"))
$base64 | Set-Clipboard
```

---

## 📊 Script Çıktısı Örneği

```
================================================================================
 BorcTakip Release Signing Kurulumu
================================================================================

Bu script sihayla keystore kurulumunu yapacaksınız.
Kendi şifrelerinizi güvenli şekilde gireceksiniz.

[1/5] Keystore Dosyası Kontrol Ediliyor...
ℹ️ release-key.keystore bulunamadı!

Seçenekler:
  A) Yeni keystore oluştur
  B) Mevcut keystore'u kopyala

Seçiminiz (A/B): A

[...keystore oluşturma...]

✅ Keystore başarıyla oluşturuldu!

[2/5] local.properties Dosyası Oluşturuluyor...

Keystore Şifresi: ****
Key Alias [release-key]: release-key
Key Şifresi: ****

✅ local.properties güncellenmiştir

[3/5] Lokal Build Test Ediliyor...

Komut: ./gradlew :app:assembleDebug

[...build çalışıyor...]

BUILD SUCCESSFUL in 6s
✅ Build başarılı!

[4/5] Base64 SIGNING_KEY Oluşturuluyor...
✅ Base64 SIGNING_KEY oluşturuldu!
✅ Clipboard'a kopyalandı!

[5/5] GitHub Setup Talimatları

GitHub'da şu 4 Secret'i eklemeli siniz:

1. BORC_TAKIP_STORE_PASSWORD
   Value: *** (girildi)

2. BORC_TAKIP_KEY_ALIAS
   Value: release-key

3. BORC_TAKIP_KEY_PASSWORD
   Value: *** (girildi)

4. SIGNING_KEY
   Value: (Clipboard'dan yapıştır - otomatik kopyalandı)

================================================================================
 KURULUM TAMAMLANDI!
================================================================================

✅ Keystore oluşturuldu: release-key.keystore
✅ local.properties güncellenmiştir
✅ Build test başarılı
✅ GitHub Secrets talimatları gösterildi

SONRA YAPACAK:
  1. GitHub'da 4 Secret ekleyin
     👉 https://github.com/burhan2855/borctakip/settings/secrets/actions

  2. İlk test commit'i yapın:
     git push origin develop

  3. GitHub Actions'ta çalışmaları izleyin:
     👉 https://github.com/burhan2855/borctakip/actions
```

---

## ✅ Sonra Ne Yapacak?

### 1. GitHub Secrets Ekleyin (5 dakika)
```
https://github.com/burhan2855/borctakip/settings/secrets/actions
```

Script'in verdiği 4 Secret'i ekleyin.

### 2. İlk Test Commit (1 dakika)
```bash
git push origin develop
```

### 3. GitHub Actions'ı İzleyin (5 dakika)
```
https://github.com/burhan2855/borctakip/actions
```

Green check görmeli misiniz! ✅

---

## 📁 Oluşturulan Dosyalar

Script çalıştıktan sonra:
- ✅ `release-key.keystore` (yeni veya mevcut)
- ✅ `local.properties` (güncellenmiş)
- ✅ `.gitignore` (*.keystore gizli)

---

## 🔗 Linkler

- **Setup Script (PowerShell):** `setup-keystore.ps1`
- **Setup Script (CMD):** `setup-keystore.bat`
- **GitHub Secrets:** https://github.com/burhan2855/borctakip/settings/secrets/actions
- **GitHub Actions:** https://github.com/burhan2855/borctakip/actions

---

**Şimdi setup script'ini çalıştırın! 🚀**

```powershell
.\setup-keystore.ps1
```

veya

```cmd
setup-keystore.bat
```
