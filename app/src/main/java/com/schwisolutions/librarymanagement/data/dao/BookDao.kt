package com.schwisolutions.librarymanagement.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.schwisolutions.librarymanagement.data.entity.Book
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Book database operations.
 *
 * Official docs reference:
 * - developer.android.com/training/data-storage/room/accessing-data
 * - developer.android.com/training/data-storage/room/async-queries
 *
 * Rules:
 * - One-shot write operations (insert, update, delete) must be suspend functions.
 * - Observable read queries return Flow<T>, which Room executes asynchronously on a background dispatcher.
 */
@Dao
interface BookDao {

    /**
     * Inserts a new book into the database.
     * OnConflictStrategy.IGNORE ignores conflicting primary keys rather than aborting or overwriting.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBook(book: Book)

    /**
     * Updates an existing book matching by primary key (bookId).
     */
    @Update
    suspend fun updateBook(book: Book)

    /**
     * Deletes a book matching by primary key (bookId).
     */
    @Delete
    suspend fun deleteBook(book: Book)

    /**
     * Emits the complete list of books ordered alphabetically by title whenever the table updates.
     */
    @Query("SELECT * FROM book ORDER BY title ASC")
    fun getAllBooks(): Flow<List<Book>>

    /**
     * Emits the specific book matching bookId whenever its record changes.
     */
    @Query("SELECT * FROM book WHERE bookId = :bookId")
    fun getBookById(bookId: Int): Flow<Book?>

    /**
     * Database-level search query filtering books where title contains searchQuery.
     * Note: MainScreen uses in-memory filtering for simplicity in a 6-hour test,
     * but this method is available for direct SQL filtering if needed.
     */
    @Query("SELECT * FROM book WHERE title LIKE '%' || :searchQuery || '%'")
    fun searchBooks(searchQuery: String): Flow<List<Book>>
}