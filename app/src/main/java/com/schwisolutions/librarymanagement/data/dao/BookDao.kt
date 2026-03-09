package com.schwisolutions.librarymanagement.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.schwisolutions.librarymanagement.data.entity.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBook(book: Book)

    @Update
    suspend fun updateBook(book: Book)

    @Delete
    suspend fun deleteBook(book: Book)

    @Query("SELECT * FROM book ORDER BY title ASC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM book WHERE title LIKE '%' || :searchQuery || '%'")
    fun searchBooks(searchQuery: String): Flow<List<Book>>

    @Query("SELECT * FROM book WHERE bookId=:bookId")
    fun getBookById(bookId: Int): Flow<Book>
}