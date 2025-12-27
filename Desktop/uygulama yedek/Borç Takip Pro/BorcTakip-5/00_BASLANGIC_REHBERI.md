# 🎯 Android Studio API Anahtar Sistemi - BAŞLANGIÇ REHBERI

> ⚠️ **ÖNCE BU DOSYAYI OKU** - Diğer tüm rehberler buna dayanıyor!

## 📌 Sorunuz Neydi?

Gördüğünüz hatalar:
```
❌ "Requests from this Android client application <empty> are blocked"
❌ "Model bulunamadı (gemini-1.5-flash)"  
❌ "API keys are not supported by this API"
❌ "Expected OAuth2 access token"
```

**Hepsi AYNI nedenden kaynaklanıyor:** Android Studio'da API Anahtar Sistemi yanlış yapılandırılmış.

---

## ✅ ÇÖZÜM: 4 AŞAMA (20 DAKIKA)

### 🟦 AŞAMA 1: Google Cloud Console Ayarları (5 DAKIKA)

**1.1 - Proje Oluştur**
```
1. https://console.cloud.google.com → Aç
2. Üst solda "Proje seç" → "YENİ PROJE"
3. İsim: "Borç Takip"
4. Oluştur
```

**1.2 - API Etkinleştir**
```
1. Sol menü → "API'ler ve Hizmetler"
2. "Hizmetleri Etkinleştir" butonunu tıkla
3. "Generative Language API" ara
4. Tıkla → "Etkinleştir"
5. Bekle (1-2 dakika)
```

**1.3 - API Anahtarı Oluştur**
```
1. Sol menü → "Kimlik Bilgileri"
2. Üst kısım "Oluştur" → "API Anahtarı"
3. Popup'ta açılan anahtarı KÖPYALA (Ctrl+C)
4. "Kimlik Bilgisini Kapat" - Güvenli sakla!
```

**Anahtarı şöyle görmeli:**
```
AIzaSy[50 karakterli kod]
```

### 🟦 AŞAMA 2: API Anahtarını Kısıtla (3 DAKIKA)

Google Cloud'da **biraz önce oluşturduğun anahtarı güvenleştir:**

```
1. Kimlik Bilgileri → Anahtarı tıkla (Edit simgesi)
2. Sayfanın en üstünde "İsim" başlığında ismi yaz: "Android Borç Takip"
```

**2A - Uygulama Kısıtlaması Ayarla**
```
"Uygulama Kısıtlamaları" bölümünde:

1. ⭕ "Android uygulamaları" seçeneğini tıkla
   
2. "Paket adı *" alanına yaz:
   com.burhan2855.borctakip
   
3. "SHA-1 parmak izi *" alanına yaz:
   6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C
   
   (Eğer farklı parmak izin varsa, Windows terminal'de çalıştır:)
   keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" ^
     -alias androiddebugkey -storepass android -keypass android | findstr "SHA1"
```

**2B - API Kısıtlaması Ayarla**
```
"API Kısıtlamaları" bölümünde:

1. ⭕ "Kısıtlanmış anahtar" seçeneğini tıkla

2. Aşağıda açılan "API Seçin" dropdown'unda:
   📌 "Generative Language API" ara ve seç
   
   (DİKKAT: "Vertex AI API" seçme! YANLIŞ!)
```

**2C - Kaydet**
```
Sayfanın altında "KAYDET" butonunu tıkla
Yeşil "Kimlik Bilgisi güncellendi" mesajı görmen gerekir
```

⏱️ **10 dakika bekle** (API cache'i güncellenir)

---

### 🟦 AŞAMA 3: Android Studio Yapılandırması (5 DAKIKA)

**3.1 - local.properties Güncelle**

Proje kökünde `local.properties` dosyasını aç ve şunu ekle:

```ini
# Windows
GEMINI_API_KEY=AIzaSy[KOPYALADIĞIN_ANAHTAR]

# Örnek:
GEMINI_API_KEY=AIzaSyAUzi7qz-V1dwomDaVWMO9gNGF4fQng4oM
```

(NOT: İlk 7-8 karakteri değiştiğine dikkat et!)

**3.2 - build.gradle.kts Kontrol Et**

`app/build.gradle.kts` dosyası zaten doğru yapılandırılmış. Aşağıdaki satırları kontrol et:

```kotlin
// Dependency olmalı
implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

// defaultConfig içinde olmalı
buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")

// buildFeatures içinde olmalı
buildConfig = true
```

**3.3 - Kotlin Dosyalarını Kontrol Et**

Aşağıdaki dosyalar zaten oluşturulmuş ve doğru:

```
✓ app/src/main/java/com/burhan2855/borctakip/util/GeminiService.kt
✓ app/src/main/java/com/burhan2855/borctakip/util/GeminiViewModel.kt
✓ app/src/main/java/com/burhan2855/borctakip/ui/GeminiAIScreen.kt
```

**3.4 - AndroidManifest.xml Kontrol Et**

Aşağıdakiler zaten var mı kontrol et:

```xml
<!-- Internet izni -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- API Key meta-data -->
<meta-data
    android:name="com.google.ai.mobile.EMBEDDED_API_KEY"
    android:value="AIzaSyAUzi7qz-V1dwomDaVWMO9gNGF4fQng4oM" />
```

---

### 🟦 AŞAMA 4: Derleme ve Test (7 DAKIKA)

**4.1 - Build Yapılandır**

Windows Terminal'i aç ve proje köküne gidip:

```powershell
# JAVA_HOME'u ayarla
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Clean build yap
.\gradlew.bat clean

# APK derle
.\gradlew.bat :app:assembleDebug
```

**Başarı görmek için** bu mesajları bekle:
```
BUILD SUCCESSFUL in XXs
Created app/build/outputs/apk/debug/app-debug.apk
```

**4.2 - Hata Giderleme**

Eğer hata alırsan:
```
HATA 1: "JAVA_HOME is invalid"
→ ÇÖZÜM: Java yolu doğru olmalı: "C:\Program Files\Java\jdk-21"

HATA 2: "API key cannot be empty"
→ ÇÖZÜM: local.properties'de GEMINI_API_KEY var mı kontrol et

HATA 3: "Cannot resolve symbol 'BuildConfig'"
→ ÇÖZÜM: gradle clean && gradle assembleDebug
```

**4.3 - Cihazda Test Et**

```powershell
# APK'yı kur
adb install app/build/outputs/apk/debug/app-debug.apk

# Uygulamayı aç ve "Soru sor" butonunu tıkla
# Yanıt alırsan → HER ŞEY TAMAM! ✅
```

---

## 🆘 SORUN GIDERME

### Problem: "Requests from this Android client application are blocked"

**Neden:** SHA-1 parmak izi Google Cloud'da kaydedilmemiş

**Kontrol:**
```powershell
# Parmak izini al
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" `
  -alias androiddebugkey -storepass android -keypass android | findstr "SHA1"

# Çıktı: SHA1: 6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C
```

**Çözüm:**
1. Google Cloud Console'a gidip anahtarı düzenle
2. Bu SHA-1'i Android Kısıtlaması'na yapıştır
3. 10 dakika bekle
4. Uygulamayı yeniden derle ve test et

---

### Problem: "Model gemini-1.5-flash not found"

**Neden:** Model adı yanlış veya deprecated

**Çözüm:** `GeminiService.kt`'de şu satırı değiştir:
```kotlin
// YANLIŞ
modelName = "gemini-1.5-flash"

// DOĞRU
modelName = "gemini-2.0-flash"  // ← Bunu kullan
```

---

### Problem: "API keys are not supported by this API"

**Neden:** Yanlış API seçildi

**Çözüm:**
1. Google Cloud Console'da **Generative Language API** etkinleştirdin mi?
2. API anahtarında **Generative Language API** seçili mi?
3. (NOT: "Vertex AI API" seçme!)

---

## 📚 Başka Rehberler

Kurulumdan sonra bu dosyaları oku:

1. **GEMINI_API_SISTEM_REHBERI.md** - Detaylı teknik bilgiler
2. **GEMINI_API_HATA_COZUMLEME.md** - Yaygın hatalar
3. **API_ANAHTAR_HIZLI_REFERANS.md** - Hızlı kod örnekleri

---

## 🎓 Kod Örneği (Kontrol Et)

Tüm bu kod dosyalar zaten yazılmış! Sadece kontrol et:

**GeminiService.kt:**
```kotlin
object GeminiService {
    fun initialize() {
        generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = BuildConfig.GEMINI_API_KEY  // ← local.properties'den geliyor
        )
    }
}
```

**MainActivity.kt'de kullanım:**
```kotlin
lifecycleScope.launch {
    GeminiService.initialize()
    val response = GeminiService.generateContent("Merhaba!")
    Log.d("Gemini", "Yanıt: $response")
}
```

---

## ✨ BAŞARININ BELİRTİLERİ

Tüm bu şeyleri görürsen kurulum başarılı:

- ✅ `./gradlew assembleDebug` hatasız tamamlanıyor
- ✅ APK oluşturuluyor: `app-debug.apk`
- ✅ Uygulamayı cihazda yükleyebildin
- ✅ Yazı gir → Soru sor → Yanıt al
- ✅ Logcat'te hata yok

---

## 📋 Son Kontrol Listesi

Aşağıdaki yapılıp yapılmadığını kontrol et:

- [ ] Google Cloud Projesi oluşturdun
- [ ] Generative Language API etkinleştirdin
- [ ] API anahtarı oluşturdun
- [ ] Android kısıtlaması ayarladın
- [ ] SHA-1 parmak izini ekledin
- [ ] local.properties'i güncelledin
- [ ] 10 dakika bekledin
- [ ] `gradlew clean` ve `assembleDebug` yaptın
- [ ] APK oluşturulmuş
- [ ] Cihaza kurdum
- [ ] Soru sorup yanıt aldım

Tüm kutular işaretliyse → **HER ŞEY TAMAM!** 🎉

---

## 💬 Sık Sorulan Sorular

**S: API Key'i paylaşabilir miyim?**
A: Hayır! local.properties'i .gitignore'a ekle

**S: Ücretsiz mi?**
A: Evet! 100,000 serbest aramadan sonra ücretlendirilir

**S: Çevrimdışı çalışır mı?**
A: Hayır, internet gerekli

**S: Başka modeller kullanabilir miyim?**
A: Evet: `gemini-1.5-pro`, `gemini-1.5-flash-latest`

---

## 🆘 YARDIM

Sorun yaşarsan:

1. Bu dosyayı tekrar oku (en çok sorun AŞAMA 2'de oluyor)
2. **GEMINI_API_HATA_COZUMLEME.md** oku
3. Google Cloud Console'da ayarları kontrol et
4. SHA-1 parmak izini doğrula
5. 10 dakika bekle (API cache)
6. Tekrar derle ve test et

---

## 📞 Bağlantılar

- [Google Cloud Console](https://console.cloud.google.com)
- [Google AI Documentation](https://ai.google.dev/tutorials/kotlin)
- [Generative Language API](https://ai.google.dev/docs)

---

**Başarılar! 🚀**

*Sorun? Başlangıç Rehberi'nden başla. Sorunu çözemezsen, GEMINI_API_HATA_COZUMLEME.md oku.*

Güncelleme Tarihi: 27 Aralık 2025
