package com.schwisolutions.librarymanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.schwisolutions.librarymanagement.ui.LibraryNavigation
import com.schwisolutions.librarymanagement.ui.theme.LibraryManagementTheme

/**
 * Single Activity entry point hosting the Compose UI.
 *
 * Official docs reference:
 * - developer.android.com/develop/ui/compose/setup#activity
 *
 * Responsibilities:
 * - Calls enableEdgeToEdge() for full-screen layout behind system bars.
 * - Sets the Compose content root wrapped with LibraryManagementTheme and Scaffold.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LibraryManagementTheme {
                LibraryNavigation(modifier = Modifier.fillMaxSize())
            }
        }
    }
}