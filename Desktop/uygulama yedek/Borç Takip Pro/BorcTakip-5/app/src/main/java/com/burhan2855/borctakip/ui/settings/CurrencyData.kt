package com.burhan2855.borctakip.ui.settings

import java.util.Currency

data class CurrencyData(val code: String, val name: String, val symbol: String, val flag: String)

fun getFlagEmojiForCurrency(currencyCode: String): String {
    val countryCode = try {
        // Handle special cases where currency code doesn't map to a country
        if (currencyCode == "EUR") return "🇪🇺"
        Currency.getInstance(currencyCode).currencyCode.substring(0, 2)
    } catch (e: Exception) {
        return "🏳️"
    }

    // Safety check for the country code
    if (countryCode.length < 2 || !countryCode.all { it.isLetter() }) {
        return "🏳️"
    }

    val firstLetter = countryCode[0].uppercaseChar().code - 'A'.code + 0x1F1E6
    val secondLetter = countryCode[1].uppercaseChar().code - 'A'.code + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}
