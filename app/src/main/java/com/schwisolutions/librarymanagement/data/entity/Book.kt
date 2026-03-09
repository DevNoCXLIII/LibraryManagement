package com.schwisolutions.librarymanagement.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "book")
data class Book (
@PrimaryKey(autoGenerate = true)
    val bookId: Int = 0,
    val title: String,
    val author: String,
    val releaseDate: String,
    val genre: String,
)