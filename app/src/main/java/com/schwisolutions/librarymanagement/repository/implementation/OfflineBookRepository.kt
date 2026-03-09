package com.schwisolutions.librarymanagement.repository.implementation

import com.schwisolutions.librarymanagement.data.dao.BookDao
import com.schwisolutions.librarymanagement.data.entity.Book
import com.schwisolutions.librarymanagement.repository.`interface`.BookRepository
import kotlinx.coroutines.flow.Flow

class OfflineBookRepository(private val bookDao: BookDao) : BookRepository{
    override fun getAllBooksStream(): Flow<List<Book>> {
        return bookDao.getAllBooks()
    }

    override fun getBookStream(id: Int): Flow<Book> {
        return bookDao.getBookById(id)
    }

    override fun searchBooksStream(query: String): Flow<List<Book>> {
        return bookDao.searchBooks(query)
    }

    override suspend fun insertBook(book: Book) {
        return bookDao.insertBook(book)
    }

    override suspend fun updateBook(book: Book) {
        return bookDao.updateBook(book)
    }

    override suspend fun deleteBook(book: Book) {
        return bookDao.deleteBook(book)
    }
}