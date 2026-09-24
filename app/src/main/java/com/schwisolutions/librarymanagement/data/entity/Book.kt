package com.schwisolutions.librarymanagement.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing the 'book' table in the SQLite database.
 *
 * Official docs reference:
 * - developer.android.com/training/data-storage/room/defining-data
 *
 * @param bookId Auto-generated primary key. Use 0 when instantiating new records for insertion.
 * @param title Title of the book.
 * @param author Author of the book.
 * @param releaseDate Publication year or release date string.
 * @param genre Category or genre of the book.
 * @param imagePath Absolute path to the cover image stored in internal storage, or null if unset.
 */
@Entity(tableName = "book")
data class Book(
    @PrimaryKey(autoGenerate = true)
    val bookId: Int = 0,
    val title: String,
    val author: String,
    val releaseDate: String,
    val genre: String,
    val imagePath: String? = null
)