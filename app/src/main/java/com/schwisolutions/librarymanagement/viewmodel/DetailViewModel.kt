package com.schwisolutions.librarymanagement.viewmodel

import androidx.lifecycle.ViewModel
import com.schwisolutions.librarymanagement.data.entity.Book
import com.schwisolutions.librarymanagement.repository.`interface`.BookRepository
import kotlinx.coroutines.flow.Flow

class DetailViewModel(private val bookRepository: BookRepository) : ViewModel() {
    fun getBookStream(id: Int): Flow<Book> = bookRepository.getBookStream(id)
}
