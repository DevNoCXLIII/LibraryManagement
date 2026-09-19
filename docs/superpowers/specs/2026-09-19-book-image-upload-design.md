# Book Image Upload Design (Camera and Gallery)

## Overview
Add book cover image upload functionality using only official Android SDK and Jetpack APIs documented on `developer.android.com`. Users can capture an image via the camera or select an existing photo from the gallery. Images are persisted to internal app storage and displayed in the book list, details screen, and add/edit dialogs.

## Architecture and Data Layer

### 1. Entity and Database Changes
- **`Book` entity (`com.schwisolutions.librarymanagement.data.entity.Book`)**:
  - Add optional field: `val imagePath: String? = null`.
- **`LibraryDatabase` (`com.schwisolutions.librarymanagement.data.LibraryDatabase`)**:
  - Increment schema version from `1` to `2`.
  - Add `.fallbackToDestructiveMigration()` in `Room.databaseBuilder(...)` to rebuild schema cleanly during development without manual SQL scripts.

### 2. Image Storage Helper
Create `ImageStorageHelper` in `com.schwisolutions.librarymanagement.data`:
- `saveBitmap(context: Context, bitmap: Bitmap): String?`: Compresses the given `Bitmap` into a JPEG (quality 90) and writes it to `context.filesDir` with a unique filename (`book_${UUID.randomUUID()}.jpg`). Returns the absolute file path or `null` on failure.
- `saveUri(context: Context, uri: Uri): String?`: Reads an `InputStream` via `context.contentResolver.openInputStream(uri)`, decodes it with `BitmapFactory.decodeStream()`, and writes it using `saveBitmap`.
- `deleteImage(path: String?)`: Deletes the file at `path` if present to clean up replaced or deleted book covers.

### 3. ViewModel Updates
- **`HomeViewModel`**:
  - Update `addNewBook` to take `imagePath: String? = null`.
  - Update `deleteBook` to delete any associated image file from disk via `ImageStorageHelper.deleteImage(book.imagePath)`.

## UI and Components

### 1. Add and Edit Dialogs
- In `AddBookDialog` and `EditBookDialog`:
  - Add a clickable cover preview box (`100.dp` width, `120.dp` height, rounded corners) at the top of the dialog.
  - If `imagePath` is set, decode and display the image with `BitmapFactory.decodeFile(imagePath)?.asImageBitmap()`.
  - If null, display a placeholder icon with an "Add Cover" label.
  - Tapping the preview box opens a chooser dialog offering "Take Photo" and "Choose from Gallery".
  - Register activity result launchers:
    - `ActivityResultContracts.TakePicturePreview()` for camera capture.
    - `ActivityResultContracts.GetContent()` with input `"image/*"` for gallery selection.
  - In `EditBookDialog`, if the user selects a new image, delete the old image file upon saving.

### 2. Main Screen List Item
- Update `BookListItem` in `MainScreen.kt`:
  - Add a `50.dp` by `50.dp` thumbnail on the left side of the row.
  - If `book.imagePath` is valid, render the decoded `ImageBitmap` with `ContentScale.Crop`.
  - Fallback: Display an icon container with `Icons.Default.Book`.

### 3. Detail Screen
- Update `DetailScreen.kt`:
  - Add a centered `140.dp` by `180.dp` cover frame above the book metadata card.
  - If `book.imagePath` is valid, display the decoded `ImageBitmap`.
  - Fallback: Display a large placeholder icon container.

## Error Handling and Edge Cases
- **Missing or invalid files**: `BitmapFactory.decodeFile(path)` returns `null` if the file is absent or corrupted. The UI renders the placeholder icon without crashing.
- **Picker cancellations**: Dismissing the camera or gallery returns `null` to the launcher callback and preserves the existing state.
- **File I/O exceptions**: Wrap file read/write operations in `try/catch` blocks inside `ImageStorageHelper`. Return `null` and log errors on failure.
- **Disk cleanup**: Delete old image files when replacing an image during editing or when deleting a book.

## Verification
- **Compilation**: Run `./gradlew compileDebugKotlin` to verify Compose contracts, Room entities, and launchers compile without errors.
- **Unit tests**: Run `./gradlew test` to ensure existing tests pass.
