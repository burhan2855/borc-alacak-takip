package com.burhan2855.borctakip.util

import android.content.Context
import com.burhan2855.borctakip.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gemini API ile iletişim kurmak için servis sınıfı
 * API anahtarı local.properties'den okunur ve BuildConfig'te saklanır
 */
object GeminiService {
    
    private var generativeModel: GenerativeModel? = null
    
    /**
     * GenerativeModel'i başlat (lazy initialization)
     */
    fun initialize(apiKey: String? = null) {
        val key = apiKey ?: BuildConfig.GEMINI_API_KEY
        if (key.isNotBlank()) {
            generativeModel = GenerativeModel(
                modelName = "gemini-2.0-flash",  // En yeni ve kararlı model
                apiKey = key
            )
        }
    }
    
    /**
     * Metin tabanlı sorguya yanıt al
     */
    suspend fun generateContent(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                initialize()
            }
            
            val response = generativeModel!!.generateContent(prompt)
            response.text ?: "Yanıt alınamadı"
        } catch (e: Exception) {
            "Hata: ${e.message}"
        }
    }
    
    /**
     * Metin + resim tabanlı sorguya yanıt al
     */
    suspend fun generateContentWithImage(
        prompt: String,
        imageParts: List<Any>
    ): String = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                initialize()
            }
            
            val response = generativeModel!!.generateContent(prompt)
            response.text ?: "Yanıt alınamadı"
        } catch (e: Exception) {
            "Hata: ${e.message}"
        }
    }
    
    /**
     * Chat tabanlı konuşma (multi-turn)
     */
    fun startChat(): Any {
        if (generativeModel == null) {
            initialize()
        }
        return generativeModel!!.startChat()
    }
}
