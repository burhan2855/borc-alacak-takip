# Gemini AI Entegrasyonu - Özel Talimatlar

Bu dokümanda, BorçTakip uygulamasında Gemini AI'ı nasıl kullanacağınızı öğreneceksiniz.

## 📋 İçindekiler
1. [API Anahtarı Alma](#api-anahtarı-alma)
2. [Uygulamaya API Anahtarı Ekleme](#uygulamaya-api-anahtarı-ekleme)
3. [Gemini AI Özelliği Kullanma](#gemini-ai-özelliği-kullanma)
4. [Play Store'da Dağıtım](#play-storede-dağıtım)

---

## API Anahtarı Alma

### Adım 1: Google AI Studio'ya Erişim

1. Tarayıcınızda şu adresi açın: **https://aistudio.google.com**
2. Google hesabınızla giriş yapın (Gmail hesabı ile olur)

### Adım 2: API Key Oluşturma

1. Sayfanın sol menüsünde **"API keys"** veya **"Get API key"** düğmesine tıklayın
2. **"Create API key"** veya **"Create new API key"** düğmesine tıklayın
3. Açılan diyalogdan **"Create API key in new project"** seçeneğini seçin
4. API anahtarı oluşturulur ve size gösterilir
5. Anahtarı **kopyalayıp güvenli bir yere kaydedin**

**Örnek API anahtarı:**
```
AIzaSyBoVtEtgl6-cgdgg7GpsS_6I1iYcC_e2HA
```

> ⚠️ **ÖNEMLİ:** Bu anahtarı kimseyle paylaşmayın! Başkası tarafından kötüye kullanılabilir.

---

## Uygulamaya API Anahtarı Ekleme

### Seçenek 1: Uygulama İçinde Girme (Kolay - Önerilen)

Bu uygulamada API anahtarını doğrudan uygulama içinden girebilirsiniz:

1. **BorçTakip uygulamasını açın**
2. **Ana menüden "Gemini AI"** bölümüne gidin
3. **"Ayarlar" (⚙️) düğmesine tıklayın**
4. Açılan ekranda **API anahtarını yapıştırın**
5. **"Kaydet" düğmesine tıklayın**

Artık Gemini AI'ı kullanabilirsiniz!

### Seçenek 2: Derleme Sırasında (Geliştiriciler İçin)

Eğer uygulamayı kendiniz derlemek istiyorsanız:

1. Proje kök klasöründeki `local.properties` dosyasını açın
2. Şu satırı ekleyin:
```properties
GEMINI_API_KEY=AIzaSyBoVtEtgl6-cgdgg7GpsS_6I1iYcC_e2HA
```
3. Dosyayı kaydedin
4. Uygulamayı derleyin

---

## Gemini AI Özelliği Kullanma

### Gemini AI Ekranı

1. **Ana menüye gidin**
2. **"Gemini AI Asistanı"** bölümüne tıklayın
3. **Sorunuzu yazın** (örnek sorular):
   - "Aylık harcamalarımda tasarruf yapabilir miyim?"
   - "Borç yönetimi hakkında tavsiye ver"
   - "Bütçe oluşturmada yardım et"
4. **"Yanıt Al" düğmesine tıklayın**
5. **AI'ın yanıtını okuyun**

### Örnek Sorular

- "Borcumu nasıl hızlı ödeyebilirim?"
- "Finansal bütçe hazırlamada tavsiye ver"
- "Kredi kartı kullanımında en iyi uygulamalar nelerdir?"
- "Tasarruf yapmak için ipuçları ver"
- "Aylık harcama takibi nasıl yapılmalı?"

---

## Play Store'da Dağıtım

Uygulamayı Play Store'da yayınlarken:

### API Anahtarı Kısıtlamaları Ayarlama (ÖNEMLİ)

Güvenlik için API anahtarınıza kısıtlamalar ekleyin:

1. **Google Cloud Console**'a gidin: https://console.cloud.google.com
2. **Credentials (Kimlik Bilgileri)** menüsüne gidin
3. Oluşturduğunuz API key'e tıklayın
4. **"Application restrictions"** bölümünde:
   - **"Android apps"** seçin
   - **SHA-1 Fingerprint** ekleyin (Release keystore'unuzdan)
5. **"API restrictions"** bölümünde:
   - Sadece **"Generative Language API"** seçin

### SHA-1 Fingerprint Bulma

Release APK'yı imzaladığınız keystore için:

```bash
keytool -list -v -keystore "release-key.keystore" -alias androidreleasekey | findstr "SHA1"
```

### AndroidManifest.xml Kontrolü

Uygulamanız zaten gerekli izinlere sahip olmalı, ancak kontrol edin:

```xml
<!-- İnternet erişimi (zaten mevcut) -->
<uses-permission android:name="android.permission.INTERNET" />
```

---

## Sorun Giderme

### Problem: "API Anahtarı Eksik" Hatası

**Çözüm:**
1. Ayarlar > Gemini Ayarları'na gidin
2. API anahtarınızı doğru şekilde yapıştırın
3. İşletim sistemini yeniden başlatın

### Problem: "Requests from this Android client application are blocked"

**Çözüm:**
1. Google Cloud Console'da API key kısıtlamalarını kontrol edin
2. SHA-1 Fingerprint'in doğru olduğundan emin olun
3. Android apps kısıtlamasını ekleyin
4. Generative Language API'sini etkinleştirin

### Problem: Yanıt Almama

**Çözüm:**
1. İnternet bağlantısını kontrol edin
2. API anahtarının geçerli olduğundan emin olun
3. Google API Console'da API kullanım limitini kontrol edin
4. Sorunuzun çok uzun olmadığından emin olun

---

## Sık Sorulan Sorular (FAQ)

### S: API anahtarı ücretsiz mi?
**C:** Evet, Google AI Studio ile oluşturduğunuz API anahtarları ücretsizdir. Ancak, yüksek kullanım durumunda fiyatlandırma uygulanabilir. Detaylı bilgi için: https://ai.google.dev/pricing

### S: API anahtarının süresi sonu mu olur?
**C:** Hayır, süresi dolmaz. Ancak, ihlal tespit edilirse Google tarafından silinebilir.

### S: API anahtarını sıfırlamak mümkün mü?
**C:** Evet, Google AI Studio'da API key'i silebilir ve yenisini oluşturabilirsiniz.

### S: Play Store'da yayınlarken API anahtarımı gizli tutabilir miyim?
**C:** Hayır, Android uygulamalarında API anahtarları paketlenmiştir. Bu yüzden kısıtlamalar eklemeniz çok önemlidir.

### S: Birden fazla API anahtarı kullanabilir miyim?
**C:** Evet, uygulamayı ayarlar kısmından farklı anahtarlara geçirebilirsiniz.

---

## İletişim ve Destek

Sorun yaşarsanız:
1. Logcat'i kontrol edin (`adb logcat | grep "Gemini"`)
2. Hata mesajını Google'da arayın
3. Google AI Studio belgelerine bakın: https://ai.google.dev/docs

---

**Son Güncelleme:** Aralık 2025
**Uygulamada Entegre Gemini AI Sürümü:** gemini-2.0-flash
