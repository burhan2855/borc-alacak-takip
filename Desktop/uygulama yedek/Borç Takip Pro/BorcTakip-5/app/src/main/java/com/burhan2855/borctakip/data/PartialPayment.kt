package com.burhan2855.borctakip.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "partial_payments",
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["transactionId"])]
)
class PartialPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long = 0,
    val amount: Double = 0.0,
    val date: Long = 0
) {
    // Parametresiz constructor - Firebase Firestore deserialization için gerekli
    constructor() : this(id = 0, transactionId = 0, amount = 0.0, date = 0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PartialPayment) return false
        
        if (id != other.id) return false
        if (transactionId != other.transactionId) return false
        if (amount != other.amount) return false
        if (date != other.date) return false
        
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + transactionId.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + date.hashCode()
        return result
    }

    override fun toString(): String {
        return "PartialPayment(id=$id, transactionId=$transactionId, amount=$amount, date=$date)"
    }
}

