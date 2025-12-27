# 📖 Gemini API Entegrasyonu - Dokümantasyon Index

## 🎯 BAŞLA BURADAN

> Eğer ilk defa okuyorsan, **sayfanın altındaki "Sırayla Oku" bölümünü** takip et!

---

## 📚 Dokümantasyon Dosyaları

### 🔴 ZORUNLU (Kurulum İçin)
| Dosya | Amaç | Okuma Süresi |
|-------|------|--------------|
| **00_BASLANGIC_REHBERI.md** | Başlangıç (EN ÖNEMLI!) | 10 dk |
| **API_ANAHTAR_OZET.md** | Yapılanlar ve Özet | 5 dk |
| **GEMINI_API_SISTEM_REHBERI.md** | Detaylı Kurulum | 20 dk |

### 🟡 KULLANIŞLI (Sorun Çözmek İçin)
| Dosya | Amaç | Kullan |
|-------|------|--------|
| **GEMINI_API_HATA_COZUMLEME.md** | Hatalar ve Çözümleri | Hata alınca |
| **API_ANAHTAR_HIZLI_REFERANS.md** | Kod Örnekleri | Kod yazarken |

### 🟢 OPSİYONEL (Setup Scriptleri)
| Dosya | Platform | İşlev |
|-------|----------|-------|
| **setup_gemini_api.bat** | Windows | Otomatik kurulum |
| **setup_gemini_api.ps1** | PowerShell | Otomatik kurulum |

---

## 🏗️ Kod Dosyaları (Zaten Hazır)

Aşağıdaki dosyalar otomatik olarak oluşturulmuş ve kullanıma hazır:

### Kotlin Services
```
app/src/main/java/com/burhan2855/borctakip/util/
├── GeminiService.kt       ✅ API çağrılarını yönetir
├── GeminiViewModel.kt     ✅ UI mantığı ve state
└── ...
```

### Compose UI
```
app/src/main/java/com/burhan2855/borctakip/ui/
├── GeminiAIScreen.kt      ✅ Örnek ekran (kullanabilirsin)
└── ...
```

### Yapılandırma
```
├── build.gradle.kts       ✅ Dependency ve ayarlar
├── local.properties       ✅ API Key (sen güncelle!)
└── AndroidManifest.xml    ✅ İzinler ve meta-data
```

---

## 🎓 Sırayla Oku (Tavsiye Edilen Sırası)

### Gün 1: Kurulum (30 dakika)
```
1. 00_BASLANGIC_REHBERI.md        (10 dk)
   ↓
2. AŞAMA 1: Google Cloud (5 dk)
   ↓
3. AŞAMA 2: API Kısıtla (3 dk)
   ↓
4. AŞAMA 3: Android Studio (5 dk)
   ↓
5. AŞAMA 4: Derle & Test (7 dk)
```

Sonuç: ✅ Çalışan uygulamam var!

### Gün 2: Derinlemesine Anlama (45 dakika)
```
1. GEMINI_API_SISTEM_REHBERI.md   (15 dk)
2. API_ANAHTAR_OZET.md             (10 dk)
3. API_ANAHTAR_HIZLI_REFERANS.md   (20 dk)
```

Sonuç: 🧠 Nasıl çalıştığını tam anladım!

### Gün 3+: Sorun Çözmek
```
Hata alırsan:
1. Hatanın adını kopyala
2. GEMINI_API_HATA_COZUMLEME.md'de ara
3. Çözümü uygula
```

---

## 🗂️ Hızlı Referans

### Sorunun Ne Olduğunu Biliyorsan

| Sorun | Dosya |
|-------|-------|
| Build başarısız | GEMINI_API_HATA_COZUMLEME.md → "Build Testi" |
| "Blocked" hatası | GEMINI_API_HATA_COZUMLEME.md → Hata 1 |
| API Key boş | GEMINI_API_HATA_COZUMLEME.md → Hata 5 |
| Model not found | GEMINI_API_HATA_COZUMLEME.md → Hata 2 |
| Kod yazacağım | API_ANAHTAR_HIZLI_REFERANS.md → Kod Snippet'leri |
| Merak ediyorum | GEMINI_API_SISTEM_REHBERI.md → Detaylar |

---

## 📋 Dosyalar Nerede?

Proje kökü:
```
C:\Users\burha\Desktop\uygulama yedek\Borç Takip Pro\BorcTakip-5\
```

### Dokümantasyon
```
00_BASLANGIC_REHBERI.md              ← BURADAN BAŞLA
API_ANAHTAR_OZET.md
GEMINI_API_SISTEM_REHBERI.md
GEMINI_API_HATA_COZUMLEME.md
API_ANAHTAR_HIZLI_REFERANS.md
README_DOKUMANTASYON_INDEX.md        ← Bu dosya
```

### Kotlin Kodu
```
app/src/main/java/com/burhan2855/borctakip/
├── util/
│   ├── GeminiService.kt             ← API çağrıları
│   └── GeminiViewModel.kt           ← State yönetimi
└── ui/
    └── GeminiAIScreen.kt            ← Örnek ekran
```

### Yapılandırma
```
app/
├── build.gradle.kts                 ← Dependency
├── src/main/AndroidManifest.xml     ← İzinler
└── proguard-rules.pro               ← Güvenlik

local.properties                     ← API Key (Özel!)
```

### Kurulum Scriptleri
```
setup_gemini_api.bat                 ← Windows Batch
setup_gemini_api.ps1                 ← PowerShell
```

---

## ✅ Kontrol Listesi: Ne Yapıldı?

### ✅ Yapılan İşler
- [x] API Service sınıfı oluşturuldu (GeminiService.kt)
- [x] ViewModel oluşturuldu (GeminiViewModel.kt)
- [x] Compose UI ekranı oluşturuldu (GeminiAIScreen.kt)
- [x] build.gradle.kts yapılandırıldı
- [x] AndroidManifest.xml ayarlandı
- [x] ProGuard kuralları eklendi
- [x] local.properties hazırlandı
- [x] Kurulum dokümantasyonu yazıldı
- [x] Hata çözümleme rehberi yazıldı
- [x] Kod örnekleri hazırlandı
- [x] Kurulum scriptleri oluşturuldu

### ⏳ Yapılacaklar (Senin Yapman Gereken)
1. [ ] Google Cloud Projesi oluştur
2. [ ] Generative Language API etkinleştir
3. [ ] API anahtarı oluştur
4. [ ] Android kısıtlamalarını ayarla
5. [ ] local.properties'i güncelle
6. [ ] `./gradlew assembleDebug` yap
7. [ ] Cihazda test et

---

## 🚀 Hızlı Start (5 Adım)

```bash
# 1. Google Cloud Console'a gidip:
#    - Proje oluştur
#    - Generative Language API etkinleştir
#    - API anahtarı oluştur (AIzaSy...)

# 2. local.properties'i güncelle:
#    GEMINI_API_KEY=AIzaSy[SENIN_ANAHTARIN]

# 3. Build yapılandırmalarını kontrol et:
#    ✓ build.gradle.kts
#    ✓ AndroidManifest.xml
#    ✓ Kotlin dosyaları

# 4. Build et:
./gradlew clean :app:assembleDebug

# 5. Cihazda test et:
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 🆘 Sorun Yaşıyorsan?

### Adım 1: Hatanı Oku
Hangi hatayı alıyorsun? Örnek:
- "Requests from this Android client application are blocked"
- "Model gemini-1.5-flash not found"
- "API keys are not supported"

### Adım 2: GEMINI_API_HATA_COZUMLEME.md Aç
Hata adını dosyada ara ve çözüm uygula.

### Adım 3: Kontrol Et
- Google Cloud Console'da ayarları doğrula
- SHA-1 parmak izini kontrol et
- 10 dakika bekle (API cache)
- Tekrar derle

### Adım 4: Hala Çalışmıyorsa
1. 00_BASLANGIC_REHBERI.md'yi tekrar oku
2. GEMINI_API_SISTEM_REHBERI.md'yi oku
3. Her aşamayı adım adım kontrol et

---

## 📞 Yardımcı Bağlantılar

- **Google Cloud Console**: https://console.cloud.google.com
- **Google AI Documentation**: https://ai.google.dev/tutorials/kotlin
- **Generative Language API**: https://ai.google.dev/docs
- **Android Developer Docs**: https://developer.android.com

---

## 📊 Doküman İstatistikleri

| Dokümantasyon | Kelime | Kod Örneği | Hata Çözümü |
|---------------|--------|-----------|------------|
| 00_BASLANGIC_REHBERI.md | 3000+ | 10+ | 4 |
| GEMINI_API_SISTEM_REHBERI.md | 5000+ | 20+ | 6 |
| GEMINI_API_HATA_COZUMLEME.md | 4000+ | 15+ | 8 |
| API_ANAHTAR_HIZLI_REFERANS.md | 2000+ | 15+ | 3 |
| **TOPLAM** | **14000+** | **60+** | **21** |

---

## 🎯 Son Tavsiyeler

✅ **YAPILMASI GEREKENLER:**
- Google Cloud ayarlarını dikkatlice oku
- API anahtarına zamanında sınırlandırma ekle
- local.properties'i gizli tut
- Regular olarak maliyeti kontrol et

❌ **YAPILMAMASI GEREKENLER:**
- API Key'i GitHub'a yükleme
- API Key'i uygulamaya hardcode etme
- Vertex AI API seçme (Generative Language API seç!)
- Eski modeller kullanma (gemini-2.0-flash kullan)

---

## 📝 Versiyon Bilgisi

```
Belge Sürümü: 1.0
Oluşturma Tarihi: 27 Aralık 2025
SDK Sürümü: generativeai:0.9.0
Model: gemini-2.0-flash
Dil: Türkçe
```

---

## 🎓 İleri Konular (Sonra)

Temel kurulumdan sonra, bunu öğrenmeyi düşün:
- Chat tabanlı konuşmalar (multi-turn)
- Resim analiz etme
- Rate limiting
- Error handling
- Cache mekanizması
- Firebase entegrasyonu

---

**Hazırsan? 00_BASLANGIC_REHBERI.md'yi aç ve başla! 🚀**

*Son güncelleme: 27 Aralık 2025*
