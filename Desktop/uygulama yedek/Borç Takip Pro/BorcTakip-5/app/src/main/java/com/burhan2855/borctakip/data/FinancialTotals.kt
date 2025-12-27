package com.burhan2855.borctakip.data

/**
 * PDF raporlarında genel finansal durum özetini temsil eden veri modeli
 */
data class FinancialTotals(
    val cashBalance: Double,
    val bankBalance: Double,
    val unpaidDebts: Double,
    val unpaidReceivables: Double,
    val netWorth: Double
) {
    companion object {
        /**
         * İşlem listesinden FinancialTotals nesnesi oluşturur
         */
        fun fromTransactions(transactions: List<Transaction>): FinancialTotals {
            val cashBalance = transactions.filter { it.category == "Kasa Girişi" }.sumOf { it.amount.toDouble() } -
                             transactions.filter { it.category == "Kasa Çıkışı" }.sumOf { it.amount.toDouble() }
            
            val bankBalance = transactions.filter { it.category == "Banka Girişi" }.sumOf { it.amount.toDouble() } -
                             transactions.filter { it.category == "Banka Çıkışı" }.sumOf { it.amount.toDouble() }
            
            val unpaidDebts = transactions.filter { it.isDebt && it.status != "Ödendi" }.sumOf { it.amount.toDouble() }
            
            val unpaidReceivables = transactions.filter { !it.isDebt && it.status != "Ödendi" }.sumOf { it.amount.toDouble() }
            
            val netWorth = cashBalance + bankBalance + unpaidReceivables - unpaidDebts
            
            return FinancialTotals(
                cashBalance = cashBalance,
                bankBalance = bankBalance,
                unpaidDebts = unpaidDebts,
                unpaidReceivables = unpaidReceivables,
                netWorth = netWorth
            )
        }
    }
}