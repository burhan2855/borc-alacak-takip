package com.burhan2855.borctakip.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import com.burhan2855.borctakip.BuildConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

data class CopilotMessage(
    val role: String,
    val content: String
)

data class CopilotRequest(
    val messages: List<CopilotMessage>
)

data class CopilotResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

data class Message(
    val content: String
)

class CopilotService(private val context: Context) {
    
    private val apiKey: String = BuildConfig.GITHUB_COPILOT_TOKEN
    private val client = OkHttpClient()
    private val gson = Gson()
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    
    companion object {
        const val TAG = "CopilotService"
        const val COPILOT_API_URL = "https://api.github.com/copilot_chat/completions"
    }
    
    init {
        // Text to Speech başlat
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("tr", "TR")
                Log.d(TAG, "Text to Speech hazır")
            }
        }
    }
    
    /**
     * Sesli komutu metne çevir
     */
    fun startSpeechRecognition(onResult: (String) -> Unit) {
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Komutu söyleyin...")
            }
            
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    Log.e(TAG, "Ses tanıma hatası: $error")
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        onResult(matches[0])
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Ses tanıma başlatma hatası", e)
        }
    }
    
    /**
     * Copilot Chat'e soru sor ve cevap al
     */
    suspend fun askCopilot(question: String): String = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isEmpty() || apiKey == "") {
                return@withContext "Lütfen önce GitHub Personal Access Token'ını ayarlardan girin."
            }
            
            val messages = listOf(
                CopilotMessage("system", "Sen Borç Takip Pro uygulaması için yardımcı bir asistansın. Kullanıcıların borç yönetimi, ödeme takibi ve finansal analizler konusunda yardım etmelisin. Kısa ve öz cevaplar ver."),
                CopilotMessage("user", question)
            )
            
            val request = CopilotRequest(messages)
            val jsonBody = gson.toJson(request)
            
            val httpRequest = Request.Builder()
                .url(COPILOT_API_URL)
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(jsonBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(httpRequest).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrEmpty()) {
                    val copilotResponse = gson.fromJson(body, CopilotResponse::class.java)
                    val answer = copilotResponse.choices.firstOrNull()?.message?.content
                        ?: "Cevap alınamadı"
                    
                    Log.d(TAG, "Copilot Cevap: $answer")
                    return@withContext answer
                }
            } else {
                Log.e(TAG, "API Hatası: ${response.code} - ${response.message}")
                return@withContext "Copilot şu anda kullanılamıyor. Lütfen tekrar deneyin."
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Copilot sorgusu başarısız", e)
            return@withContext "Copilot bağlantı hatası: ${e.message}"
        }
        
        "Cevap alınamadı"
    }
    
    /**
     * Cevabı sesli olarak oku
     */
    fun speakResponse(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }
    
    /**
     * Rapor oluştur (borç özeti)
     */
    suspend fun generateDebtReport(totalDebt: Double, totalCredit: Double): String {
        val prompt = """
            Aşağıdaki borç takip verilerine göre finansal özet oluştur:
            - Toplam Borç: ₺$totalDebt
            - Toplam Alacak: ₺$totalCredit
            - Net Durum: ₺${totalDebt - totalCredit}
            
            Kısa bir finansal tavsiye ve özet yap. Türkçe cevap ver.
        """.trimIndent()
        
        return askCopilot(prompt)
    }
    
    /**
     * Ödeme tavsiyesi al
     */
    suspend fun getPaymentAdvice(debtAmount: Double, monthlyIncome: Double): String {
        val prompt = """
            Borç Miktarı: ₺$debtAmount
            Aylık Gelir: ₺$monthlyIncome
            
            Bu borç ve gelire göre ödeme stratejisi ve finansal tavsiye ver. Türkçe cevap ver.
        """.trimIndent()
        
        return askCopilot(prompt)
    }
    
    /**
     * Bütçe analizi
     */
    suspend fun analyzeBudget(income: Double, expenses: Double): String {
        val prompt = """
            Aylık Gelir: ₺$income
            Aylık Gideri: ₺$expenses
            Tasarruf Oranı: %${((income - expenses) / income * 100).toInt()}
            
            Bu bütçe verilerine göre finansal sağlık analizi ve iyileştirme önerileri sun. Türkçe cevap ver.
        """.trimIndent()
        
        return askCopilot(prompt)
    }
    
    /**
     * Service'i temizle
     */
    fun cleanup() {
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
