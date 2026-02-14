package com.burhan2855.personeltakip.shared.adapters

import com.burhan2855.personeltakip.shared.util.ISettings
import kotlinx.browser.localStorage

class WebSettings : ISettings {
    override fun isPasswordSet(): Boolean {
        return localStorage.getItem("app_password") != null
    }

    override fun getPassword(): String? {
        val pass = localStorage.getItem("app_password")
        println("WebSettings: Stored password is '$pass'") // Debug log
        return pass
    }

    override fun savePassword(password: String) {
        localStorage.setItem("app_password", password)
    }

    override fun clearPassword() {
        localStorage.removeItem("app_password")
    }
}
