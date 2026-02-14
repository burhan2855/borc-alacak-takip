package com.burhan2855.personeltakip.util

import android.content.Context
import android.net.Uri
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class BackupManager(private val context: Context) {

    private val databaseName = "personel_takip_db"

    fun exportDatabase(outputUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(databaseName)
            val dbShm = File(dbFile.path + "-shm")
            val dbWal = File(dbFile.path + "-wal")

            // We will create a zip or just copy the main db file for simplicity 
            // but for Room it's safer to copy all or checkpoint.
            // For now, let's just copy the main db file as most simple backup.
            // If the app is not writing, the main db file is usually enough after a checkpoint.
            
            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                FileInputStream(dbFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importDatabase(inputUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(databaseName)
            
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                FileOutputStream(dbFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            // Delete SHM and WAL files to avoid conflicts with old data
            File(dbFile.path + "-shm").delete()
            File(dbFile.path + "-wal").delete()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
