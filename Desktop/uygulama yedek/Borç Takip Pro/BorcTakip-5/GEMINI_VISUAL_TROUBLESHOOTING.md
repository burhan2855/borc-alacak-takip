# Gemini API - Resimlerdeki Hataları Çöz

## Resim 1: "Android uygulamasını düzenle"
```
Paket adı: com.burhan2855.borctakip
SHA-1 sertifika parmak izi: 6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C
```

✅ **BU DOĞRU!** Bildirdiğin SHA-1, Debug keystore'undan alınan SHA-1 ile eşleşiyor.

---

## Resim 2: "Hata: API isteği engellendi"
```
Hata mesajı: "Requests from this Android client application <empty> are blocked"
```

### ❌ PROBLEM
- API Key'in Application Restrictions ayarları yanlış veya eksik
- Paket adı kayıtlı değil
- SHA-1 fingerprint kayıtlı değil

### ✅ ÇÖZÜM

1. **Google Cloud Console'a Git**
   ```
   https://console.cloud.google.com
   ```

2. **API Key'i Bul ve Düzenle**
   - Sol menü > APIs & Services > Credentials
   - API Key 4'ü bul (AIzaSyAUzi...)
   - Tıkla ve açılan sayfada:

3. **Application Restrictions**
   ```
   Seçim: Android apps
   + Ekle
   
   Package name: com.burhan2855.borctakip
   SHA-1 fingerprint: 6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C
   
   Ekle
   ```

4. **API Restrictions**
   ```
   ☑️ Restrict key (işaretli)
   
   Generative Language API seç
   ```

5. **KAYDET** (sağ üst köşe)

---

## Resim 3: "API anatarını tanımlamak için benzeriz bir ad kullanın"
```
API anahtarını tanımlamak için benzeriz bir ad kullanın.
```

### PROBLEM
Bu sadece bir öneri. İsterseniz:
- "BorcTakip Android Debug" 
- "Gemini API Key"
- vs. gibi bir ad verebilirsin

### ÇÖZÜM
- Ad alanını doldur (zorunlu değil)
- Kaydet

---

## Resim 4: "Model bulunamadı (gemini-1.5-flash)"
```
Hata: "models/gemini-1.5-flash is not found for API version v1beta"
```

### ❌ PROBLEM
Generative Language API etkin değil

### ✅ ÇÖZÜM

1. **Google Cloud Console > APIs & Services > Library**
2. **Ara:** "generativeai"
3. **Sonuç:** "Generative Language API"
4. **Tıkla ve ENABLE klikle**

---

## Resim 5: "OAuth2 token gerekli"
```
Hata: "API keys are not supported by this API. 
Expected OAuth2 access token or other authentication 
credentials that assert a principal."
```

### ❌ PROBLEM
- API Key türü yanlış
- OAuth2 authentication gerekli (server-side işin için)

### ✅ ÇÖZÜM
- Mobil uygulamada API Key kullanmak doğru
- Bu hata Google server'ında bir ayar sorunu gösteriyor
- Retry et, 30 saniye bekle

---

## Resim 6: "Restrict key"
```
✓ Restrict key (Seçilmiş)
  1 API
  
  Selected APIs:
  Generative Language API
```

✅ **BU DOĞRU!** Kısıtlama ayarı tamam. Sadece Generative Language API erişebilir.

---

## Resim 7: "Beklenmedik Response"
```
Hata: {
  "error": {
    "code": 404,
    "message": "models/gemini-1.5-flash is not found"
  }
}

Hata: MissingFieldException: Field 'details' is required...
```

### PROBLEM
1. Model API'den bulunamıyor
2. Yanıt format hatalı

### ÇÖZÜM
```bash
# Gradle cache'i temizle
./gradlew clean

# Yeniden derle
./gradlew :app:assembleDebug
```

---

## Resim 8: "Model bulunamadı. Lütfen API anahtarını kontrol edin"
```
Bu resimdeki seçim:
✓ Restrict key
  1 API
  
  Selected APIs:
  Generative Language API
```

### PROBLEM
- Model bulunamıyor çünkü API etkin değil
- VEYA Generative Language API seçili değil

### ÇÖZÜM
Bu resimde gösterilen ayar **doğru**. Yani:
1. Google Cloud Console'da bu ayarı tamamladın ✅
2. Ama API'yi etkinleştirmedin ❌

Sonraki adım:
- Google Cloud Console > Library
- "Generative Language API" ara
- ENABLE tıkla

---

## Hızlı Kontrol Listesi

Sırasıyla kontrol et:

- [ ] **1. Paket Adı ve SHA-1**
  ```
  Paket: com.burhan2855.borctakip
  SHA-1: 6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C
  ```

- [ ] **2. Generative Language API Etkin**
  - Google Cloud > Library
  - Ara: "generativeai"
  - ENABLE klikle

- [ ] **3. API Key Kısıtlaması Doğru**
  - Credentials > API Key
  - Restrict key ✓
  - Generative Language API seçili ✓
  - Android apps seçili ✓
  - Paket adı + SHA-1 doğru ✓

- [ ] **4. Billing Hesabı Bağlı**
  - Google Cloud > Billing
  - Billing account seç
  - Project'i bağla

- [ ] **5. Gradle Clean Build**
  ```bash
  ./gradlew clean
  ./gradlew :app:assembleDebug
  ```

- [ ] **6. Test**
  - Uygulamayı çalıştır
  - Gemini ekranına git
  - Soruştur: "Merhaba"
  - Cevap geldi mi?

---

## Hata Alırsan

| Hata Mesajı | Çözüm |
|------------|-------|
| "API isteği engellendi" | Paket adı ve SHA-1 kontrol et |
| "Model bulunamadı (404)" | API'yi ENABLE et |
| "Erişim reddedildi (403)" | Billing account bağla |
| "API Key geçersiz (401)" | Key doğrula, yeni key oluştur |
| "MissingFieldException" | Cache temizle, rebuild et |

---

## Sonuç

Seni gördüğüm hatalardan en sık:
1. **API Key kısıtlamaları yanlış** → Credentials ekranında düzeltt
2. **Generative Language API etkin değil** → Library'de ENABLE klikle
3. **Billing yok** → Billing account oluştur ve bağla

Bu 3 şeyi yapıştığında hata çözülecek! 🎉

---

**Tarih:** 2025-12-27
**Tür:** Visual Problem Solving Guide
