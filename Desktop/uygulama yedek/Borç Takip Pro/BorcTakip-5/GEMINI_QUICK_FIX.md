# Gemini API - Hızlı Çözüm Adımları

## Sorun: "Hata: API isteği engellendi"

### ✅ Çözüm Adımları

1. **Google Cloud Console'a Git**
   ```
   https://console.cloud.google.com
   ```

2. **API Key'i Bul**
   - Sol menü > APIs & Services > Credentials
   - Tüm API keys'leri listele
   - `AIzaSyA...` başlayan key'i bul (senin key'in)

3. **Key Kısıtlamalarını Ayarla**
   - API Key'in üzerine tıkla
   - "Application restrictions" bölümü:
     - ✅ `Android apps` seç
     - Add (Ekle) butonuna tıkla
     - **Package name:** `com.burhan2855.borctakip`
     - **SHA-1 fingerprint:** `6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C`
     - Ekle

4. **API Kısıtlamaları**
   - "API restrictions" bölümü:
     - ✅ `Restrict key` seç
     - "Generative Language API" bul ve seç
     - KAYDET

---

## Sorun: "Model bulunamadı (404)"

### ✅ Çözüm

1. **API'yi Etkinleştir**
   ```
   Google Cloud Console > APIs & Services > Library
   "generativeai" ara
   "Generative Language API" bulunca ENABLE tıkla
   ```

2. **Model Adını Kontrol Et**
   - `GeminiViewModel.kt` dosyasında:
   ```kotlin
   modelName = "gemini-1.5-flash"  // Bu olmalı
   ```

---

## Sorun: "Erişim reddedildi (403)"

### ✅ Çözüm: Billing Aç

**ÖNEMLİ:** Ücretsiz tier'da sadece sınırlı kullanım var. Daha fazla kullanım için Billing gerekir.

1. **Billing Account Oluştur**
   ```
   Google Cloud Console > Billing
   "Create Billing Account"
   Kredi kartı ekle
   ```

2. **Projeyi Billing'e Bağla**
   ```
   Billing > "Link a project"
   Senin projenizi seç
   ```

---

## Sorun: "API Key geçersiz (401)"

### ✅ Çözüm

1. **Key'i Doğrula**
   - `BorcTakip-5/local.properties` dosyasını aç:
   ```properties
   GEMINI_API_KEY=AIzaSyA...
   ```

2. **Yeni Key Oluştur**
   ```
   Google Cloud Console > APIs & Services > Credentials
   "+ Create Credentials" > API Key
   Yeni key'i kopyala
   local.properties'e yapıştır
   ```

3. **Cache Temizle ve Yeniden Derle**
   ```bash
   ./gradlew clean
   ./gradlew :app:assembleDebug
   ```

---

## Android SHA-1 Fingerprint'imi Nasıl Bulurum?

Powershell'de çalıştır:

```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | findstr "SHA1"
```

Output:
```
SHA1: 6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C
```

Bu değeri Google Cloud Console'da kaydet.

---

## Uygulamayı Test Et

```bash
# 1. Gradle cache temizle
./gradlew clean

# 2. Yeniden derle
./gradlew :app:assembleDebug

# 3. Cihaza/emulatöre yükle
./gradlew :app:installDebug

# 4. Uygulamayı aç ve test et
# Gemini ekranına git > bir soru sor > cevap gelmeldi

# 5. Hata varsa logcat'te oku
adb logcat | grep "GeminiViewModel"
```

---

## Kontrol Listesi

Hata alıyorsan sırasıyla kontrol et:

- [ ] ✅ API Key `local.properties` dosyasında doğru yerinde
- [ ] ✅ Package name: `com.burhan2855.borctakip` Google Cloud'da kayıtlı
- [ ] ✅ SHA-1: `6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C` Google Cloud'da kayıtlı
- [ ] ✅ Generative Language API etkin
- [ ] ✅ Billing account bağlı
- [ ] ✅ API Key kısıtlamaları ayarlanmış
- [ ] ✅ Cache temizlendi ve yeniden derlenmiş

---

## Tüm Hata Kodları

| Kod | Anlam | Çözüm |
|-----|-------|-------|
| 400 | Bad Request | Prompt formatı hatalı |
| 401 | Unauthorized | API Key yanlış |
| 403 | Forbidden | Billing/Quota sorunu |
| 404 | Not Found | Model bulunamadı |
| 429 | Too Many Requests | Çok hızlı istek |
| 500 | Server Error | Google Server hatalı |

---

**Tarih:** 2025-12-27  
**Proje:** BorcTakip Android  
**Kütüphane:** Google Generative AI SDK 0.9.0
