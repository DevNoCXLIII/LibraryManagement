package com.schwisolutions.librarymanagement.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorageHelper {
    private const val TAG = "ImageStorageHelper"
    private const val DIRECTORY_NAME = "book_images"

    fun saveBitmap(context: Context, bitmap: Bitmap): String? {
        return try {
            val directory = File(context.filesDir, DIRECTORY_NAME)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, "book_${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving bitmap", e)
            null
        }
    }

    fun saveUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    saveBitmap(context, bitmap)
                } else {
                    Log.e(TAG, "Failed to decode bitmap from URI")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving URI: $uri", e)
            null
        }
    }

    fun createTempImageFile(context: Context): File {
        val directory = File(context.filesDir, DIRECTORY_NAME)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return File(directory, "book_${UUID.randomUUID()}.jpg")
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun deleteImage(path: String?) {
        if (path.isNullOrBlank()) return
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image at path: $path", e)
        }
    }
}
