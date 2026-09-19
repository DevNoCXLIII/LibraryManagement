# Codefest Reference Guide: Library Management

This guide documents every file in the project, explains its mechanism, and points to the exact section in the official Android developer documentation (`developer.android.com`).

---

## 1. Build and Configuration

### [gradle/libs.versions.toml](file:///home/schwi/Projects/LibraryManagement/gradle/libs.versions.toml)
- **Purpose**: Defines dependencies, versions, and plugins in a centralized TOML version catalog.
- **Key contents**: AGP, Kotlin, Compose BOM, Room, Navigation Compose, and Kotlinx Serialization versions and libraries.
- **Official documentation**:
  - Search: "Migrate to version catalogs"
  - Path: `developer.android.com/build/migrate-to-catalogs`

### [build.gradle.kts](file:///home/schwi/Projects/LibraryManagement/build.gradle.kts)
- **Purpose**: Root-level Gradle build file. Configures top-level plugins without applying them.
- **Official documentation**:
  - Search: "Configure your build"
  - Path: `developer.android.com/build`

### [app/build.gradle.kts](file:///home/schwi/Projects/LibraryManagement/app/build.gradle.kts)
- **Purpose**: Module-level build script for the Android application.
- **Key contents**:
  - `plugins`: Android application, Kotlin Compose, Kotlin Serialization, KSP.
  - `android`: `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`, `compose = true`.
  - `dependencies`: Room runtime and compiler (KSP), Compose BOM, Navigation Compose, Lifecycle runtime and ViewModel.
- **Official documentation**:
  - Search: "Configure build variants" / "Dependencies in Gradle"
  - Path: `developer.android.com/build/dependencies`

---

## 2. Android Manifest and App Lifecycle

### [app/src/main/AndroidManifest.xml](file:///home/schwi/Projects/LibraryManagement/app/src/main/AndroidManifest.xml)
- **Purpose**: Declares application components, app name, launcher icon, activities, and content providers.
- **Key components**:
  - `android:name=".LibraryApplication"`: Registers the custom `Application` subclass.
  - `MainActivity`: Main entry point with `MAIN` and `LAUNCHER` intent filters.
  - `FileProvider`: Grants secure content URIs for camera photo captures.
- **Official documentation**:
  - Search: "App manifest overview" / "FileProvider"
  - Path: `developer.android.com/guide/topics/manifest/manifest-intro`
  - Path: `developer.android.com/reference/androidx/core/content/FileProvider`

### [app/src/main/res/xml/file_paths.xml](file:///home/schwi/Projects/LibraryManagement/app/src/main/res/xml/file_paths.xml)
- **Purpose**: Specifies directory paths accessible via `FileProvider`.
- **Key configuration**: `<files-path name="book_images" path="book_images/" />` maps the internal `context.filesDir/book_images/` directory.
- **Official documentation**:
  - Search: "FileProvider setting up provider"
  - Path: `developer.android.com/reference/androidx/core/content/FileProvider#SettingUpProvider`

### [app/src/main/java/com/schwisolutions/librarymanagement/LibraryApplication.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/LibraryApplication.kt)
- **Purpose**: Custom `Application` class that initializes app-level state before any activity starts.
- **Key mechanism**: Instantiates `AppContainer` (`AppDataContainer(this)`) as a manual dependency injection container.
- **Official documentation**:
  - Search: "Application class" / "Manual dependency injection"
  - Path: `developer.android.com/reference/android/app/Application`
  - Path: `developer.android.com/training/dependency-injection/manual`

### [app/src/main/java/com/schwisolutions/librarymanagement/AppContainer.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/AppContainer.kt)
- **Purpose**: Manual dependency injection container interface and implementation.
- **Key mechanism**: `AppDataContainer` lazily initializes `OfflineBookRepository` by injecting `LibraryDatabase.getDatabase(context).bookDao()`.
- **Official documentation**:
  - Search: "Manual dependency injection"
  - Path: `developer.android.com/training/dependency-injection/manual#app-container`

### [app/src/main/java/com/schwisolutions/librarymanagement/MainActivity.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/MainActivity.kt)
- **Purpose**: Single activity host for Jetpack Compose UI.
- **Key mechanism**: Calls `enableEdgeToEdge()` and `setContent { LibraryManagementTheme { LibraryNavigation() } }`.
- **Official documentation**:
  - Search: "Compose setup in Activity"
  - Path: `developer.android.com/develop/ui/compose/setup#activity`

---

## 3. Data Layer (Room and Storage)

### [app/src/main/java/com/schwisolutions/librarymanagement/data/entity/Book.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/data/entity/Book.kt)
- **Purpose**: Room entity representing the `book` SQLite database table.
- **Key annotations and fields**:
  - `@Entity(tableName = "book")`
  - `@PrimaryKey(autoGenerate = true) val bookId: Int = 0`
  - `title`, `author`, `releaseDate`, `genre`, and `imagePath: String? = null`.
- **Official documentation**:
  - Search: "Define data using Room entities"
  - Path: `developer.android.com/training/data-storage/room/defining-data`

### [app/src/main/java/com/schwisolutions/librarymanagement/data/dao/BookDao.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/data/dao/BookDao.kt)
- **Purpose**: Data Access Object (DAO) defining SQLite database operations.
- **Key methods**:
  - `@Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertBook(book: Book)`
  - `@Update suspend fun updateBook(book: Book)`
  - `@Delete suspend fun deleteBook(book: Book)`
  - `@Query("SELECT * FROM book ORDER BY title ASC") fun getAllBooks(): Flow<List<Book>>`
  - `@Query("SELECT * FROM book WHERE bookId=:bookId") fun getBookById(bookId: Int): Flow<Book>`
  - `@Query("SELECT * FROM book WHERE title LIKE '%' || :searchQuery || '%'") fun searchBooks(searchQuery: String): Flow<List<Book>>`
- **Official documentation**:
  - Search: "Accessing data using Room DAOs" / "Write asynchronous DAO queries"
  - Path: `developer.android.com/training/data-storage/room/accessing-data`
  - Path: `developer.android.com/training/data-storage/room/async-queries`

### [app/src/main/java/com/schwisolutions/librarymanagement/data/LibraryDatabase.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/data/LibraryDatabase.kt)
- **Purpose**: Room database singleton class.
- **Key annotations and configuration**:
  - `@Database(entities = [Book::class], version = 2, exportSchema = false)`
  - Singleton pattern via `Instance ?: synchronized(this) { Room.databaseBuilder(...).fallbackToDestructiveMigration(true).build() }`.
- **Official documentation**:
  - Search: "Room database class"
  - Path: `developer.android.com/training/data-storage/room#database`

### [app/src/main/java/com/schwisolutions/librarymanagement/data/ImageStorageHelper.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/data/ImageStorageHelper.kt)
- **Purpose**: Manages file I/O for book cover images in internal storage.
- **Key methods**:
  - `createTempImageFile(context)`: Generates a unique JPEG file in `context.filesDir/book_images/`.
  - `getUriForFile(context, file)`: Creates a content URI via `FileProvider.getUriForFile`.
  - `saveBitmap(context, bitmap)`: Compresses a bitmap to JPEG (90% quality) and writes to disk.
  - `saveUri(context, uri)`: Reads an `InputStream` from `contentResolver`, decodes with `BitmapFactory.decodeStream`, and saves to disk.
  - `deleteImage(path)`: Deletes file from internal storage.
- **Official documentation**:
  - Search: "Internal storage app-specific" / "Bitmap and BitmapFactory"
  - Path: `developer.android.com/training/data-storage/app-specific#internal-access-files`
  - Path: `developer.android.com/reference/android/graphics/BitmapFactory`

### [app/src/main/java/com/schwisolutions/librarymanagement/repository/interface/BookRepository.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/repository/interface/BookRepository.kt)
- **Purpose**: Repository interface abstracting data sources from the UI layer.
- **Official documentation**:
  - Search: "Guide to app architecture - Data layer"
  - Path: `developer.android.com/topic/architecture/data-layer`

### [app/src/main/java/com/schwisolutions/librarymanagement/repository/implementation/OfflineBookRepository.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/repository/implementation/OfflineBookRepository.kt)
- **Purpose**: Concrete repository implementation calling `BookDao`.
- **Official documentation**:
  - Search: "Repository pattern Android"
  - Path: `developer.android.com/topic/architecture/data-layer#repository`

---

## 4. ViewModel Layer

### [app/src/main/java/com/schwisolutions/librarymanagement/viewmodel/HomeViewModel.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/viewmodel/HomeViewModel.kt)
- **Purpose**: Manages UI state for the book list and handles CRUD actions.
- **Key components**:
  - `homeUiState`: Converts `bookRepository.getAllBooksStream()` into `StateFlow<HomeUiState>` via `.map { ... }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), HomeUiState())`.
  - `addNewBook`, `updateBook`, `deleteBook`: Coroutine methods running on `viewModelScope.launch`.
  - `AppViewModelProvider`: Factory object using `viewModelFactory { initializer { ... } }` to supply dependencies from `LibraryApplication`.
- **Official documentation**:
  - Search: "ViewModel overview" / "StateFlow in Android" / "ViewModel factories"
  - Path: `developer.android.com/topic/libraries/architecture/viewmodel`
  - Path: `developer.android.com/kotlin/flow/stateflow-and-sharedflow`
  - Path: `developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories`

### [app/src/main/java/com/schwisolutions/librarymanagement/viewmodel/DetailViewModel.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/viewmodel/DetailViewModel.kt)
- **Purpose**: ViewModel for `DetailScreen`.
- **Key method**: `getBookStream(id: Int): Flow<Book>` delegates directly to `bookRepository.getBookStream(id)`.
- **Official documentation**:
  - Search: "ViewModel and Flow"
  - Path: `developer.android.com/topic/libraries/architecture/viewmodel`

---

## 5. UI and Navigation Layer

### [app/src/main/java/com/schwisolutions/librarymanagement/ui/LibraryNavigation.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/LibraryNavigation.kt)
- **Purpose**: Sets up Jetpack Compose type-safe navigation.
- **Key routes**:
  - `@Serializable object PinRoute`
  - `@Serializable object MainRoute`
  - `@Serializable data class DetailsRoute(val bookId: Int)`
- **Key mechanism**: Uses `NavHost` with `composable<PinRoute>`, `composable<MainRoute>`, and `composable<DetailsRoute>`. Extracts arguments using `backStackEntry.toRoute<DetailsRoute>()`.
- **Official documentation**:
  - Search: "Navigation with Compose" / "Type safety in Navigation Compose"
  - Path: `developer.android.com/guide/navigation/navigation-with-compose`
  - Path: `developer.android.com/guide/navigation/design/type-safety`

### [app/src/main/java/com/schwisolutions/librarymanagement/ui/PinScreen.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/PinScreen.kt)
- **Purpose**: 4-digit PIN lock screen guarding app access.
- **Key mechanisms**:
  - `rememberSaveable` for PIN state and error status.
  - Interactive 3x4 numeric keypad (0-9, Clear, Delete).
  - 4 circular indicator dots showing entry progress.
  - Automatic validation against `CORRECT_PIN = "1234"` triggering `onLoginSuccess()`.
- **Official documentation**:
  - Search: "State in Jetpack Compose" / "rememberSaveable"
  - Path: `developer.android.com/develop/ui/compose/state#save-ui-state`

### [app/src/main/java/com/schwisolutions/librarymanagement/ui/MainScreen.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/MainScreen.kt)
- **Purpose**: Primary dashboard displaying searchable book list, Add Book dialog, Edit Book dialog, and Delete confirmation dialog.
- **Key components**:
  - `Scaffold`: Top app bar and floating action button.
  - `OutlinedTextField`: Search input filtering `uiState.bookList` in real time.
  - `LazyColumn`: Recycler list of `BookListItem` components.
  - `BookListItem`: Card with 50dp thumbnail (decoded via `BitmapFactory`), title, year, and ellipsis options menu (`DropdownMenu`).
  - `ImageChooserDialog`: Modal to select between Camera and Gallery.
  - `AddBookDialog` & `EditBookDialog`: Input forms with live cover preview, `ActivityResultContracts.TakePicture()` for camera, and `ActivityResultContracts.GetContent()` for gallery.
- **Official documentation**:
  - Search: "Compose Scaffold" / "Lists and grids Compose" / "Dialogs Compose" / "Activity Results in Compose"
  - Path: `developer.android.com/develop/ui/compose/components/scaffold`
  - Path: `developer.android.com/develop/ui/compose/lists`
  - Path: `developer.android.com/develop/ui/compose/components/dialog`
  - Path: `developer.android.com/develop/ui/compose/libraries#activity-result`

### [app/src/main/java/com/schwisolutions/librarymanagement/ui/DetailScreen.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/DetailScreen.kt)
- **Purpose**: Displays full metadata and cover for a selected book.
- **Key mechanisms**:
  - Collects `viewModel.getBookStream(bookId).collectAsState(initial = null)`.
  - Shows `CircularProgressIndicator` while loading.
  - Renders 140dp x 180dp cover image with fallback placeholder icon.
  - Formats title, author, publication year, and genre in a Material 3 card.
- **Official documentation**:
  - Search: "State and Compose collectAsState" / "Card Material 3 Compose"
  - Path: `developer.android.com/develop/ui/compose/state`
  - Path: `developer.android.com/develop/ui/compose/components/card`

### [app/src/main/java/com/schwisolutions/librarymanagement/ui/theme/](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/theme/)
- **[Color.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/theme/Color.kt)**: Defines raw color values.
- **[Type.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/theme/Type.kt)**: Defines typography typography styles (`Typography`).
- **[Theme.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/theme/Theme.kt)**: Defines `LibraryManagementTheme` with dynamic color support (`dynamicDarkColorScheme`, `dynamicLightColorScheme`).
- **Official documentation**:
  - Search: "Material 3 in Compose" / "Compose theming"
  - Path: `developer.android.com/develop/ui/compose/designsystems/material3`

---

## 6. Unit Testing

### [app/src/test/java/com/schwisolutions/librarymanagement/ImageStorageHelperTest.kt](file:///home/schwi/Projects/LibraryManagement/app/src/test/java/com/schwisolutions/librarymanagement/ImageStorageHelperTest.kt)
- **Purpose**: Local JVM unit tests for file deletion and error handling in `ImageStorageHelper`.
- **Official documentation**:
  - Search: "Test in Android" / "Build local unit tests"
  - Path: `developer.android.com/training/testing/local-tests`
