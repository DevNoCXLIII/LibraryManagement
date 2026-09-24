package com.schwisolutions.librarymanagement.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Helper object for saving and deleting book cover images in app-internal storage.
 *
 * Why app-internal storage:
 * - Files stored in context.filesDir are private to this application.
 * - No Android runtime storage permissions (READ_MEDIA_IMAGES or READ_EXTERNAL_STORAGE) are required.
 * - Files are automatically removed when the user uninstalls the app.
 *
 * Official docs reference:
 * - developer.android.com/training/data-storage/app-specific#internal-access-files
 * - developer.android.com/reference/android/graphics/BitmapFactory
 */
object ImageStorageHelper {
    private const val TAG = "ImageStorageHelper"
    private const val DIRECTORY_NAME = "book_images"

    /**
     * Compresses and writes a Bitmap to internal storage as a JPEG.
     *
     * @param context Application or UI context used to access internal filesDir.
     * @param bitmap Bitmap to compress and write.
     * @return Absolute file path on disk, or null if writing failed.
     */
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

    /**
     * Reads an image URI from the system gallery, downsamples it to prevent OOM errors,
     * and saves it to internal storage.
     *
     * @param context Context used to resolve contentResolver and internal filesDir.
     * @param uri Content URI from ActivityResultContracts.GetContent().
     * @param maxDimension Maximum width or height in pixels to bound memory allocation.
     * @return Absolute file path on disk, or null if reading or decoding failed.
     */
    fun saveUri(context: Context, uri: Uri, maxDimension: Int = 800): String? {
        return try {
            // First pass: decode bounds only
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            // Calculate power-of-2 inSampleSize
            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            // Second pass: decode sampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                this.inSampleSize = inSampleSize
            }

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
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

    /**
     * Safely decodes an image file into a sampled Bitmap to avoid allocating full-resolution
     * bitmaps on the UI thread during list rendering.
     *
     * @param path Absolute file path of the image.
     * @param reqWidth Target bounding width in pixels.
     * @param reqHeight Target bounding height in pixels.
     * @return Downsampled Bitmap or null if decoding fails.
     */
    fun loadThumbnail(path: String?, reqWidth: Int = 120, reqHeight: Int = 120): Bitmap? {
        if (path.isNullOrBlank()) return null
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(path, options)

            var inSampleSize = 1
            if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            BitmapFactory.decodeFile(path, options)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading thumbnail for path: $path", e)
            null
        }
    }

    /**
     * Deletes an image file from disk.
     *
     * Call this when a book is deleted or when an existing cover image is replaced during editing.
     *
     * @param path Absolute file path of the image to delete.
     */
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
