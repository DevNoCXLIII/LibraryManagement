package com.schwisolutions.librarymanagement

import android.app.Application

/**
 * Base Application class for maintaining global application state and dependency container.
 *
 * Official docs reference:
 * - developer.android.com/reference/android/app/Application
 * - developer.android.com/training/dependency-injection/manual#app-container
 *
 * Requirements:
 * Must be registered in AndroidManifest.xml:
 * `<application android:name=".LibraryApplication" ...>`
 */
class LibraryApplication : Application() {

    /**
     * AppContainer instance holding app-scoped dependencies (e.g., repositories).
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}