# Android API Anahtar Sistemi - Hızlı Referans Kılavuzu

## 🚀 10 Dakikalık Kurulum

### 1. API Anahtarı Oluştur (2 dakika)
```bash
1. Google Cloud Console → console.cloud.google.com
2. Projeler → Yeni Proje → "Borç Takip"
3. API'ler ve Hizmetler → Hizmetleri Etkinleştir
4. "Generative Language API" ara → Etkinleştir
5. Kimlik Bilgileri → + Oluştur → API Anahtarı
6. Anahtarı kopyala
```

### 2. Android Studio Yapılandır (3 dakika)
```ini
# 1. local.properties dosyası (proje kökü)
GEMINI_API_KEY=AIzaSy[SENIN_ANAHTARIN]

# 2. build.gradle.kts
dependencies {
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
}

defaultConfig {
    buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
}

buildFeatures {
    buildConfig = true
}
```

### 3. Kotlin Kodu Yaz (3 dakika)
```kotlin
// GeminiService.kt
import com.google.ai.client.generativeai.GenerativeModel
import com.burhan2855.borctakip.BuildConfig

object GeminiService {
    private var model: GenerativeModel? = null
    
    fun initialize() {
        model = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    }
    
    suspend fun ask(prompt: String): String {
        return model?.generateContent(prompt)?.text ?: "Hata"
    }
}

// MainActivity.kt
lifecycleScope.launch {
    val response = GeminiService.ask("Merhaba!")
    Log.d("Gemini", response)
}
```

### 4. Derle ve Test Et (2 dakika)
```powershell
./gradlew clean :app:assembleDebug
# Cihaza kur ve test et
```

---

## 📚 Dosya Yapısı

```
project/
├── local.properties           ← API Key buraya
├── app/
│   ├── build.gradle.kts       ← Dependency ve Config
│   ├── src/main/
│   │   ├── AndroidManifest.xml ← Internet Permission
│   │   └── java/
│   │       └── util/
│   │           ├── GeminiService.kt    ← API Service
│   │           └── GeminiViewModel.kt  ← UI Logic
│   └── proguard-rules.pro     ← API Protection
└── GEMINI_API_SISTEM_REHBERI.md ← Bu dosya
```

---

## 🔧 Kod Snippet'leri

### Örnek 1: Basit Sorgu
```kotlin
class ChatViewModel : ViewModel() {
    private val _response = MutableStateFlow("")
    
    fun ask(question: String) {
        viewModelScope.launch {
            try {
                val response = GeminiService.generateContent(question)
                _response.value = response
            } catch (e: Exception) {
                _response.value = "Hata: ${e.message}"
            }
        }
    }
}
```

### Örnek 2: Chat (Multi-turn)
```kotlin
// Başla
val chat = GeminiService.startChat()

// 1. İlk sordu
var response = chat.sendMessage("Bana matematikle yardımcı olabilir misin?")

// 2. Follow-up
response = chat.sendMessage("2 + 2 kaç eder?")
// Yanıt: "4 eder"

// Chat context korunur
response = chat.sendMessage("Ya 3 + 3?")
// Yanıt: "6 eder"
```

### Örnek 3: Resim ile Sorgu
```kotlin
val generativeModel = GenerativeModel(
    modelName = "gemini-2.0-flash",
    apiKey = BuildConfig.GEMINI_API_KEY,
)

val image = File("path/to/image.jpg")
val response = generativeModel.generateContent(
    content(
        image,
        "Bu resimde ne var? Açıkla."
    )
)
```

### Örnek 4: Compose UI
```kotlin
@Composable
fun GeminiChat() {
    var input by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    val viewModel: GeminiViewModel = viewModel()
    
    Column(Modifier.padding(16.dp)) {
        TextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Soru sor") }
        )
        
        Button(onClick = { viewModel.ask(input) }) {
            Text("Yanıt Al")
        }
        
        if (response.isNotEmpty()) {
            Text(response, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
```

---

## ❌ Yaygın Hatalar & Düzeltmeler

| Hata | Neden | Çözüm |
|------|-------|--------|
| "Requests blocked" | SHA-1 yanlış | `keytool` ile kontrol et |
| "Model not found" | Eski model adı | `gemini-2.0-flash` kullan |
| "API key empty" | local.properties boş | `GEMINI_API_KEY=...` ekle |
| "401 Unauthorized" | API etkinleştirilmemiş | Console'da etkinleştir |
| "404 Not found" | Yanlış API seçildi | Generative Language API seç |

---

## 🔐 Güvenlik Checklist

- [ ] API Key **hardcoded değil** (BuildConfig'ten oku)
- [ ] local.properties `.gitignore`'da
- [ ] ProGuard kuralları eklendi
- [ ] AndroidManifest'te INTERNET permission var
- [ ] Release build için ayrı anahtar

---

## 📊 API Quota Kontrol

```
Google Cloud Console:
1. Billing → Overview
2. "Raporlar ve İstatistikler" → "Detaylı Kullanım"
3. "Generative Language API" filtresi
4. Günlük quota: 100,000 free calls
```

---

## 🧪 Hızlı Test

```kotlin
// MainActivity.kt
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    lifecycleScope.launch {
        GeminiService.initialize()
        
        // Test 1: Basit Sorgu
        val test1 = GeminiService.generateContent("Merhaba!")
        Log.d("TEST1", test1)
        
        // Test 2: Türkçe Sorgu
        val test2 = GeminiService.generateContent("Android nedir?")
        Log.d("TEST2", test2)
        
        // Test 3: Matematiksel
        val test3 = GeminiService.generateContent("10 + 5 = ?")
        Log.d("TEST3", test3)
    }
}
```

Logcat'te yanıtları görmeli.

---

## 📱 Cihazda Test

```powershell
# Debug apk kur
adb install app/build/outputs/apk/debug/app-debug.apk

# Logcat'i takip et
adb logcat | findstr "GenerativeAI|GEMINI|GeminiService"

# Uygulamayı çalıştır ve soru sor
```

---

## 💰 Maliyet Bilgisi

| Model | Giriş | Çıkış |
|-------|-------|-------|
| gemini-2.0-flash | $0.075 per M | $0.3 per M |
| gemini-1.5-pro | $1.25 per M | $5 per M |
| gemini-1.5-flash | $0.075 per M | $0.3 per M |

**Free Tier**: 100,000 calls/day

---

## 🔗 Yararlı Linkler

- [Google AI for Android](https://ai.google.dev/tutorials/kotlin)
- [Generative Language API](https://ai.google.dev/docs)
- [Cloud Console](https://console.cloud.google.com)
- [API Reference](https://ai.google.dev/api/rest/google.ai.generativelanguage.v1)

---

## 🎯 Adım Adım Video Rehberi

### Video 1: API Anahtarı Oluştur (2 dakika)
```
1. console.cloud.google.com aç
2. Yeni Proje oluştur
3. Generative Language API etkinleştir
4. API Anahtarı oluştur
```

### Video 2: Android Studio Entegrasyonu (5 dakika)
```
1. local.properties güncelle
2. build.gradle.kts dependency ekle
3. GeminiService.kt oluştur
4. ViewModel entegrasyonu
```

### Video 3: Compose UI (3 dakika)
```
1. GeminiAIScreen.kt oluştur
2. State Management
3. Error Handling
```

---

## ❓ FAQ

**S: API Key'i nereden alabilirim?**
A: console.cloud.google.com → Kimlik Bilgileri → API Anahtarı

**S: Local.properties'i Git'e yüklemelimi?**
A: HAYIR! `.gitignore`'a ekle

**S: Kaç API çağrısı yapabilirim?**
A: Free Tier: 100,000/gün. Paid: Sınırsız (faturalandırılır)

**S: Off-line çalışır mı?**
A: HAYIR. Internet gerekli.

**S: VPN ile çalışır mı?**
A: Evet, ama API anahtarının sınırlandırılmış olması gerekir.

**S: API Key expired olur mu?**
A: HAYIR, süresi sınırsız. Ama şifre sıfırlanırsa iptal edilmelidir.

---

**Güncelleme Tarihi:** 27 Aralık 2025
**Sürüm:** 1.0
**Dil:** Türkçe
