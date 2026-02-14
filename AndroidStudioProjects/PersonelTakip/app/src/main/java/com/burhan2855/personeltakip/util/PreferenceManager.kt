package com.burhan2855.personeltakip.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("personel_takip_prefs", Context.MODE_PRIVATE)

    fun savePassword(password: String) {
        sharedPreferences.edit().putString("app_password", password).apply()
    }
    
    fun clearPassword() {
        sharedPreferences.edit().remove("app_password").apply()
    }

    fun getPassword(): String? {
        return sharedPreferences.getString("app_password", null)
    }

    fun isPasswordSet(): Boolean {
        return getPassword() != null
    }

    fun setFirstRun(isFirstRun: Boolean) {
        sharedPreferences.edit().putBoolean("is_first_run", isFirstRun).apply()
    }

    fun isFirstRun(): Boolean {
        return sharedPreferences.getBoolean("is_first_run", true)
    }
}
