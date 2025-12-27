# Android Studio'da API Anahtar Sistemi - Kapsamlı Rehber

## 🎯 Sorun Özeti
Gördüğünüz hatalar:
- ❌ "Requests from this Android client application <empty> are blocked"
- ❌ "Model bulunamadı (gemini-1.5-flash)"
- ❌ "API keys are not supported by this API"

## ✅ Çözüm: Adım Adım

### 1. Google Cloud Console'de Doğru Ayarlar

#### A. API Anahtarı Oluştur
1. **Google Cloud Console** → https://console.cloud.google.com
2. **Projeler** → Proje seçin (varsa) veya yeni oluştur
3. **API'ler ve Hizmetler** → **Kimlik Bilgileri**
4. **Kimlik Bilgisi Oluştur** → **API Anahtarı**
5. **API Anahtarını Kopyala**

#### B. API Kısıtlamalarını Ayarla
1. Oluşturulan anahtarı **Düzenle** butonuyla aç
2. **Temel Bilgiler** bölümünde isim değiştir: `Android Borç Takip`
3. **Uygulama Kısıtlamaları**:
   - ✅ **Android uygulamaları** seç
   - Paket adı: `com.burhan2855.borctakip`
   - SHA-1 parmak izi: `6C:A5:38:94:61:1D:C6:0C:84:95:64:CF:4E:81:69:6A:34:D5:B6:8C`

4. **API Kısıtlamaları**:
   - ✅ **Kısıtlanmış anahtar** seç
   - ✅ **Generative Language API** seç
   - (Dikkat: Vertex AI API DEĞİL, Generative Language API olmalı)

5. **Kaydet**

### 2. Android Studio Yapılandırması

#### A. local.properties Dosyası
```ini
sdk.dir=C:\\Users\\<username>\\AppData\\Local\\Android\\Sdk
GEMINI_API_KEY=AIzaSy[YOUR_KEY_HERE]
```

#### B. build.gradle.kts
```kotlin
// local.properties'den API anahtarını oku
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY", "")

android {
    // ... diğer ayarlar ...
    
    defaultConfig {
        // ... diğer konfigürasyon ...
        
        // BuildConfig'te API anahtarını sakla (ProGuard'dan korunan)
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
    }
    
    buildFeatures {
        buildConfig = true  // BuildConfig sınıfını etkinleştir
    }
}

dependencies {
    // Google Generative AI SDK
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
}
```

#### C. AndroidManifest.xml
```xml
<!-- API anahtarını manifest'te de tanımla (opsiyonel, daha güvenli) -->
<application>
    <meta-data
        android:name="com.google.ai.mobile.EMBEDDED_API_KEY"
        android:value="AIzaSy[YOUR_KEY]" />
    
    <!-- ... diğer bileşenler ... -->
</application>
```

### 3. Kotlin Kodunda Kullanım

#### A. Service Sınıfı (GeminiService.kt)
```kotlin
import com.burhan2855.borctakip.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

object GeminiService {
    private var generativeModel: GenerativeModel? = null
    
    fun initialize() {
        if (generativeModel == null) {
            generativeModel = GenerativeModel(
                modelName = "gemini-2.0-flash",  // ✅ En yeni model
                apiKey = BuildConfig.GEMINI_API_KEY  // ✅ BuildConfig'ten oku
            )
        }
    }
    
    suspend fun generateContent(prompt: String): String {
        if (generativeModel == null) initialize()
        return try {
            val response = generativeModel!!.generateContent(prompt)
            response.text ?: "Yanıt bulunamadı"
        } catch (e: Exception) {
            "Hata: ${e.message}"
        }
    }
}
```

#### B. ViewModel'de Kullanım
```kotlin
class GeminiViewModel : ViewModel() {
    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse
    
    init {
        GeminiService.initialize()
    }
    
    fun askGemini(question: String) {
        viewModelScope.launch {
            val response = GeminiService.generateContent(question)
            _aiResponse.value = response
        }
    }
}
```

#### C. Compose UI'de Kullanım
```kotlin
@Composable
fun ChatScreen(viewModel: GeminiViewModel = viewModel()) {
    var input by remember { mutableStateOf("") }
    val response by viewModel.aiResponse.collectAsState()
    
    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Soru sor") }
        )
        
        Button(onClick = { viewModel.askGemini(input) }) {
            Text("Yanıt Al")
        }
        
        if (response.isNotEmpty()) {
            Text(response)
        }
    }
}
```

### 4. ProGuard Kuralları

```ini
# app/proguard-rules.pro

# Gemini SDK'yı koruma
-keep class com.google.ai.client.generativeai.** { *; }
-keepclassmembers class com.google.ai.client.generativeai.** {
    public <init>();
    public <fields>;
    public <methods>;
}

# BuildConfig sınıfını koruma
-keep class com.burhan2855.borctakip.BuildConfig { *; }
```

### 5. Hata Çözümleri

#### ❌ "Requests from this Android client application are blocked"
**Çözüm:**
1. SHA-1 parmak izini doğru gir (console.cloud.google.com'de)
2. API anahtarında Android kısıtlaması seçili olmalı
3. Paket adı doğru olmalı: `com.burhan2855.borctakip`

Parmak izi kontrol:
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" `
  -alias androiddebugkey -storepass android -keypass android | findstr "SHA1"
```

#### ❌ "Model not found: gemini-1.5-flash"
**Çözüm:**
- `gemini-2.0-flash` kullan (daha yeni ve desteklenen)
- Alternatif: `gemini-1.5-pro`, `gemini-1.5-flash-latest`

#### ❌ "Expected OAuth2 access token"
**Çözüm:**
- API anahtarı değil, OAuth2 token gerekli
- Service account kullanman gerekebilir
- Generative Language API (Vertex AI değil)

#### ❌ "API is not enabled"
**Çözüm:**
1. Console → API'ler ve Hizmetler → Hizmetleri Etkinleştir
2. "Generative Language API" arayıp etkinleştir

### 6. Güvenlik İpuçları

✅ **Yapılması Gerekenler:**
- API anahtarını `local.properties`'de sakla
- `local.properties`'i `.gitignore`'a ekle
- BuildConfig üzerinden oku (derleme zamanında)
- Release derleme için ayrı anahtar kullan

❌ **Yapılmaması Gerekenler:**
- API anahtarını kaynak koda embed etme
- GitHub'a API anahtarı yükleme
- Prod'da debug anahtarı kullanma

### 7. Test Etme

```kotlin
// MainActivity'de test
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    lifecycleScope.launch {
        val result = GeminiService.generateContent("Merhaba!")
        Log.d("Gemini", "Yanıt: $result")
    }
}
```

Logcat'te "Yanıt: ..." görmeli.

## 📚 Kaynaklar
- [Google AI SDK Documentation](https://ai.google.dev/tutorials/kotlin)
- [Generative Language API](https://developers.google.com/generative-ai)
- [Android Security Best Practices](https://developer.android.com/training/articles/security-key-attestation)

## 🔧 Sorun Giderme Checklist

- [ ] API anahtarı oluşturuldu
- [ ] Generative Language API etkinleştirildi (Vertex AI değil)
- [ ] Android kısıtlaması ayarlandı
- [ ] SHA-1 parmak izi doğru girildi
- [ ] Paket adı doğru: `com.burhan2855.borctakip`
- [ ] local.properties'de GEMINI_API_KEY var
- [ ] BuildConfig.GEMINI_API_KEY kod içinde kullanıldı
- [ ] `gemini-2.0-flash` modeli seçildi
- [ ] ProGuard kuralları eklendi
- [ ] Internet permission AndroidManifest'te var

---
**Son Güncelleme:** 27 Aralık 2025
