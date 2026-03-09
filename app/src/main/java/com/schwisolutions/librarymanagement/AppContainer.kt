package com.schwisolutions.librarymanagement

import android.content.Context
import com.schwisolutions.librarymanagement.data.LibraryDatabase
import com.schwisolutions.librarymanagement.repository.implementation.OfflineBookRepository
import com.schwisolutions.librarymanagement.repository.`interface`.BookRepository

interface AppContainer {
    val bookRepository: BookRepository
}

class AppDataContainer (private val context: Context) : AppContainer {
    override val bookRepository: BookRepository by lazy {
        OfflineBookRepository(LibraryDatabase.getDatabase(context).bookDao())
    }
}