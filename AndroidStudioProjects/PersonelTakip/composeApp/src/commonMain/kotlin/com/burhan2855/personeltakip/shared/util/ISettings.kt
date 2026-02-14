package com.burhan2855.personeltakip.shared.util

interface ISettings {
    fun isPasswordSet(): Boolean
    fun getPassword(): String?
    fun savePassword(password: String)
    fun clearPassword()
}
