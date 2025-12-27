# GitHub Actions Entegrasyonu Tamamlandı! 🚀

## Oluşturulan Dosyalar

```
.github/
├── workflows/
│   ├── android-build.yml          # Her push'ta debug build
│   ├── android-release.yml        # Tag'de release build
│   └── lint.yml                   # Kod kalitesi kontrolü
├── copilot-instructions.md        # Copilot talimatları
├── HIZLI_BASLANGIC.md            # 2 dakikalık kurulum
├── GITHUB_ACTIONS_SETUP.md       # Detaylı dokumentasyon
├── SECRETS_KURULUMU.md           # Secrets eklemek için
└── README.md                      # Bu dosya
```

## Hızlı Kurulum (3 Adım)

### 1. Repository'yi GitHub'a Push Et
```bash
git remote add origin https://github.com/YOUR_USERNAME/BorcTakip.git
git push -u origin main
git push -u origin develop
```

### 2. GitHub Secrets Ekle
**Settings → Secrets and variables → Actions** kısmında:
- `BORC_TAKIP_STORE_PASSWORD`
- `BORC_TAKIP_KEY_ALIAS`
- `BORC_TAKIP_KEY_PASSWORD`
- `SIGNING_KEY` (base64 encoded)

👉 Detaylı rehber: `.github/SECRETS_KURULUMU.md`

### 3. Test Et
```bash
git add .
git commit -m "GitHub Actions test"
git push origin develop
```

**GitHub → Actions sekmesine gidin ve yeşil ✅ bekleyin!**

---

## Neler Otomatik Çalışıyor?

| Dosya | Koşul | İşlem |
|-------|-------|-------|
| **android-build.yml** | `main` veya `develop`'e push | Debug APK build + Unit Test |
| **android-release.yml** | `v*` tag oluştur | Release APK + GitHub Release |
| **lint.yml** | Her push | Kod kalitesi kontrolü |

---

## Workflow Durumunu İzleme

1. Repository → **Actions** sekmesi
2. İş akışı adına tıklayın (android-build, lint vs)
3. Run detaylarını görmek için başarılı/başarısız iş akışına tıklayın

**Yeşil ✅** = Başarılı  
**Kırmızı ❌** = Başarısız (logları kontrol edin)

---

## Artifacts (Derlenmiş Dosyalar)

İş akışı başarılıysa, artifacts'ı download edebilirsiniz:

```
android-build.yml çıktıları:
├── app-debug (APK dosyası)
└── test-results (Test raporları)

lint.yml çıktıları:
└── lint-report (HTML rapor)

android-release.yml çıktıları:
└── GitHub Release (Otomatik oluşturulur)
```

---

## Release Yayınlama

```bash
# 1. Tag oluştur
git tag v1.0.0

# 2. Push et
git push origin v1.0.0

# 3. GitHub'da Release sekmesine bakın
# Otomatik olarak release-build.yml çalışacak ve APK eklenecek
```

---

## Documentasyon

- **Hızlı başlangıç** → `.github/HIZLI_BASLANGIC.md`
- **Secrets kurulumu** → `.github/SECRETS_KURULUMU.md`
- **Detaylı rehber** → `.github/GITHUB_ACTIONS_SETUP.md`

---

## Sık Sorulan Sorular

**S: Build neden başarısız oldu?**  
C: Logları kontrol edin (Actions → başarısız iş akışı → hata mesajı). Lokal'de `./gradlew :app:assembleDebug` çalıştırın.

**S: Secrets nerden eklerim?**  
C: Repository → Settings → Secrets and variables → Actions. `.github/SECRETS_KURULUMU.md` dosyasına bakın.

**S: Workflow'u nasıl test ederim?**  
C: `develop` branch'e commit push edin. Android-build.yml otomatik çalışacak.

**S: APK dosyasını nasıl download ederim?**  
C: Actions → başarılı iş akışı → Artifacts kısmında `app-debug` indirin.

---

## Troubleshooting

### "gradlew: permission denied"
Normal, GitHub Actions otomatik izin verir.

### "Keystore not found"
- Secrets adlarını kontrol edin (case-sensitive!)
- SIGNING_KEY base64 formatında mı?

### "Test başarısız"
```bash
./gradlew :app:testDebugUnitTest
```

### "Build başarısız"
```bash
./gradlew clean :app:assembleDebug
```

---

## İleri Seviye

Workflow'ları özelleştirmek için `.github/workflows/` dosyalarını düzenleyin:

- **JDK sürümünü değiştir**: `java-version: '11'` → `'17'` vs
- **Branch'leri değiştir**: `main, develop` → `main, staging` vs
- **Test ekle**: `android-build.yml`'ye instrumented test adımı ekle

---

## Kontrol Listesi

- [ ] Repository GitHub'da
- [ ] `main` ve `develop` branch'leri push ettim
- [ ] Secrets ekledim
- [ ] İlk commit'i push ettim
- [ ] Actions sekmesinde yeşil check gördüm
- [ ] Artifacts'ı download edebildim

---

**🎉 Başarılı! GitHub Actions kurulumu tamamlandı. Artık her commit'te otomatik build, test ve lint çalışacak!**

📧 Sorular veya sorunlar için GitHub Issues açın.
