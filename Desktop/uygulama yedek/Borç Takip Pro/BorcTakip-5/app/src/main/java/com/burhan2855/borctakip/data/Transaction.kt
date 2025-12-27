package com.burhan2855.borctakip.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@Entity(tableName = "transactions")
@IgnoreExtraProperties
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Int? = null,
    val amount: Double = 0.0,
    val type: String = "", // "debt", "credit", "payment_in", "payment_out"
    val description: String = "",
    val date: Long? = System.currentTimeMillis(), // Changed from Date to Long (timestamp)
    val dueDate: Long? = null, // Changed from Date to Long (timestamp)
    val isPaid: Boolean = false,
    val isSynced: Boolean = false,
    val category: String? = null,
    val title: String = "",
    val isDebt: Boolean = true,
    val status: String = "Ödenmedi",
    val remainingAmount: Double = 0.0,
    val documentId: String? = null,
    val paymentType: String = "",
    val transactionDate: Long? = System.currentTimeMillis() // Changed from Date to Long (timestamp)
) {
    // No-argument constructor required by Firestore
    constructor() : this(0, null, 0.0, "", "", System.currentTimeMillis(), null, false, false, null, "", true, "Ödenmedi", 0.0, null, "", System.currentTimeMillis())
}