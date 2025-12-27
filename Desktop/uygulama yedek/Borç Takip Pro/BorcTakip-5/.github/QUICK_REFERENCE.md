# GitHub Actions - Hızlı Referans Kartı 📋

## 🚀 Kurulum (Kopyala-Yapıştır)

### 1️⃣ Repository'yi GitHub'a Push Et
```bash
git remote add origin https://github.com/YOUR_USERNAME/BorcTakip.git
git push -u origin main
git push -u origin develop
```

### 2️⃣ PowerShell'de SIGNING_KEY Oluştur
```powershell
$base64 = [Convert]::ToBase64String([IO.File]::ReadAllBytes(".\release-key.keystore"))
$base64 | Set-Clipboard
Write-Host "Base64 string Clipboard'a kopyalandı!"
```

### 3️⃣ GitHub Secrets Ekle
```
Repository → Settings → Secrets and variables → Actions → New repository secret
```

| Secret Adı | Değer |
|-----------|-------|
| `BORC_TAKIP_STORE_PASSWORD` | Keystore şifresi |
| `BORC_TAKIP_KEY_ALIAS` | release-key (yada sizin key alias'ınız) |
| `BORC_TAKIP_KEY_PASSWORD` | Key şifresi |
| `SIGNING_KEY` | Base64 kodlanmış keystore (Clipboard'dan yapıştır) |

---

## 📊 Workflow Komutları

### Debug Build Tetikle
```bash
git add .
git commit -m "Mesaj"
git push origin develop  # Otomatik build başlar
```

### Release Build Tetikle
```bash
git tag v1.0.0
git push origin v1.0.0  # Otomatik release build başlar
```

### Lint Kontrolü Tetikle
```bash
git push origin develop  # Otomatik lint çalışır
```

---

## 🔍 İzleme

**GitHub Actions sekmesi:**
```
Repository → Actions → Workflow adı → Latest run
```

| Status | Anlamı |
|--------|--------|
| ✅ Green | Başarılı |
| ❌ Red | Başarısız |
| ⏳ Yellow | Çalışıyor |

---

## 📥 APK Download

```
Actions → Başarılı workflow → Artifacts → app-debug indir
```

---

## 🆘 Hata Çözümü

### Build başarısız?
```bash
./gradlew clean :app:assembleDebug
```

### Test başarısız?
```bash
./gradlew :app:testDebugUnitTest
```

### Secrets yanlış?
1. Secret adlarını kontrol et (case-sensitive!)
2. Secret değerlerinin boş olmadığını kontrol et
3. SIGNING_KEY base64 formatında mı kontrol et

---

## 📁 Dosya Yapısı

```
.github/
├── workflows/
│   ├── android-build.yml      ← Debug + Test
│   ├── android-release.yml    ← Release
│   └── lint.yml               ← Kalite kontrol
└── [README dosyaları]
```

---

## 🔗 Linkler

| Dokuman | Amaç |
|---------|------|
| `HIZLI_BASLANGIC.md` | 2 dakikalık başlangıç |
| `GITHUB_ACTIONS_SETUP.md` | Detaylı kurulum |
| `SECRETS_KURULUMU.md` | Secrets eklemek |
| `README_GITHUB_ACTIONS.md` | Genel bakış |

---

## ✅ Kontrol Listesi

- [ ] Repository GitHub'da ve push ettim
- [ ] Tüm 4 Secret'i ekledim
- [ ] İlk commit'i push ettim
- [ ] Actions sekmesinde yeşil check gördüm
- [ ] APK'yı artifacts'tan download edebildim

---

## 💡 İpuçları

1. **Branch stratejisi**: `feature/` → `develop` → `main` (release)
2. **Commit mesajları**: `[feature]`, `[fix]`, `[docs]` prefixleri kullan
3. **Tags**: Semantic versioning: `v1.0.0`, `v1.0.1`, `v2.0.0`
4. **Pull Requests**: Feature branch'inizde tüm checks yeşil olana kadar merge etmeyin

---

**📚 Daha fazla bilgi için `.github/` klasöründeki dokumanlara bakın!**
