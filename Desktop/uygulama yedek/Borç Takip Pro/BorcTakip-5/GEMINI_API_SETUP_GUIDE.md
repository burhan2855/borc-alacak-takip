# Gemini API Kurulum ve Sorun Giderme Kılavuzu

## Gördüğün Hatalar ve Çözümleri

### 1. ❌ "Hata: API isteği engellendi"
**Sebep:** API anahtarının kısıtlamaları ayarlı değil veya yanlış ayarlanmış

**Çözüm:**
```
1. Google Cloud Console'a git: https://console.cloud.google.com
2. Sağ üst köşedeki proje adını tıkla
3. Sol menüden "APIs & Services" > "Credentials" seç
4. API Key 4'ü bul (AIzaSyAUzi7qz-V1dwomDaVWMO9gNGF4fQng4oM)
5. "Application restrictions" bölümünde:
   ✅ "Android apps" seçili olmalı
   ✅ Package name: com.burhan2855.borctakip
   ✅ SHA-1 fingerprint: 6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C
6. "API restrictions" bölümünde:
   ✅ "Restrict key" seçili olmalı
   ✅ "Generative Language API" seçili olmalı
7. KAYDET
```

### 2. ❌ "Model bulunamadı (gemini-1.5-flash)"
**Sebep:** API modele erişemi yok veya model adı yanlış

**Çözüm:**
```
1. Generative Language API'nin etkin olup olmadığını kontrol et:
   - Google Cloud Console > APIs & Services > Library
   - "generativeai" veya "Generative Language API" ara
   - "ENABLE" butonuna tıkla
   
2. Doğru model adını kullan:
   - gemini-1.5-flash (hızlı, daha ucuz)
   - gemini-1.5-pro (daha güçlü)
   - gemini-pro (eski model)
```

### 3. ❌ "API anahtarı geçersiz veya yetkisiz (401)"
**Sebep:** API anahtarı yanlış veya süresi dolmuş

**Çözüm:**
```
1. API Key doğruluğunu kontrol et:
   local.properties dosyasında GEMINI_API_KEY değeri tam olmalı
   
2. Yeni API Key oluştur:
   - Google Cloud Console > APIs & Services > Credentials
   - "+ Create Credentials" > API Key
   - Yeni key'i local.properties dosyasına kopyala
   
3. Gradle cache'i temizle:
   ./gradlew clean
```

### 4. ❌ "Erişim reddedildi (403)"
**Sebep:** API Key'in gerekli izinleri yok veya quota aşıldı

**Çözüm:**
```
1. Billing'i etkinleştir (ÖNEMLI!):
   - Google Cloud Console > Billing
   - Billing account seç
   - Bu projeyi billing account'a bağla
   
2. Quota'yı kontrol et:
   - Google Cloud Console > APIs & Services > Quotas
   - Generative Language API'nin quotas sınırlarını kontrol et
```

### 5. ❌ "API isteği beklenmedik cevap verdi"
**Sebep:** Model yanıt format hatası (MissingFieldException)

**Çözüm:**
```
1. Kod yeniden derle:
   ./gradlew clean
   ./gradlew :app:assembleDebug
   
2. Model adını kontrol et (build.gradle.kts dosyasında)
   
3. API Key'in geçerli olduğundan emin ol
```

## Adım Adım Kurulum

### 1. Google Cloud Project Oluştur
```bash
1. Google Cloud Console'a git: https://console.cloud.google.com
2. Sağ üst köşedeki "Select a Project" > "New Project"
3. Proje adı gir: "BorcTakip"
4. Create
```

### 2. Generative Language API'yi Etkinleştir
```bash
1. Console > APIs & Services > Library
2. "generativeai" ara
3. "Generative Language API" bulunca tıkla
4. ENABLE butonuna tıkla
```

### 3. Billing Ayarını Düzenle (ZORUNLU!)
```bash
1. Google Cloud Console > Billing
2. Create a new Billing Account
3. Kredi kartını ekle
4. Projeni bu billing account'a bağla
```

### 4. API Key Oluştur
```bash
1. Console > APIs & Services > Credentials
2. "+ Create Credentials" > API Key
3. Oluşturulan Key'i kopyala
4. local.properties dosyasına yapıştır:
   GEMINI_API_KEY=AIzaSyA...
```

### 5. API Key'i Kısıtla (Güvenlik)
```bash
1. API Key'in üzerine tıkla
2. Application restrictions:
   ✅ Android apps
   ✅ Package name: com.burhan2855.borctakip
   ✅ SHA-1: 6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C
3. API restrictions:
   ✅ Restrict key
   ✅ Generative Language API
4. KAYDET
```

## Sık Sorulan Sorular

### P: Android SHA-1 fingerprint'i nereden bulurum?
```bash
# Terminal/PowerShell'de çalıştır:
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android | findstr "SHA1"
```

### P: local.properties dosyası nerede?
```
BorcTakip-5/local.properties
```

### P: Yeni API Key'i sonra tekrar build etmem gerekir mi?
Evet. API Key'i değiştirdikten sonra:
```bash
./gradlew clean
./gradlew :app:assembleDebug
```

### P: Modeli gemini-pro olarak değiştirebilir miyim?
Evet, GeminiViewModel.kt dosyasında:
```kotlin
modelName = "gemini-pro"  // gemini-1.5-flash yerine
```

### P: Kullanıcıdan API Key isteme özelliği ekleyebilir miyim?
Evet, Settings ekranına API Key giriş alanı ekleyebiliriz.

## Test Etme

API'nin çalışıp çalışmadığını test etmek için:

1. Uygulamayı çalıştır
2. Gemini ekranına git
3. Bir soru sor (örn: "Merhaba")
4. Cevap gelmeldi
5. Eğer hata görürsen, logcat'te hata mesajını oku:
   ```bash
   adb logcat | grep "GeminiViewModel"
   ```

## Hata Mesajlarının Anlamı

| Hata | Anlamı | Çözüm |
|------|--------|-------|
| API isteği engellendi | Key kısıtlaması yanlış | Kısıtlamaları kontrol et |
| 401 Unauthorized | API Key geçersiz | Yeni key oluştur |
| 403 Forbidden | Billing yok veya quota aşıldı | Billing'i aç |
| 404 Not Found | Model bulunamadı | Model adını kontrol et |
| MissingFieldException | API yanıt formatı hatalı | Gradle cache temizle |

## Hızlı Çözüm Adımları

Eğer hala hata alıyorsan sırasıyla dene:

```bash
# 1. Gradle cache temizle
./gradlew clean

# 2. API Key'i doğrula
# local.properties'i aç ve GEMINI_API_KEY kontrol et

# 3. Yeniden derle
./gradlew :app:assembleDebug

# 4. Uygulamayı sil ve tekrar yükle
adb uninstall com.burhan2855.borctakip
./gradlew :app:installDebug

# 5. Logcat'te hata mesajını gör
adb logcat | grep "GeminiViewModel"
```

## İletişim / Yardım

Eğer yukarıdaki çözümler işe yaramazsa, aşağıdaki bilgileri kontrol et:

1. ✅ Billing account bağlı mı?
2. ✅ Generative Language API etkin mi?
3. ✅ API Key doğru mu?
4. ✅ SHA-1 fingerprint doğru mu?
5. ✅ Android package name doğru mu?

Tüm kontroller yapıldıysa, Google Cloud Console'da detaylı hata loglarını incele.

---

**Son Güncelleme:** 2025-12-27
**Proje:** BorcTakip Android App
**API Sürümü:** Generative Language API (Gemini)
