package com.schwisolutions.librarymanagement.repository

import com.schwisolutions.librarymanagement.data.entity.Book
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining data access operations for Book entities.
 *
 * Official docs reference:
 * - developer.android.com/topic/architecture/data-layer#repository
 *
 * Architecture rationale:
 * The repository acts as a single source of truth and abstracts the underlying data source
 * (Room SQLite database) from the ViewModel and UI layers.
 */
interface BookRepository {

    /**
     * Returns a Flow emitting the complete list of books ordered alphabetically.
     */
    fun getAllBooksStream(): Flow<List<Book>>

    /**
     * Returns a Flow emitting a single book matching the given ID.
     */
    fun getBookStream(id: Int): Flow<Book?>

    /**
     * Returns a Flow emitting books matching the search query.
     */
    fun searchBooksStream(query: String): Flow<List<Book>>

    /**
     * Inserts a new book into the repository.
     */
    suspend fun insertBook(book: Book)

    /**
     * Updates an existing book in the repository.
     */
    suspend fun updateBook(book: Book)

    /**
     * Deletes a book from the repository.
     */
    suspend fun deleteBook(book: Book)
}
