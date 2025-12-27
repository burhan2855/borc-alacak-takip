package com.burhan2855.borctakip.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    private val oneTapClient: SignInClient = Identity.getSignInClient(context)
    
    private val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId("1040576994070-4o2bout45bfhldrg576e2lcgpja078o9.apps.googleusercontent.com")
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(false)
        .build()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isUserSignedIn(): Boolean = isUserSignedInFromPrefs() || getCurrentUser() != null

    suspend fun signInWithGoogle(launcher: ActivityResultLauncher<IntentSenderRequest>): Boolean {
        return try {
            val result = oneTapClient.beginSignIn(signInRequest).await()
            launcher.launch(
                IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
            )
            true
        } catch (e: Exception) {
            // Google Auth başarısız olursa basit giriş sistemine geri dön
            false
        }
    }
    
    // Firebase email/password ile giriş
    suspend fun signInWithEmailPassword(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            
            if (user != null) {
                // SharedPreferences'a kaydet
                setSignedIn(true, email, user.displayName ?: "Kullanıcı")
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Giriş başarısız")
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("no user record") == true || 
                e.message?.contains("user not found") == true -> 
                    "Bu email ile kayıtlı kullanıcı bulunamadı"
                e.message?.contains("password is invalid") == true || 
                e.message?.contains("wrong password") == true -> 
                    "Şifre hatalı"
                e.message?.contains("network error") == true -> 
                    "İnternet bağlantısı hatası"
                e.message?.contains("too many requests") == true -> 
                    "Çok fazla başarısız deneme. Lütfen daha sonra tekrar deneyin"
                else -> "Giriş başarısız: ${e.message}"
            }
            AuthResult.Error(errorMessage)
        }
    }
    
    // Firebase ile email/password kayıt
    suspend fun signUpWithEmailPassword(name: String, email: String, password: String): Boolean {
        return try {
            // Firebase Authentication ile kullanıcı oluştur
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            
            if (user != null) {
                // Kullanıcı profil bilgilerini güncelle
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdates).await()
                
                // SharedPreferences'a kaydet
                setSignedIn(true, email, name)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            // Hata mesajını daha anlaşılır hale getir
            val errorMessage = when {
                e.message?.contains("email address is already in use") == true -> 
                    "Bu email adresi zaten kullanımda"
                e.message?.contains("network error") == true -> 
                    "İnternet bağlantısı hatası"
                e.message?.contains("password is invalid") == true -> 
                    "Şifre geçersiz"
                else -> e.message ?: "Kayıt başarısız"
            }
            throw Exception(errorMessage)
        }
    }
    
    // Basit giriş sistemi (demo için)
    fun signInWithCredentials(email: String, password: String): Boolean {
        // Demo hesap kontrolü
        return (email == "demo@example.com" && password == "1234") ||
               (email.isNotEmpty() && password.length >= 4)
    }

    suspend fun handleSignInResult(data: Intent?): AuthResult {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(firebaseCredential).await()
                val user = result.user
                
                if (user != null) {
                    AuthResult.Success(user)
                } else {
                    AuthResult.Error("Authentication failed")
                }
            } else {
                AuthResult.Error("No ID token received")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    fun signOut() {
        // Firebase Auth'dan çıkış yap
        auth.signOut()
        
        // Google One Tap'tan çıkış yap
        oneTapClient.signOut()
        
        // SharedPreferences'dan kullanıcı bilgilerini temizle
        setSignedIn(false)
    }
    
    // Basit giriş durumu kontrolü
    fun setSignedIn(isSignedIn: Boolean, email: String = "", name: String = "") {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("is_signed_in", isSignedIn)
            putString("user_email", email)
            putString("user_name", name)
            apply()
        }
    }
    
    fun isUserSignedInFromPrefs(): Boolean {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_signed_in", false)
    }
    
    fun getUserEmailFromPrefs(): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_email", null)
    }
    
    fun getUserDisplayNameFromPrefs(): String? {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return prefs.getString("user_name", null)
    }

    fun getUserDisplayName(): String? = getUserDisplayNameFromPrefs() ?: getCurrentUser()?.displayName
    fun getUserEmail(): String? = getUserEmailFromPrefs() ?: getCurrentUser()?.email
    fun getUserPhotoUrl(): String? = getCurrentUser()?.photoUrl?.toString()
}

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}