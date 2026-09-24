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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.schwisolutions.librarymanagement.data.ImageStorageHelper
import com.schwisolutions.librarymanagement.data.entity.Book
import com.schwisolutions.librarymanagement.viewmodel.AppViewModelProvider
import com.schwisolutions.librarymanagement.viewmodel.HomeViewModel

/**
 * Main dashboard screen displaying the searchable book list, Add Book FAB,
 * and dialogs for creating, editing, and deleting books.
 *
 * Official docs reference:
 * - developer.android.com/develop/ui/compose/components/scaffold
 * - developer.android.com/develop/ui/compose/lists
 * - developer.android.com/develop/ui/compose/components/dialog
 *
 * @param onViewBook Callback invoked when the user selects "View" on a book, passing its bookId.
 * @param viewModel HomeViewModel providing the reactive book list and CRUD methods.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onViewBook: (Int) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // 1. Observe Room database state via StateFlow
    val uiState by viewModel.homeUiState.collectAsState()

    // 2. Dialog state holders
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var bookToEdit by remember { mutableStateOf<Book?>(null) }
    var bookToDelete by remember { mutableStateOf<Book?>(null) }

    // 3. Search query state
    var searchQuery by rememberSaveable { mutableStateOf("") }

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
            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Books") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Book list with in-memory filtering
            // Rationale for 6-hour test: In-memory filtering is immediate and avoids complex Flow pipelines.
            val filteredBooks = uiState.bookList.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Offset for FAB
            ) {
                items(filteredBooks, key = { it.bookId }) { book ->
                    BookListItem(
                        book = book,
                        onViewClick = { onViewBook(book.bookId) },
                        onEditClick = { bookToEdit = book },
                        onDeleteClick = { bookToDelete = book }
                    )
                }
            }
        }

        // Add Book Dialog (using unified BookFormDialog)
        if (showAddDialog) {
            BookFormDialog(
                titleText = "Add a New Book",
                onDismiss = { showAddDialog = false },
                onConfirm = { title, author, releaseDate, genre, imagePath ->
                    viewModel.addNewBook(title, author, releaseDate, genre, imagePath)
                    showAddDialog = false
                }
            )
        }

        // Edit Book Dialog (using unified BookFormDialog with initial data)
        bookToEdit?.let { book ->
            BookFormDialog(
                titleText = "Edit Book",
                initialBook = book,
                onDismiss = { bookToEdit = null },
                onConfirm = { title, author, releaseDate, genre, imagePath ->
                    viewModel.updateBook(
                        book.copy(
                            title = title,
                            author = author,
                            releaseDate = releaseDate,
                            genre = genre,
                            imagePath = imagePath
                        )
                    )
                    bookToEdit = null
                }
            )
        }

        // Delete Confirmation Dialog
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

/**
 * List item card displaying thumbnail, title, publication year, and options dropdown.
 *
 * @param book Book entity to display.
 * @param onViewClick Callback to navigate to details screen.
 * @param onEditClick Callback to open edit dialog.
 * @param onDeleteClick Callback to open delete confirmation.
 */
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
            // Decode image from local file path with safe downsampling
            val bitmap = remember(book.imagePath) {
                ImageStorageHelper.loadThumbnail(book.imagePath, reqWidth = 100, reqHeight = 100)?.asImageBitmap()
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
                    // Note: Icons.Default.Star is part of core icons; no material-icons-extended dependency required.
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Book Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = book.title, style = MaterialTheme.typography.titleMedium)
                Text(text = "Year: ${book.releaseDate}", style = MaterialTheme.typography.bodyMedium)
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                }

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

/**
 * Chooser modal to select between Camera and Gallery.
 */
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

/**
 * Unified form dialog for creating and editing books.
 *
 * Rationale for 6-hour recreation:
 * - Unifying Add and Edit into one dialog saves ~150 lines of duplicate code.
 * - Uses ActivityResultContracts.TakePicturePreview() which returns a Bitmap directly.
 *   This avoids FileProvider, XML file_paths, and temporary URI generation.
 * - Uses ActivityResultContracts.GetContent() with an image MIME type for gallery picking.
 *
 * @param titleText Dialog header title ("Add a New Book" or "Edit Book").
 * @param initialBook Optional existing Book if editing, or null if adding.
 * @param onDismiss Invoked when the dialog is dismissed.
 * @param onConfirm Invoked with validated form values when user saves.
 */
@Composable
fun BookFormDialog(
    titleText: String,
    initialBook: Book? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, author: String, releaseDate: String, genre: String, imagePath: String?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialBook?.title ?: "") }
    var author by rememberSaveable { mutableStateOf(initialBook?.author ?: "") }
    var releaseDate by rememberSaveable { mutableStateOf(initialBook?.releaseDate ?: "") }
    var genre by rememberSaveable { mutableStateOf(initialBook?.genre ?: "") }
    var imagePath by rememberSaveable { mutableStateOf(initialBook?.imagePath) }
    var showChooser by rememberSaveable { mutableStateOf(false) }

    var isTitleError by rememberSaveable { mutableStateOf(false) }
    var isAuthorError by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Camera launcher: TakePicturePreview returns a Bitmap directly.
    // No FileProvider, no XML configuration, and no content URI setup required.
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            imagePath = ImageStorageHelper.saveBitmap(context, bitmap)
        }
    }

    // Gallery launcher: GetContent returns the image Uri, which ImageStorageHelper decodes and saves.
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
            onTakePhoto = { cameraLauncher.launch(null) },
            onChooseGallery = { galleryLauncher.launch("image/*") }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cover image preview box with safe downsampled thumbnail
                val bitmap = remember(imagePath) {
                    ImageStorageHelper.loadThumbnail(imagePath, reqWidth = 200, reqHeight = 240)?.asImageBitmap()
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

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = { Text("Title *") },
                    isError = isTitleError,
                    supportingText = if (isTitleError) {
                        { Text("Title cannot be empty") }
                    } else null,
                    singleLine = true
                )
                OutlinedTextField(
                    value = author,
                    onValueChange = {
                        author = it
                        if (it.isNotBlank()) isAuthorError = false
                    },
                    label = { Text("Author *") },
                    isError = isAuthorError,
                    supportingText = if (isAuthorError) {
                        { Text("Author cannot be empty") }
                    } else null,
                    singleLine = true
                )
                OutlinedTextField(
                    value = releaseDate,
                    onValueChange = { releaseDate = it },
                    label = { Text("Publication Year") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hasTitle = title.isNotBlank()
                    val hasAuthor = author.isNotBlank()
                    if (!hasTitle) isTitleError = true
                    if (!hasAuthor) isAuthorError = true

                    if (hasTitle && hasAuthor) {
                        // Clean up old image if replaced during edit
                        if (initialBook != null && imagePath != initialBook.imagePath) {
                            ImageStorageHelper.deleteImage(initialBook.imagePath)
                        }
                        onConfirm(title.trim(), author.trim(), releaseDate.trim(), genre.trim(), imagePath)
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
