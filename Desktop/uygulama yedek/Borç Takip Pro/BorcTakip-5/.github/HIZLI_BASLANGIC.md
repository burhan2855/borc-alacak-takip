# GitHub Actions - Hızlı Başlangıç (2 Dakika)

## Adım 1: Repository'yi GitHub'a Push Edin

```bash
git remote add origin https://github.com/YOU/BorcTakip.git
git push -u origin main
git push -u origin develop
```

## Adım 2: Secrets Ekleyin

GitHub repository'nize gidin:
1. **Settings** → **Secrets and variables** → **Actions** 
2. **New repository secret** butonuna tıklayın

Aşağıdaki Secrets'i ekleyin:

| Secret Adı | Açıklama |
|-----------|----------|
| `BORC_TAKIP_STORE_PASSWORD` | Keystore şifresi |
| `BORC_TAKIP_KEY_ALIAS` | Key alias adı |
| `BORC_TAKIP_KEY_PASSWORD` | Key şifresi |
| `SIGNING_KEY` | Base64 kodlanmış keystore (opsiyonel, sadece release için) |

## Adım 3: Workflow Dosyalarını Kontrol Edin

Proje klasöründe `.github/workflows/` altında 3 dosya olmalı:
- ✅ `android-build.yml`
- ✅ `android-release.yml`
- ✅ `lint.yml`

## Adım 4: Test Edin

```bash
# 1. develop branch'e kod ekle
git add .
git commit -m "Test mesajı"
git push origin develop

# 2. GitHub Actions sekmesini açın
# https://github.com/YOU/BorcTakip/actions
```

**Yeşil ✅ işareti görmeli misiniz?** 
- Evet: Başarılı! 🎉

**Kırmızı ❌ görmüş müyüz?**
- Build başarısız olmuşsa logları kontrol edin:
  - Actions sekmesinde iş akışını tıklayın
  - Hata mesajı okuyun
  - Lokal'de aynı komutu çalıştırın

## Adım 5: Release Yayınlayın (Opsiyonel)

```bash
# 1. Tag oluştur
git tag v1.0.0

# 2. Push et
git push origin v1.0.0

# 3. GitHub'da Release sekmesine bakın
```

---

## En Yaygın Hatalar

| Hata | Çözüm |
|------|------|
| `./gradlew: permission denied` | Normal, GitHub Actions otomatik izin verir |
| `Keystore not found` | Secrets adlarını kontrol edin |
| `Build failed` | `./gradlew clean :app:assembleDebug` lokal'de çalıştırın |
| `Test failed` | `./gradlew :app:testDebugUnitTest` lokal'de çalıştırın |

---

**Daha detaylı bilgi için:** `.github/GITHUB_ACTIONS_SETUP.md` dosyasını okuyun.
