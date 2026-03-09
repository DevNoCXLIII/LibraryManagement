package com.schwisolutions.librarymanagement.repository.`interface`

import com.schwisolutions.librarymanagement.data.entity.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {

    fun getAllBooksStream(): Flow<List<Book>>
    fun getBookStream(id: Int): Flow<Book>
    fun searchBooksStream(query: String): Flow<List<Book>>

    suspend fun insertBook(book: Book)
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(book: Book)
}