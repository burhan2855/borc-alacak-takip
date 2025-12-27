# GitHub Secrets Kurulumu - Adım Adım Resimli Rehber

## Önemli ⚠️
Secrets asla commit etmeyin! Sadece GitHub UI üzerinden ekleyin.

## Adım 1: Repository Settings'e Gidin

```
GitHub.com → Your Repository → Settings (sağ üst)
```

## Adım 2: Secrets Menüsünü Açın

```
Settings → Secrets and variables (sol menü) → Actions
```

## Adım 3: Secret Ekleyin

**"New repository secret" butonuna tıklayın**

### Eklenmesi Gereken Secrets:

#### 1️⃣ BORC_TAKIP_STORE_PASSWORD
```
Name: BORC_TAKIP_STORE_PASSWORD
Secret: YourKeystorePassword123
```

#### 2️⃣ BORC_TAKIP_KEY_ALIAS
```
Name: BORC_TAKIP_KEY_ALIAS
Secret: release-key
```

#### 3️⃣ BORC_TAKIP_KEY_PASSWORD
```
Name: BORC_TAKIP_KEY_PASSWORD
Secret: YourKeyPassword123
```

#### 4️⃣ SIGNING_KEY (Release Build İçin)

**YAPACAK İŞ:**
1. `release-key.keystore` dosyanızı Base64'e kodlayın
2. GitHub Secrets'e ekleyin

**Windows PowerShell'de:**
```powershell
# Dosyayı Base64'e kodla
$base64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\release-key.keystore"))

# Clipboard'a kopyala
$base64 | Set-Clipboard

# Ya da direkt yazdır
Write-Host $base64
```

**Linux/Mac'te:**
```bash
# macOS
cat release-key.keystore | base64 | pbcopy

# Linux
cat release-key.keystore | base64 | xclip
```

**Oluşturulan base64 string'i GitHub'a ekleyin:**
```
Name: SIGNING_KEY
Secret: [Base64 stringi buraya yapıştır]
```

## Kurulum Tamamlandı! ✅

Artık iş akışlarınız çalışmaya hazır:

- **Debug Build**: `develop` veya `main`'e push yaptığında
- **Lint**: Her push'ta kod kalitesi kontrolü
- **Release Build**: `v1.0.0` gibi tag oluşturduğunda

## Verification

Secrets doğru kurulmuş mu kontrol etmek için:

1. `.github/workflows/` klasöründeki bir workflow'u açın
2. GitHub'da Actions sekmesine gidin
3. Son run'u tıklayın
4. Build aşamasının loglarına bakın

```
✅ "Building with Gradle" başarılıysa secrets doğru kurulmuş
❌ Hata alıyorsa secret adlarını veya değerlerini kontrol edin
```

---

## Hızlı Kontrol Listesi

- [ ] Repository Settings'de Secrets sekmesini buldum
- [ ] BORC_TAKIP_STORE_PASSWORD ekledim
- [ ] BORC_TAKIP_KEY_ALIAS ekledim
- [ ] BORC_TAKIP_KEY_PASSWORD ekledim
- [ ] SIGNING_KEY ekledim (release için)
- [ ] Workflow dosyaları `.github/workflows/` altında var
- [ ] İlk push'tan sonra Actions sekmesinde yeşil check görmüş müyüm

---

## Sorun Giderme

### Secret eklendikten sonra görünmüyor
- Sayfayı yenile (F5)
- Listenin sonuna scroll et

### "Secret not found" hatası
- Secret adını workflow dosyasında kontrol et
- `BORC_TAKIP_STORE_PASSWORD` vs `borc_takip_store_password` case-sensitive!

### "Keystore not found" hatası
- SIGNING_KEY'in Base64 formatını kontrol et
- Boş karakter olup olmadığını kontrol et

---

**Tüm secretler ekledikten sonra ilk commit'i push edin ve Actions sekmesinden sonucu kontrol edin!**
