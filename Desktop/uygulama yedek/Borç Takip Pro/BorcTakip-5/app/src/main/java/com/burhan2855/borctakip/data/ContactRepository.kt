package com.burhan2855.borctakip.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class ContactRepository(private val contactDao: ContactDao) {

    private val firestore = Firebase.firestore
    private val contactsCollection = firestore.collection("contacts")
    private val auth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(Dispatchers.IO)
    private var listenerRegistration: ListenerRegistration? = null

    fun getContactsCollection() = auth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("contacts")
    }

    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()

    fun startListeningForChanges() {
        Log.d("ContactRepo", "startListeningForChanges called")
        listenerRegistration?.remove()
        val currentUserId = auth.currentUser?.uid
        Log.d("ContactRepo", "Current user ID: $currentUserId")
        
        currentUserId?.let {
            try {
                val collection = getContactsCollection()
                Log.d("ContactRepo", "Contacts collection: $collection")
                
                listenerRegistration = collection?.orderBy("name", Query.Direction.ASCENDING)
                    ?.addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("ContactRepo", "Firestore listener error: ${error.message}", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            Log.d("ContactRepo", "Received ${snapshot.size()} contacts from Firestore")
                            try {
                                val contacts = mutableListOf<Contact>()
                                for (document in snapshot.documents) {
                                    try {
                                        val contact = document.toObject(Contact::class.java)
                                        if (contact != null) {
                                            val updatedContact = contact.copy(documentId = document.id)
                                            contacts.add(updatedContact)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("ContactRepo", "Error deserializing individual contact: ${e.message}", e)
                                        // Skip this document and continue with others
                                    }
                                }
                                
                                scope.launch {
                                    if (contacts.isNotEmpty()) {
                                        contactDao.syncContacts(contacts)
                                        Log.d("ContactRepo", "Synced ${contacts.size} contacts to local DB")
                                    } else {
                                        Log.d("ContactRepo", "No valid contacts to sync")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("ContactRepo", "Error processing Firestore contacts: ${e.message}", e)
                                // Continue with offline mode - contacts can still be accessed from local DB
                            }
                        }
                    }
                Log.d("ContactRepo", "Firestore listener registered successfully for contacts")
            } catch (e: Exception) {
                Log.e("ContactRepo", "Failed to start Firestore listener: ${e.message}", e)
            }
        } ?: Log.w("ContactRepo", "Cannot start listener - user not signed in")
    }

    fun stopListeningForChanges() {
        Log.d("ContactRepo", "stopListeningForChanges called")
        listenerRegistration?.remove()
    }

    suspend fun insert(contact: Contact) {
        val id = contactDao.insertContact(contact)
        val userCollection = getContactsCollection()
        val documentId = contact.documentId ?: userCollection?.document()?.id ?: contactsCollection.document().id
        val newContact = contact.copy(id = id, documentId = documentId, isSynced = true)
        contactDao.updateContact(newContact)
        
        // Kullanıcıya özel koleksiyona kaydet
        userCollection?.document(documentId)?.set(newContact)?.await()
            ?: Log.w("ContactRepo", "User not logged in, contact not synced to Firestore")
    }

    suspend fun update(contact: Contact) {
        contactDao.updateContact(contact)
        val userCollection = getContactsCollection()
        contact.documentId?.let { docId ->
            userCollection?.document(docId)?.set(contact)?.await()
                ?: Log.w("ContactRepo", "User not logged in, contact update not synced")
        }
    }

    suspend fun clearContact(contact: Contact) {
        withContext(Dispatchers.IO) {
            contactDao.deleteContact(contact)
        }
    }

    suspend fun clearAllLocalData() {
        withContext(Dispatchers.IO) {
            contactDao.deleteAllContacts()
        }
    }

    suspend fun syncContacts() {
        try {
            val userCollection = getContactsCollection()
            if (userCollection == null) {
                Log.w("ContactRepo", "User not logged in, cannot sync contacts")
                return
            }
            
            val snapshot = userCollection.get().await()
            val firestoreContacts = snapshot.toObjects(Contact::class.java)
            Log.d("ContactRepo", "Received ${firestoreContacts.size} contacts from Firestore")
            val localContacts = contactDao.getAllContacts().first()

            for (firestoreContact in firestoreContacts) {
                val localContact = localContacts.find { it.documentId == firestoreContact.documentId }
                if (localContact == null) {
                    contactDao.insertContact(firestoreContact)
                } else {
                    if ((firestoreContact.lastUpdated ?: 0L) > (localContact.lastUpdated ?: 0L)) {
                        contactDao.updateContact(firestoreContact)
                    }
                }
            }
            Log.d("ContactRepo", "Synced ${firestoreContacts.size} contacts to local DB")
        } catch (e: Exception) {
            Log.e("ContactRepo", "Error syncing contacts", e)
        }
    }
}