package com.schwisolutions.librarymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.schwisolutions.librarymanagement.LibraryApplication
import com.schwisolutions.librarymanagement.data.entity.Book
import com.schwisolutions.librarymanagement.repository.`interface`.BookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(val bookList: List<Book> = listOf())

class HomeViewModel(private val bookRepository: BookRepository) : ViewModel() {
    val homeUiState: StateFlow<HomeUiState> =
        bookRepository.getAllBooksStream().map { HomeUiState(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = HomeUiState()
        )

    fun addNewBook(title: String, author: String, releaseDate: String, genre: String) {
        viewModelScope.launch {
            val newBook = Book(
                title = title,
                author = author,
                releaseDate = releaseDate,
                genre = genre
            )

            bookRepository.insertBook(newBook)
        }
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            bookRepository.deleteBook(book)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = (this[APPLICATION_KEY] as LibraryApplication)
            HomeViewModel(bookRepository = application.container.bookRepository)
        }
    }
}