# Codefest Reference Guide: Library Management

This guide documents every file in the project, explains its mechanism, points to the exact section in official Android developer documentation (`developer.android.com`), and highlights practical patterns for a 6-hour offline coding exam.

---

## 1. Quick Reference: 6-Hour Exam Strategy

When building this application from scratch in a time-constrained environment without outside sources:

1. **Avoid FileProvider**:
   - Do not use `FileProvider`, `file_paths.xml`, or `ActivityResultContracts.TakePicture()`.
   - Use `ActivityResultContracts.TakePicturePreview()`. It returns a `Bitmap?` directly. This requires zero XML files and zero manifest changes.
2. **Avoid keyword packages**:
   - Keep repository classes in `com.schwisolutions.librarymanagement.repository`.
   - Never use reserved words like `interface` as package names (`repository.interface` requires cumbersome backticks).
3. **Unify form dialogs**:
   - Combine Add and Edit dialogs into a single `BookFormDialog`. This cuts ~150 lines of duplicate code.
4. **Use in-memory filtering for search**:
   - Filter `uiState.bookList.filter { it.title.contains(query, ignoreCase = true) }` directly in Compose. It avoids writing reactive search query flows.
5. **Use core icons**:
   - Use `Icons.Default.Add`, `Icons.Default.Search`, `Icons.Default.MoreVert`, `Icons.Default.Star`, `Icons.Default.Lock`, `Icons.Default.Delete`.
   - These are included in the default `material3` library. They do not require `androidx.compose.material:material-icons-extended`.

---

## 2. Build and Configuration

### [gradle/libs.versions.toml](file:///home/schwi/Projects/LibraryManagement/gradle/libs.versions.toml)
- **Purpose**: Centralized version catalog for dependencies and plugins.
- **Key libraries**:
  - `androidx-room-runtime` and `androidx-room-compiler` (Room database)
  - `androidx-navigation-compose-android` (Navigation Compose)
  - `kotlinx-serialization-json` (Type-safe navigation routes)
  - `androidx-lifecycle-viewmodel-compose` (ViewModel integration in Compose)
- **Official documentation**:
  - Search: "Migrate to version catalogs"
  - URL: `developer.android.com/build/migrate-to-catalogs`

### [app/build.gradle.kts](file:///home/schwi/Projects/LibraryManagement/app/build.gradle.kts)
- **Purpose**: Module-level build script.
- **Key plugins**:
  - `alias(libs.plugins.android.application)`
  - `alias(libs.plugins.kotlin.compose)`
  - `kotlin("plugin.serialization")`
  - `id("com.google.devtools.ksp")`
- **Key configuration**:
  - `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`
  - `buildFeatures { compose = true }`
- **Official documentation**:
  - Search: "Configure your build" / "Dependencies in Gradle"
  - URL: `developer.android.com/build/dependencies`

---

## 3. App Entry and Manifest

### [app/src/main/AndroidManifest.xml](file:///home/schwi/Projects/LibraryManagement/app/src/main/AndroidManifest.xml)
- **Purpose**: Registers application components, custom Application class, and main Activity.
- **Key declarations**:
  - `android:name=".LibraryApplication"`: Informs Android to instantiate our custom Application subclass on boot.
  - `MainActivity`: Single activity entry point with `MAIN` and `LAUNCHER` intent filters.
  - *No FileProvider needed*: `TakePicturePreview()` bypasses content URI requirements.
- **Official documentation**:
  - Search: "App manifest overview"
  - URL: `developer.android.com/guide/topics/manifest/manifest-intro`

### [app/src/main/java/com/schwisolutions/librarymanagement/LibraryApplication.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/LibraryApplication.kt)
- **Purpose**: Custom `Application` class maintaining the app-level dependency injection container.
- **Mechanism**: Initializes `container = AppDataContainer(this)` in `onCreate()`.
- **Official documentation**:
  - Search: "Application class" / "Manual dependency injection"
  - URL: `developer.android.com/reference/android/app/Application`
  - URL: `developer.android.com/training/dependency-injection/manual`

### [app/src/main/java/com/schwisolutions/librarymanagement/AppContainer.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/AppContainer.kt)
- **Purpose**: Manual dependency injection container.
- **Mechanism**: `AppDataContainer` lazily initializes `OfflineBookRepository` by injecting `LibraryDatabase.getDatabase(context).bookDao()`.
- **Official documentation**:
  - Search: "Manual dependency injection"
  - URL: `developer.android.com/training/dependency-injection/manual#app-container`

### [app/src/main/java/com/schwisolutions/librarymanagement/MainActivity.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/MainActivity.kt)
- **Purpose**: Single activity hosting the Compose UI.
- **Mechanism**: Calls `enableEdgeToEdge()` and sets `LibraryManagementTheme { Scaffold { LibraryNavigation() } }`.
- **Official documentation**:
  - Search: "Compose setup in Activity"
  - URL: `developer.android.com/develop/ui/compose/setup#activity`

---

## 4. Data Layer (Room and Storage)

### [app/src/main/java/com/schwisolutions/librarymanagement/data/entity/Book.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/data/entity/Book.kt)
- **Purpose**: Room entity representing SQLite table `book`.
- **Annotations and fields**:
  - `@Entity(tableName = "book")`
  - `@PrimaryKey(autoGenerate = true) val bookId: Int = 0`
  - `title: String`, `author: String`, `releaseDate: String`, `genre: String`
  - `imagePath: String? = null` (stores internal absolute file path)
- **Official documentation**:
  - Search: "Define data using Room entities"
  - URL: `developer.android.com/training/data-storage/room/defining-data`

### [app/src/main/java/com/schwisolutions/librarymanagement/data/dao/BookDao.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/data/dao/BookDao.kt)
- **Purpose**: Data Access Object defining SQLite operations.
- **Key methods**:
  - `@Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertBook(book: Book)`
  - `@Update suspend fun updateBook(book: Book)`
  - `@Delete suspend fun deleteBook(book: Book)`
  - `@Query("SELECT * FROM book ORDER BY title ASC") fun getAllBooks(): Flow<List<Book>>`
  - `@Query("SELECT * FROM book WHERE bookId = :bookId") fun getBookById(bookId: Int): Flow<Book>`
- **Official documentation**:
  - Search: "Accessing data using Room DAOs" / "Write asynchronous DAO queries"
  - URL: `developer.android.com/training/data-storage/room/accessing-data`
  - URL: `developer.android.com/training/data-storage/room/async-queries`

### [app/src/main/java/com/schwisolutions/librarymanagement/data/LibraryDatabase.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/data/LibraryDatabase.kt)
- **Purpose**: Room database singleton.
- **Key configuration**:
  - `@Database(entities = [Book::class], version = 2, exportSchema = false)`
  - Double-checked locking singleton with `@Volatile private var Instance: LibraryDatabase? = null`.
  - `.fallbackToDestructiveMigration(true)` to rebuild tables on schema changes without manual migration scripts.
- **Official documentation**:
  - Search: "Room database class"
  - URL: `developer.android.com/training/data-storage/room#database`

### [app/src/main/java/com/schwisolutions/librarymanagement/data/ImageStorageHelper.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/data/ImageStorageHelper.kt)
- **Purpose**: Manages JPEG file persistence in private app storage (`context.filesDir/book_images/`).
- **Key methods**:
  - `saveBitmap(context, bitmap)`: Compresses Bitmap to JPEG (90% quality) and writes to disk. Returns absolute file path.
  - `saveUri(context, uri)`: Reads `InputStream` from `contentResolver`, decodes with `BitmapFactory.decodeStream`, and saves via `saveBitmap`.
  - `deleteImage(path)`: Safely deletes the file if it exists.
- **Official documentation**:
  - Search: "Internal storage app-specific" / "BitmapFactory"
  - URL: `developer.android.com/training/data-storage/app-specific#internal-access-files`
  - URL: `developer.android.com/reference/android/graphics/BitmapFactory`

### [app/src/main/java/com/schwisolutions/librarymanagement/repository/BookRepository.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/repository/BookRepository.kt)
- **Purpose**: Repository interface abstracting data sources.
- **Official documentation**:
  - Search: "Guide to app architecture - Data layer"
  - URL: `developer.android.com/topic/architecture/data-layer`

### [app/src/main/java/com/schwisolutions/librarymanagement/repository/OfflineBookRepository.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/repository/OfflineBookRepository.kt)
- **Purpose**: Concrete repository implementation delegating calls to `BookDao`.
- **Official documentation**:
  - Search: "Repository pattern Android"
  - URL: `developer.android.com/topic/architecture/data-layer#repository`

---

## 5. ViewModel Layer

### [app/src/main/java/com/schwisolutions/librarymanagement/viewmodel/HomeViewModel.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/viewmodel/HomeViewModel.kt)
- **Purpose**: Manages UI state for the book list and handles CRUD operations.
- **Key components**:
  - `homeUiState`: Converts `bookRepository.getAllBooksStream()` into `StateFlow<HomeUiState>` via `.map { ... }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), HomeUiState())`.
  - `addNewBook`, `updateBook`, `deleteBook`: Coroutines launched on `viewModelScope`.
  - `AppViewModelProvider`: Factory using `viewModelFactory { initializer { ... } }` to supply dependencies from `LibraryApplication`.
- **Official documentation**:
  - Search: "ViewModel overview" / "StateFlow in Android" / "ViewModel factories"
  - URL: `developer.android.com/topic/libraries/architecture/viewmodel`
  - URL: `developer.android.com/kotlin/flow/stateflow-and-sharedflow`
  - URL: `developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories`

### [app/src/main/java/com/schwisolutions/librarymanagement/viewmodel/DetailViewModel.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/viewmodel/DetailViewModel.kt)
- **Purpose**: ViewModel for `DetailsScreen`.
- **Key method**: `getBookStream(id: Int): Flow<Book>` delegates to `bookRepository.getBookStream(id)`.
- **Official documentation**:
  - Search: "ViewModel and Flow"
  - URL: `developer.android.com/topic/libraries/architecture/viewmodel`

---

## 6. UI and Navigation Layer

### [app/src/main/java/com/schwisolutions/librarymanagement/ui/LibraryNavigation.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/LibraryNavigation.kt)
- **Purpose**: Type-safe navigation configuration.
- **Routes**:
  - `@Serializable object PinRoute`
  - `@Serializable object MainRoute`
  - `@Serializable data class DetailsRoute(val bookId: Int)`
- **Mechanism**:
  - Uses `NavHost` with `composable<PinRoute>`, `composable<MainRoute>`, and `composable<DetailsRoute>`.
  - PIN screen pops `PinRoute` on success: `navController.navigate(MainRoute) { popUpTo(PinRoute) { inclusive = true } }`.
  - Route arguments extracted via `backStackEntry.toRoute<DetailsRoute>()`.
- **Official documentation**:
  - Search: "Navigation with Compose" / "Type safety in Navigation Compose"
  - URL: `developer.android.com/guide/navigation/navigation-with-compose`
  - URL: `developer.android.com/guide/navigation/design/type-safety`

### [app/src/main/java/com/schwisolutions/librarymanagement/ui/PinScreen.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/PinScreen.kt)
- **Purpose**: 4-digit PIN lock screen guarding app access.
- **Key mechanisms**:
  - `rememberSaveable` for PIN state and error status.
  - 4 circular indicator dots showing entry progress.
  - 3x4 keypad layout (1-9, C, 0, Backspace).
  - Validates against `CORRECT_PIN = "1234"` and calls `onLoginSuccess()`.
- **Official documentation**:
  - Search: "State in Jetpack Compose" / "rememberSaveable"
  - URL: `developer.android.com/develop/ui/compose/state#save-ui-state`

### [app/src/main/java/com/schwisolutions/librarymanagement/ui/MainScreen.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/MainScreen.kt)
- **Purpose**: Primary dashboard displaying searchable book list, Add Book FAB, and BookFormDialog.
- **Key components**:
  - `Scaffold`: Top bar and floating action button.
  - `OutlinedTextField`: Real-time in-memory filter on `uiState.bookList`.
  - `LazyColumn`: Renders `BookListItem` cards.
  - `BookListItem`: Card with 50dp thumbnail (`BitmapFactory.decodeFile`), title, year, and `DropdownMenu` (View, Edit, Delete).
  - `BookFormDialog`: Unified dialog for both Add and Edit actions:
    - Camera capture: `rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap -> ... }`
    - Gallery capture: `rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> ... }`
  - `ImageChooserDialog`: Modal to select Camera or Gallery.
  - `AlertDialog`: Confirmation prompt before deleting a book.
- **Official documentation**:
  - Search: "Compose Scaffold" / "Lists and grids Compose" / "Dialogs Compose" / "Activity Results in Compose"
  - URL: `developer.android.com/develop/ui/compose/components/scaffold`
  - URL: `developer.android.com/develop/ui/compose/lists`
  - URL: `developer.android.com/develop/ui/compose/components/dialog`
  - URL: `developer.android.com/develop/ui/compose/libraries#activity-result`
  - URL: `developer.android.com/training/camera/photobasics`

### [app/src/main/java/com/schwisolutions/librarymanagement/ui/DetailScreen.kt](file:///home/schwi/Projects/LibraryManagement/app/src/main/java/com/schwisolutions/librarymanagement/ui/DetailScreen.kt)
- **Purpose**: Displays full metadata and cover for a selected book.
- **Key mechanisms**:
  - Collects `viewModel.getBookStream(bookId).collectAsState(initial = null)`.
  - Shows `CircularProgressIndicator` while loading.
  - Renders 140dp x 180dp cover image with fallback `Icons.Default.Star` icon.
  - Formats title, author, publication year, and genre in a Material 3 card.
- **Official documentation**:
  - Search: "State and Compose collectAsState" / "Card Material 3 Compose"
  - URL: `developer.android.com/develop/ui/compose/state`
  - URL: `developer.android.com/develop/ui/compose/components/card`

---

## 7. Unit Testing

### [app/src/test/java/com/schwisolutions/librarymanagement/ImageStorageHelperTest.kt](file:///home/schwi/Projects/LibraryManagement/app/src/test/java/com/schwisolutions/librarymanagement/ImageStorageHelperTest.kt)
- **Purpose**: Local JVM unit tests for file deletion and error handling in `ImageStorageHelper`.
- **Official documentation**:
  - Search: "Test in Android" / "Build local unit tests"
  - URL: `developer.android.com/training/testing/local-tests`
