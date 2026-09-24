package com.schwisolutions.librarymanagement.viewmodel

import androidx.lifecycle.ViewModel
import com.schwisolutions.librarymanagement.data.entity.Book
import com.schwisolutions.librarymanagement.repository.BookRepository
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel for the book details screen.
 *
 * Official docs reference:
 * - developer.android.com/topic/libraries/architecture/viewmodel
 *
 * @param bookRepository Repository providing data access.
 */
class DetailViewModel(private val bookRepository: BookRepository) : ViewModel() {

    /**
     * Returns a Flow emitting the book with the specified id.
     * The Flow automatically emits updates if the book is modified elsewhere in the app.
     */
    fun getBookStream(id: Int): Flow<Book?> = bookRepository.getBookStream(id)
}
