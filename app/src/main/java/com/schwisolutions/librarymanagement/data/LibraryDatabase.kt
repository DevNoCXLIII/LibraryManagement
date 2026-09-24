package com.schwisolutions.librarymanagement.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.schwisolutions.librarymanagement.data.dao.BookDao
import com.schwisolutions.librarymanagement.data.entity.Book

/**
 * Room Database singleton class for the application.
 *
 * Official docs reference:
 * - developer.android.com/training/data-storage/room#database
 *
 * Configuration:
 * - entities: List of all entity classes associated with this database.
 * - version: Schema version number. Increment when modifying entities.
 * - exportSchema = false: Avoids requiring a schema export folder in build files.
 * - fallbackToDestructiveMigration(true): Clears and rebuilds tables cleanly on schema version bump,
 *   avoiding the need for manual Migration scripts during fast development or 6-hour exams.
 */
@Database(entities = [Book::class], version = 2, exportSchema = false)
abstract class LibraryDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var Instance: LibraryDatabase? = null

        /**
         * Returns the thread-safe singleton instance of LibraryDatabase.
         *
         * Double-checked locking with @Volatile ensures only one database instance exists across threads.
         */
        fun getDatabase(context: Context): LibraryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    LibraryDatabase::class.java,
                    "library_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-seed sample books for immediate testing without manual data entry
                            db.execSQL("INSERT INTO book (title, author, releaseDate, genre) VALUES ('Clean Code', 'Robert C. Martin', '2008', 'Software')")
                            db.execSQL("INSERT INTO book (title, author, releaseDate, genre) VALUES ('The Pragmatic Programmer', 'David Thomas', '1999', 'Software')")
                            db.execSQL("INSERT INTO book (title, author, releaseDate, genre) VALUES ('Kotlin in Action', 'Dmitry Jemerov', '2017', 'Programming')")
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }
    }
}