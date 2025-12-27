# Gemini API Hata Çözümleme Rehberi

## 📋 İçindekiler
1. [Yaygın Hatalar ve Çözümleri](#yaygın-hatalar-ve-çözümleri)
2. [Adım Adım Kontrol Listesi](#adım-adım-kontrol-listesi)
3. [Logcat Analizi](#logcat-analizi)
4. [Güvenlik Kontrolleri](#güvenlik-kontrolleri)

---

## 🔴 Yaygın Hatalar ve Çözümleri

### Hata 1: "Requests from this Android client application <empty> are blocked"

```
Hata Mesajı:
Hata: Requests from this Android client application <empty> are blocked.
```

**Neden oluşur?**
- Android cihazın SHA-1 parmak izi Google Cloud Console'da kaydedilmemiş
- Uygulama paket adı doğru değil
- API anahtarında Android kısıtlaması ayarlanmamış

**✅ Çözüm Adımları:**

1. **Debug Keystore'un SHA-1 Parmak İzini Al**
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" `
  -alias androiddebugkey `
  -storepass android `
  -keypass android | findstr "SHA1"

# Çıktı örneği:
# SHA1: 6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C
```

2. **Google Cloud Console'a Git**
   - https://console.cloud.google.com
   - "API'ler ve Hizmetler" → "Kimlik Bilgileri"
   - Oluşturduğun API Anahtarını bul
   - "Düzenle" butonunu tıkla

3. **Android Kısıtlaması Ayarla**
   ```
   ✅ Uygulama Kısıtlamaları: Android uygulamaları
   ✅ Paket adı: com.burhan2855.borctakip
   ✅ SHA-1 parmak izi: 6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C
   ```

4. **Kaydet ve 10 dakika bekle** (API değişiklikleri zaman alabilir)

5. **Uygulamayı Yeniden Derle ve Test Et**
```bash
./gradlew clean
./gradlew :app:assembleDebug
```

---

### Hata 2: "Model bulunamadı: gemini-1.5-flash"

```
Hata Mesajı:
com.google.ai.client.generativeai.type.ServerException: 
Error Code: 404
Message: models/gemini-1.5-flash is not found for API version v1beta, 
or this model api is not supported by this service.
```

**Neden oluşur?**
- Model adı yanlış veya deprecate edilmiş
- API versiyonu desteklenmiyor
- Modele erişim izni yok

**✅ Çözüm:**

Desteklenen modelleri kullan:
```kotlin
// ✅ Tercih sırasına göre:
val model = GenerativeModel(
    modelName = "gemini-2.0-flash",      // En yeni (önerilir)
    apiKey = BuildConfig.GEMINI_API_KEY
)

// Alternatif modeller:
// "gemini-1.5-pro"
// "gemini-1.5-flash-latest"
// "gemini-1.5-pro-latest"
```

---

### Hata 3: "API keys are not supported by this API"

```
Hata Mesajı:
Hata: API keys are not supported by this API. 
Expected OAuth2 access token or other authentication credentials 
that assert a principal. See https://cloud.google.com/docs/authentication
```

**Neden oluşur?**
- Yanlış API seçildi (Vertex AI yerine Generative Language API olmalı)
- API etkinleştirilmemiş
- Eski SDK versiyonu kullanılıyor

**✅ Çözüm:**

1. **Google Cloud Console'da Etkinleştir**
   - https://console.cloud.google.com
   - "API'ler ve Hizmetler" → "Hizmetleri Etkinleştir"
   - "Generative Language API" ara
   - "Etkinleştir" butonunu tıkla

2. **Doğru API'yi Seç**
   ```
   ✅ Generative Language API (API_CLIENT 'clients')
   ❌ Vertex AI API (yanlış)
   ❌ Vision API (yanlış)
   ```

3. **SDK'yı Güncelle**
   ```kotlin
   // build.gradle.kts
   implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
   ```

---

### Hata 4: "Model bulunamadı (gemini-1.5-flash)"

```
Hata Mesajı:
Unexpected Response:
{
  "error": {
    "code": 404,
    "message": "models/gemini-1.5-flash is not found for API version v1beta"
  }
}
```

**Neden oluşur?**
- API versiyonu uyumsuz
- Model henüz yayımlanmamış bölgede

**✅ Çözüm:**

GeminiService.kt'de model güncelle:
```kotlin
fun initialize() {
    if (generativeModel == null) {
        generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",  // ✅ Bunu kullan
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }
}
```

---

### Hata 5: "Build Config'te GEMINI_API_KEY boş"

```
Hata Mesajı:
java.lang.IllegalArgumentException: API key cannot be empty
```

**Neden oluşur?**
- local.properties'de GEMINI_API_KEY tanımlanmamış
- Build cache problemi
- Yanlış dosya yolu

**✅ Çözüm:**

1. **local.properties'yi Kontrol Et**
   ```ini
   # Proje kökündeki local.properties dosyası
   sdk.dir=C:\\Users\\burha\\AppData\\Local\\Android\\Sdk
   GEMINI_API_KEY=AIzaSy[KENDİ_API_KEYIN]
   ```

2. **Build Cache'i Temizle**
   ```powershell
   ./gradlew clean
   # Veya
   rm -r app/build
   ```

3. **Yeniden Derle**
   ```powershell
   ./gradlew :app:assembleDebug
   ```

---

## 📋 Adım Adım Kontrol Listesi

### Google Cloud Console Kontrolleri

- [ ] Google Cloud projesi oluşturdum
- [ ] Faturalandırma hesabını etkinleştirdim
- [ ] "Generative Language API" etkinleştirdim
  - https://console.cloud.google.com/apis/library/generativelanguage.googleapis.com
- [ ] API anahtarı oluşturdum
- [ ] API anahtarında **Android uygulamaları** kısıtlaması seçili
- [ ] Paket adı doğru: `com.burhan2855.borctakip`
- [ ] SHA-1 parmak izi doğru ve tam girildi

### Android Studio Kontrolleri

- [ ] `local.properties` dosyası proje kökünde
- [ ] `GEMINI_API_KEY=AIzaSy...` satırı var
- [ ] `build.gradle.kts`'de `buildConfigField` tanımlanmış
- [ ] `buildFeatures { buildConfig = true }` var
- [ ] `com.google.ai.client.generativeai:generativeai` dependency var
- [ ] AndroidManifest.xml'de `INTERNET` permission var

### Kod Kontrolleri

- [ ] `GeminiService.kt` dosyası oluşturuldu
- [ ] Model adı: `gemini-2.0-flash`
- [ ] API Key: `BuildConfig.GEMINI_API_KEY` (local değişken değil)
- [ ] `initialize()` fonksiyonu adında çağrılıyor
- [ ] Error handling var (try-catch)

### Derleme Kontrolleri

- [ ] `./gradlew clean` çalıştırdım
- [ ] `./gradlew :app:assembleDebug` başarıyla tamamlandı
- [ ] APK oluşturuldu: `app/build/outputs/apk/debug/app-debug.apk`
- [ ] Hiçbir warning yok

### Runtime Kontrolleri

- [ ] Uygulama cihazda yüklüyorum
- [ ] İnternet bağlantısı açık
- [ ] Logcat'i kontrol etme (aşağı bak)
- [ ] Yanıt alıyorum

---

## 🔍 Logcat Analizi

### Logcat'i Filtreleme

Android Studio'da:
1. **Logcat** sekmesini aç
2. **Filtre** alanında şunu yazı: `GEMINI|GenerativeAI|GENERATE`
3. Log seviyesini "Verbose" yap

### Başarılı İstek Örneği
```
D/GEMINI: Initializing GenerativeModel
D/GEMINI: Sending request to: generativelanguage.googleapis.com
D/GEMINI: Response received: 200 OK
D/GEMINI: Response: "Merhaba! Sana nasıl yardımcı olabilirim?"
```

### Hatalı İstek Örneği
```
E/GenerativeAI: Failed to generate content
E/GenerativeAI: Status code: 403
E/GenerativeAI: Error: Requests from this Android client application are blocked
```

### Network Hatasından Kaynaklanan
```
E/GenerativeAI: SocketTimeoutException
// Çözüm: İnternet bağlantısını kontrol et, API anahtarını doğrula
```

---

## 🔐 Güvenlik Kontrolleri

### ✅ Doğru Yapılandırma

```kotlin
// GeminiService.kt - DOĞRU
import com.burhan2855.borctakip.BuildConfig

object GeminiService {
    fun initialize() {
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = BuildConfig.GEMINI_API_KEY  // ✅ BuildConfig'ten
        )
    }
}
```

### ❌ Yanlış Yapılandırma

```kotlin
// YANLIŞ - Hardcoded key
val generativeModel = GenerativeModel(
    modelName = "gemini-2.0-flash",
    apiKey = "AIzaSy..."  // ❌ Asla hardcode etme!
)

// YANLIŞ - String resource
val key = context.getString(R.string.api_key)  // ❌ strings.xml'de saklamayın!
```

### ProGuard Kuralları

```ini
# app/proguard-rules.pro - KİM SAKLAMALI
-keep class com.google.ai.client.generativeai.** { *; }
-keep class com.burhan2855.borctakip.BuildConfig { *; }
-keep class com.burhan2855.borctakip.util.GeminiService { *; }
```

---

## 🧪 Test Komutları

### Build Testi
```bash
# Clean build
./gradlew clean :app:assembleDebug

# Manifest kontrol
./gradlew :app:validateSigningConfig

# Lint check
./gradlew :app:lintDebug
```

### Unit Test
```kotlin
// app/src/test/java/com/burhan2855/borctakip/util/GeminiServiceTest.kt
@Test
fun testGeminiInitialization() {
    GeminiService.initialize()
    // Service hazır olmalı
}
```

### Integration Test
```kotlin
// MainActivity'de
lifecycleScope.launch {
    val response = GeminiService.generateContent("Test: Merhaba!")
    Log.d("TEST", "Yanıt: $response")
    assertTrue(response.isNotEmpty())
}
```

---

## 💡 İpuçları ve Öneriler

### 1. API Key Rotation
Belirli aralıklarla API anahtarını değiştir:
```powershell
# Eski anahtarı devre dışı bırak
# Console → Kimlik Bilgileri → Eski Anahtarı sil

# Yeni anahtar oluştur
# local.properties'i güncelle
# Tekrar derle
```

### 2. Region-Based Access
Eğer bölge sınırlaması varsa:
```kotlin
// Türkiye bölgesi için
val generativeModel = GenerativeModel(
    modelName = "gemini-2.0-flash",
    apiKey = BuildConfig.GEMINI_API_KEY,
    // region: "europe-west1"  // İleriki versiyonlarda
)
```

### 3. Rate Limiting
```kotlin
private var lastRequestTime = 0L
fun askGemini(prompt: String) {
    val now = System.currentTimeMillis()
    if (now - lastRequestTime < 1000) {
        // Rate limit: 1 saniye bekleme
        return
    }
    lastRequestTime = now
    // ... API çağrısı
}
```

### 4. API Quote Kontrol
Google Cloud Console'da fatura kullanımını gözlemle:
- https://console.cloud.google.com/billing
- "Raporlar ve İstatistikler" → Kullanımı izle

---

## 📞 Daha Fazla Yardım

- **Google AI Documentation**: https://ai.google.dev/tutorials/kotlin
- **Google Cloud Console**: https://console.cloud.google.com
- **Generative Language API Docs**: https://ai.google.dev/docs
- **Community Support**: https://github.com/google-ai-sdk/issues

---

**Son Güncelleme:** 27 Aralık 2025 | Sürüm: 1.0
