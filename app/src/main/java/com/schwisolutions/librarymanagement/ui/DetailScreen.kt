package com.schwisolutions.librarymanagement.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DetailsScreen(bookId: Int, onBackClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBackClick) { // Back button to navigate to the previous activity [cite: 62]
            Text("Back")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Fetching details for Book ID: $bookId")
    }
}