package com.schwisolutions.librarymanagement.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.schwisolutions.librarymanagement.data.ImageStorageHelper
import com.schwisolutions.librarymanagement.data.entity.Book
import com.schwisolutions.librarymanagement.viewmodel.AppViewModelProvider
import com.schwisolutions.librarymanagement.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onViewBook: (Int) -> Unit, // Navigation callback
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // 1. Observe the database state
    val uiState by viewModel.homeUiState.collectAsState()

    // 2. State for controlling dialogs
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    // 3. State for the Search Bar
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library Management") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Book")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Module
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Books") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Main List of Books
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Prevents FAB from hiding last item
            ) {
                // Simple search filter implementation
                val filteredBooks = uiState.bookList.filter {
                    it.title.contains(searchQuery, ignoreCase = true)
                }

                items(filteredBooks) { book ->
                    BookListItem(
                        book = book,
                        onViewClick = { onViewBook(book.bookId) }, // Triggers navigation!
                        onEditClick = { bookToEdit = book },
                        onDeleteClick = {
                            bookToDelete = book // Set the book to trigger the confirmation dialog
                        }
                    )
                }
            }
        }

        // Add Book Form Dialog
        if (showAddDialog) {
            AddBookDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, author, releaseDate, genre, imagePath ->
                    viewModel.addNewBook(title, author, releaseDate, genre, imagePath)
                    showAddDialog = false
                }
            )
        }

        // Edit Book Form Dialog
        bookToEdit?.let { book ->
            EditBookDialog(
                book = book,
                onDismiss = { bookToEdit = null },
                onConfirm = { updatedBook ->
                    viewModel.updateBook(updatedBook)
                    bookToEdit = null
                }
            )
        }

        // Delete Confirmation Dialog (Requirement: "action confirmation" )
        bookToDelete?.let { book ->
            AlertDialog(
                onDismissRequest = { bookToDelete = null },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete '${book.title}'? This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteBook(book)
                            bookToDelete = null
                        }
                    ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { bookToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun BookListItem(
    book: Book,
    onViewClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val bitmap = remember(book.imagePath) {
                book.imagePath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
            }
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Book Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Display Title and Publication Year
                Text(text = book.title, style = MaterialTheme.typography.titleMedium)
                Text(text = "Year: ${book.releaseDate}", style = MaterialTheme.typography.bodyMedium)
            }

            Box {
                // Ellipsis Button
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                }

                // PopupMenu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View") },
                        onClick = { expanded = false; onViewClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { expanded = false; onEditClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { expanded = false; onDeleteClick() }
                    )
                }
            }
        }
    }
}

@Composable
fun ImageChooserDialog(
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Book Cover") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        onTakePhoto()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo (Camera)")
                    }
                }
                TextButton(
                    onClick = {
                        onChooseGallery()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Gallery")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var releaseDate by rememberSaveable { mutableStateOf("") }
    var genre by rememberSaveable { mutableStateOf("") }
    var imagePath by rememberSaveable { mutableStateOf<String?>(null) }
    var tempCameraPath by rememberSaveable { mutableStateOf<String?>(null) }
    var showChooser by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraPath != null) {
            imagePath = tempCameraPath
        } else if (!success && tempCameraPath != null) {
            ImageStorageHelper.deleteImage(tempCameraPath)
            tempCameraPath = null
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imagePath = ImageStorageHelper.saveUri(context, uri)
        }
    }

    if (showChooser) {
        ImageChooserDialog(
            onDismiss = { showChooser = false },
            onTakePhoto = {
                val file = ImageStorageHelper.createTempImageFile(context)
                tempCameraPath = file.absolutePath
                val uri = ImageStorageHelper.getUriForFile(context, file)
                cameraLauncher.launch(uri)
            },
            onChooseGallery = { galleryLauncher.launch("image/*") }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add a New Book") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val bitmap = remember(imagePath) {
                    imagePath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
                }
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showChooser = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Cover Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Cover")
                            Text("Add Cover", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") })
                OutlinedTextField(value = releaseDate, onValueChange = { releaseDate = it }, label = { Text("Publication Year") })
                OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Genre") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && author.isNotBlank()) {
                        onConfirm(title, author, releaseDate, genre, imagePath)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditBookDialog(
    book: Book,
    onDismiss: () -> Unit,
    onConfirm: (Book) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(book.title) }
    var author by rememberSaveable { mutableStateOf(book.author) }
    var releaseDate by rememberSaveable { mutableStateOf(book.releaseDate) }
    var genre by rememberSaveable { mutableStateOf(book.genre) }
    var imagePath by rememberSaveable { mutableStateOf(book.imagePath) }
    var tempCameraPath by rememberSaveable { mutableStateOf<String?>(null) }
    var showChooser by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraPath != null) {
            imagePath = tempCameraPath
        } else if (!success && tempCameraPath != null) {
            ImageStorageHelper.deleteImage(tempCameraPath)
            tempCameraPath = null
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imagePath = ImageStorageHelper.saveUri(context, uri)
        }
    }

    if (showChooser) {
        ImageChooserDialog(
            onDismiss = { showChooser = false },
            onTakePhoto = {
                val file = ImageStorageHelper.createTempImageFile(context)
                tempCameraPath = file.absolutePath
                val uri = ImageStorageHelper.getUriForFile(context, file)
                cameraLauncher.launch(uri)
            },
            onChooseGallery = { galleryLauncher.launch("image/*") }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Book") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val bitmap = remember(imagePath) {
                    imagePath?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
                }
                Box(
                    modifier = Modifier
                        .size(width = 100.dp, height = 120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showChooser = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Cover Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Cover")
                            Text("Add Cover", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") })
                OutlinedTextField(value = releaseDate, onValueChange = { releaseDate = it }, label = { Text("Publication Year") })
                OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Genre") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && author.isNotBlank()) {
                        if (imagePath != book.imagePath) {
                            ImageStorageHelper.deleteImage(book.imagePath)
                        }
                        onConfirm(
                            book.copy(
                                title = title,
                                author = author,
                                releaseDate = releaseDate,
                                genre = genre,
                                imagePath = imagePath
                            )
                        )
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


