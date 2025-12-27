package com.burhan2855.borctakip.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long

    @Update
    suspend fun updateContact(contact: Contact)

    @androidx.room.Delete
    suspend fun deleteContact(contact: Contact)

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<Contact>)
    
    @Query("SELECT * FROM contacts WHERE documentId = :documentId LIMIT 1")
    suspend fun getContactByDocumentId(documentId: String): Contact?
}

// Extension function for syncing
suspend fun ContactDao.syncContacts(contacts: List<Contact>) {
    // Merge strategy: upsert Firestore data without wiping local records
    // Filter nulls and blanks - only sync records with valid documentId from Firestore
    val validContacts = contacts.filter { !it.documentId.isNullOrBlank() }
    
    validContacts.forEach { contact ->
        // Check if contact with this documentId already exists
        val existing = getContactByDocumentId(contact.documentId!!)
        if (existing != null) {
            // Update existing contact with proper fields
            val updated = Contact(
                id = existing.id,
                name = contact.name,
                documentId = contact.documentId,
                balance = contact.balance,
                lastUpdated = contact.lastUpdated,
                isSynced = contact.isSynced
            )
            updateContact(updated)
        } else {
            // Insert new contact
            insertContact(contact)
        }
    }
}