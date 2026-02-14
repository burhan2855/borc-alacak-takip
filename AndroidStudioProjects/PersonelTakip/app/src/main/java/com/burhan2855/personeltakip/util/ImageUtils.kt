package com.burhan2855.personeltakip.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageUtils {
    /**
     * Copies an image from a URI to the app's internal storage.
     * Returns the absolute path of the saved file or null if failed.
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val directory = File(context.filesDir, "personnel_images")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val file = File(directory, fileName)
            
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            
            inputStream.close()
            outputStream.close()
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
