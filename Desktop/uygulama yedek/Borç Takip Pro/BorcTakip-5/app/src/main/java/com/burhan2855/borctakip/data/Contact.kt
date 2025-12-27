package com.burhan2855.borctakip.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@Entity(tableName = "contacts")
@IgnoreExtraProperties
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val documentId: String? = null,
    val balance: Double = 0.0,
    val lastUpdated: Long? = null, // Changed from Date to Long (timestamp)
    val isSynced: Boolean = false
) {
    // No-argument constructor required by Firestore
    constructor() : this(0, "", null, 0.0, null, false)
}