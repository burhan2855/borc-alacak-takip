# GitHub Actions Kurulum Rehberi - BorçTakip Android App

## Genel Bakış

Bu rehber, BorçTakip uygulaması için GitHub Actions iş akışlarını nasıl kuracağınızı açıklar.

## Dosya Yapısı

```
.github/
├── workflows/
│   ├── android-build.yml      # Her push/PR'de debug build çalıştırır
│   ├── android-release.yml    # Tag oluşturduğunda release build yapar
│   └── lint.yml               # Kod kalitesi kontrolü
└── copilot-instructions.md    # Copilot talimatları
```

## Önceden Kurulum Adımları

### 1. Repository Settings'de Secrets Tanımlayın

GitHub repository'nizde `Settings` → `Secrets and variables` → `Actions` kısmına gidin.

**Release build için gereken Secrets:**

- `SIGNING_KEY`: Base64 ile kodlanmış `.keystore` dosyası
- `BORC_TAKIP_STORE_PASSWORD`: Keystore şifresi
- `BORC_TAKIP_KEY_ALIAS`: Key alias
- `BORC_TAKIP_KEY_PASSWORD`: Key şifresi

#### SIGNING_KEY nasıl hazırlanır:

Windows PowerShell'de:
```powershell
$base64String = [Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\release-key.keystore"))
$base64String | Set-Clipboard
```

Linux/Mac'te:
```bash
cat release-key.keystore | base64 | pbcopy  # macOS
cat release-key.keystore | base64 | xclip   # Linux
```

Oluşturulan base64 stringi GitHub Secrets'e `SIGNING_KEY` olarak ekleyin.

### 2. Repository Yapısını Doğrulayın

Aşağıdaki dosyalar var olmalıdır:
- `build.gradle.kts` (root)
- `app/build.gradle.kts`
- `gradlew` ve `gradlew.bat`
- `app/src/main/AndroidManifest.xml`

## İş Akışlarının Açıklaması

### android-build.yml (Debug Build)

**Tetikleme Koşulları:**
- `main` veya `develop` branch'ine yapılan her push
- `main` veya `develop` branch'ine açılan Pull Request

**Yapılan İşlemler:**
1. Repository'yi checkout eder
2. Java 11 JDK'yı kurar
3. Gradle izinlerini ayarlar
4. Debug APK'yı build eder (`./gradlew :app:assembleDebug`)
5. Unit testleri çalıştırır (`./gradlew :app:testDebugUnitTest`)
6. Başarılıysa APK'yı artifact olarak kaydeder
7. Test sonuçlarını artifact olarak kaydeder

**Artifacts:**
- `app-debug`: Derlenmiş debug APK
- `test-results`: Test raporları

### android-release.yml (Release Build)

**Tetikleme Koşulu:**
- `v*` formatında tag oluşturulması (örn: `v1.0.0`)

**Yapılan İşlemler:**
1. Repository'yi checkout eder
2. Java 11 JDK'yı kurar
3. Keystore dosyasını Base64'ten decode eder
4. Release APK'yı imzalı olarak build eder
5. GitHub Release oluşturur ve APK'yı yükler

**Kullanım:**
```bash
git tag v1.0.0
git push origin v1.0.0
```

### lint.yml (Kod Kalitesi)

**Tetikleme Koşulları:**
- `main` veya `develop` branch'ine yapılan her push
- `main` veya `develop` branch'ine açılan Pull Request

**Yapılan İşlemler:**
1. Repository'yi checkout eder
2. Java 11 JDK'yı kurar
3. Android Lint kontrolü çalıştırır
4. Raporu artifact olarak kaydeder

**Artifact:**
- `lint-report`: HTML lint raporu

## İş Akışı Durumunu İzleme

GitHub repository'nizde `Actions` sekmesine tıklayarak tüm iş akışlarını görebilirsiniz:

1. **Workflow adı** - Hangi iş akışı çalışıyor
2. **Tetikleyici** - Ne tetikledi (push, PR, tag)
3. **Commit/Branch** - Hangi commit'te çalışıyor
4. **Durum** - ✅ Başarılı, ❌ Başarısız
5. **Run details** - Detaylı log'lar için tıklayın

## Troubleshooting

### Build başarısız oluyorsa:

1. **"gradlew: permission denied"**
   - Dosya zaten değiştirilmiş, bu normal
   - GitHub Actions otomatik olarak izin verir

2. **"Keystore not found"**
   - `SIGNING_KEY` secret'i doğru base64 formatında mı kontrol edin
   - Secret adı tamamen doğru mı (`BORC_TAKIP_STORE_PASSWORD` vs)

3. **Test başarısız**
   - Lokal'de testleri çalıştırıp düzeltmeye çalışın:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```

4. **JDK versiyonu uyuşmazsa**
   - `app/build.gradle.kts` dosyasında `targetSdkVersion`, `compileSdkVersion` kontrol edin
   - Workflow'da JDK sürümünü güncelleyin (`java-version: '11'`)

## Best Practices

1. **Feature branchları**: Her yeni özellik için `develop`'den branch açın
2. **Pull Requestler**: Feature branch'iniz CI'ı geçene kadar merge etmeyin
3. **Tags**: Release'ler için semantic versioning kullanın (`v1.0.0`, `v1.0.1`)
4. **Secrets**: Asla şifrelerinizi commit etmeyin, GitHub Secrets kullanın

## Örnek Workflow

```
1. Feature branch oluştur
   git checkout -b feature/yeni-ozellık

2. Kodu yazıp commit et
   git add .
   git commit -m "Yeni özellik ekle"

3. Push et (GitHub Actions otomatik çalışır)
   git push origin feature/yeni-ozellık

4. GitHub'da Pull Request aç
   - CI checks otomatik çalışır
   - Tüm checks yeşil olana kadar bekle

5. Merge et ve delete branch

6. Release için tag oluştur
   git tag v1.0.0
   git push origin v1.0.0
   - Release build otomatik çalışır
   - GitHub Release oluşturulur
```

## İleri Seviye Özelleştirmeler

### Instrumented Tests Ekleme

`android-build.yml`'ye ekleyin:
```yaml
- name: Run Instrumented Tests
  run: ./gradlew :app:connectedDebugAndroidTest
```

### Firebase Deployment

```yaml
- name: Deploy to Firebase App Distribution
  uses: wzieba/Firebase-Distribution-Github-Action@v1
  with:
    firebase_token: ${{ secrets.FIREBASE_TOKEN }}
    file: app/build/outputs/apk/release/app-release.apk
```

### Notification Gönderme

Push, PR veya deployment başarısızlığında Slack/Discord'a bildirim göndermek için:
```yaml
- name: Notify Slack
  if: failure()
  uses: slackapi/slack-github-action@v1
  with:
    webhook-url: ${{ secrets.SLACK_WEBHOOK }}
```

---

**Sorularınız varsa GitHub Issues'de bir issue açın veya documentation'ı güncelleyin.**
