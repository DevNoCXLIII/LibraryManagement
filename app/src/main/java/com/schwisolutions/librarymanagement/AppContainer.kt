package com.schwisolutions.librarymanagement

import android.content.Context
import com.schwisolutions.librarymanagement.data.LibraryDatabase
import com.schwisolutions.librarymanagement.repository.BookRepository
import com.schwisolutions.librarymanagement.repository.OfflineBookRepository

/**
 * Dependency injection container interface for the application.
 *
 * Official docs reference:
 * - developer.android.com/training/dependency-injection/manual#app-container
 *
 * Pattern benefits in a 6-hour test:
 * - Provides dependencies without external DI libraries (Hilt/Dagger), saving build setup time.
 * - Single point of dependency declaration and lazy instantiation.
 */
interface AppContainer {
    val bookRepository: BookRepository
}

/**
 * Concrete AppContainer implementation managing production dependencies.
 *
 * @param context Application context used for initializing Room database.
 */
class AppDataContainer(private val context: Context) : AppContainer {

    /**
     * Lazy initialization creates the database and repository on first access.
     */
    override val bookRepository: BookRepository by lazy {
        OfflineBookRepository(LibraryDatabase.getDatabase(context).bookDao())
    }
}