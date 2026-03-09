package com.schwisolutions.librarymanagement

import android.app.Application

class LibraryApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}