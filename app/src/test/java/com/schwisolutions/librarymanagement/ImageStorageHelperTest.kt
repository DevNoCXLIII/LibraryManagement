package com.schwisolutions.librarymanagement

import com.schwisolutions.librarymanagement.data.ImageStorageHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Local JVM unit tests for ImageStorageHelper.
 *
 * Official docs reference:
 * - developer.android.com/training/testing/local-tests
 */
class ImageStorageHelperTest {

    @Test
    fun deleteImage_nullOrBlankPath_doesNotThrow() {
        ImageStorageHelper.deleteImage(null)
        ImageStorageHelper.deleteImage("")
        ImageStorageHelper.deleteImage("   ")
    }

    @Test
    fun deleteImage_existingFile_deletesSuccessfully() {
        val tempFile = File.createTempFile("test_book_", ".jpg")
        assertTrue(tempFile.exists())

        ImageStorageHelper.deleteImage(tempFile.absolutePath)

        assertFalse(tempFile.exists())
    }
}
