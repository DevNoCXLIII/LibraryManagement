package com.schwisolutions.librarymanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.schwisolutions.librarymanagement.LibraryApplication
import com.schwisolutions.librarymanagement.data.ImageStorageHelper
import com.schwisolutions.librarymanagement.data.entity.Book
import com.schwisolutions.librarymanagement.repository.BookRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state data class for the home/main book list screen.
 *
 * @param bookList List of books currently in the database.
 */
data class HomeUiState(val bookList: List<Book> = listOf())

/**
 * ViewModel managing the book list and book CRUD actions.
 *
 * Official docs reference:
 * - developer.android.com/topic/libraries/architecture/viewmodel
 * - developer.android.com/kotlin/flow/stateflow-and-sharedflow
 *
 * @param bookRepository Repository providing data access.
 */
class HomeViewModel(private val bookRepository: BookRepository) : ViewModel() {

    /**
     * Hot StateFlow converting the Room database Flow into UI state.
     *
     * SharingStarted.WhileSubscribed(5000L):
     * - Keeps the upstream flow active while Compose UI collects it.
     * - Adds a 5-second buffer during configuration changes (e.g., screen rotations)
     *   to prevent restarting the database query unnecessarily.
     */
    val homeUiState: StateFlow<HomeUiState> =
        bookRepository.getAllBooksStream().map { HomeUiState(it) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = HomeUiState()
        )

    /**
     * Inserts a new book into the repository inside viewModelScope.
     */
    fun addNewBook(
        title: String,
        author: String,
        releaseDate: String,
        genre: String,
        imagePath: String? = null
    ) {
        viewModelScope.launch {
            val newBook = Book(
                title = title,
                author = author,
                releaseDate = releaseDate,
                genre = genre,
                imagePath = imagePath
            )
            bookRepository.insertBook(newBook)
        }
    }

    /**
     * Updates an existing book record.
     */
    fun updateBook(book: Book) {
        viewModelScope.launch {
            bookRepository.updateBook(book)
        }
    }

    /**
     * Deletes a book record and removes its cover image from disk if present.
     */
    fun deleteBook(book: Book) {
        viewModelScope.launch {
            ImageStorageHelper.deleteImage(book.imagePath)
            bookRepository.deleteBook(book)
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * ViewModel factory object using AndroidX viewmodelFactory DSL.
 *
 * Official docs reference:
 * - developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories
 *
 * In Compose, retrieve ViewModels using:
 * `viewModel(factory = AppViewModelProvider.Factory)`
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val application = (this[APPLICATION_KEY] as LibraryApplication)
            HomeViewModel(bookRepository = application.container.bookRepository)
        }
        initializer {
            val application = (this[APPLICATION_KEY] as LibraryApplication)
            DetailViewModel(bookRepository = application.container.bookRepository)
        }
    }
}