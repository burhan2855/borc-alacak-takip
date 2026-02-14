package com.burhan2855.personeltakip.shared.adapters

import com.burhan2855.personeltakip.util.PreferenceManager
import com.burhan2855.personeltakip.shared.util.ISettings

class AndroidSettings(private val preferenceManager: PreferenceManager) : ISettings {
    override fun isPasswordSet(): Boolean {
        return preferenceManager.isPasswordSet()
    }

    override fun getPassword(): String? {
        return preferenceManager.getPassword()
    }

    override fun savePassword(password: String) {
        preferenceManager.savePassword(password)
    }

    override fun clearPassword() {
        preferenceManager.clearPassword()
    }
}
