package com.schwisolutions.librarymanagement.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.schwisolutions.librarymanagement.data.dao.BookDao
import com.schwisolutions.librarymanagement.data.entity.Book


@Database(entities = [Book::class], version = 1, exportSchema = false)
abstract class LibraryDatabase : RoomDatabase() {
    abstract fun bookDao() : BookDao

    companion object {
        @Volatile
        private var Instance: LibraryDatabase? = null

        fun getDatabase(context: Context): LibraryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "library_database"
                )
                    .build()
                    .also { Instance = it }
            }
        }
    }
}