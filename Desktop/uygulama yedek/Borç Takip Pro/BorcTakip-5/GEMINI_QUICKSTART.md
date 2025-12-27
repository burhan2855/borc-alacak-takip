# 🚀 Gemini AI - Hızlı Başlangıç Rehberi

## 5 Dakika içinde Başlayın

### 1️⃣ API Anahtarı Alın

```
1. https://aistudio.google.com açın
2. Google ile giriş yapın
3. "Create API key" tıklayın
4. Anahtarı kopyalayın
```

**Örnek:** `AIzaSyBoVtEtgl6-cgdgg7GpsS_6I1iYcC_e2HA`

---

### 2️⃣ Uygulamaya Anahtarı Ekleyin

```
BorçTakip Uygulaması Açılır
    ↓
Gemini AI Bölümüne Gidin
    ↓
Ayarlar (⚙️) Tıklayın
    ↓
API Anahtarını Yapıştırın
    ↓
"Kaydet" Tıklayın
```

---

### 3️⃣ Gemini AI'ı Kullanın

**Anasayfaya dönün** → **Gemini AI Asistanı** → **Soru yazın** → **Yanıt alın**

---

## 💬 Örnek Sorular

| Soru | Kullanım Alanı |
|------|-----------------|
| "Aylık 5000 TL'lik harcamayı nasıl azaltırım?" | Bütçeleme |
| "Kredi kartı borcunu hızlı ödemenin yolları nelerdir?" | Borç Yönetimi |
| "Birikim yapmak için stratejiler öner" | Tasarruf |
| "Banka kredisi almadan önce dikkat etmem gerekenler" | Finansal Planlama |
| "Aylık harcama takibinde sorun yaşıyorum, nasıl başlamalıyım?" | Rehberlik |

---

## ⚠️ Önemli Hatırlatmalar

```
🔒 API Anahtarını Güvenli Tutun
   ├─ Kimseyle Paylaşmayın
   ├─ Public repositories'e Commit Etmeyin
   └─ Düzenli Olarak Kontrol Edin

⚡ Ücretsiz Kullanım
   ├─ Google AI Studio: ÜCRETSIZ
   └─ Yüksek kullanım: Ücretlendirilebilir
```

---

## 🛠️ Geliştirici Modu

Uygulamayı kendiniz derlemek istiyorsanız:

### local.properties'e Ekleyin:
```properties
GEMINI_API_KEY=YOUR_API_KEY_HERE
```

### Derleyin:
```bash
./gradlew :app:assembleDebug
```

---

## 🎯 İlk Adımlar Kontrol Listesi

- [ ] Google hesabınız var
- [ ] AI Studio'da API key oluşturdunuz
- [ ] API key'i kopyaladınız
- [ ] Uygulamayı açtınız
- [ ] Ayarlar ekranına gittiniz
- [ ] API key'i yapıştırdınız
- [ ] "Kaydet" tıkladınız
- [ ] Ana sayfaya döndünüz
- [ ] "Gemini AI" bölümünü açtınız
- [ ] İlk sorunuzu sordunuz ✨

---

## 🆘 Sorun Mu Yaşıyorsunuz?

### "API Anahtarı Eksik" Hatası
→ API key'i ayarlara doğru yapıştırıp kaydettiğinizden emin olun

### "İnternet Bağlantısı" Hatası
→ WiFi veya mobil veri bağlantınızı kontrol edin

### "Geçersiz API Key" Hatası
→ Google AI Studio'da yeni bir key oluşturun

### Hâlâ sorun varsa?
→ Logcat'i kontrol edin: `adb logcat | grep Gemini`

---

## 📱 Play Store'da Yayınlama (İleri)

```
API Key Kısıtlamaları Ayarla
    ↓
Android App Restriction Seç
    ↓
SHA-1 Fingerprint Ekle
    ↓
Generative Language API Seç
    ↓
Play Store'a Yükle
```

**SHA-1 Bulma:**
```bash
keytool -list -v -keystore release-key.keystore -alias androidreleasekey
```

---

## 💡 İpuçları

✨ **Etkili Sorular Yazın:**
- Açık ve kısa olun
- Bağlam sağlayın
- Spesifik olun

📌 **Yanıtları Saklayın:**
- Önemli yanıtları note alın
- Screenshots alın
- Yeniden sormak istediğinizde referans kullanın

🔄 **Denemeyi Sürdürün:**
- Aynı soruyu farklı şekillerde sorun
- Yanıtlara segumentation ekleyin
- Feedback verin

---

## 📚 Daha Fazla Bilgi

- **Resmi Belge:** https://ai.google.dev
- **API Dokümentasyonu:** https://ai.google.dev/docs
- **Fiyatlandırma:** https://ai.google.dev/pricing
- **Sınırlamalar:** https://ai.google.dev/docs/safety_guidelines

---

**Version:** 1.0
**Son Güncelleme:** Aralık 2025
**Dil:** Türkçe
