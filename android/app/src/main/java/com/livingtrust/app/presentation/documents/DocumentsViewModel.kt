package com.livingtrust.app.presentation.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.livingtrust.app.data.local.dao.DocumentDao
import com.livingtrust.app.data.local.entity.DocumentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Documents screen.
 *
 * WHY `List<DocumentEntity>` instead of `List<Document>` (a domain model)?
 * - Strictly speaking, the presentation layer should use domain models to stay
 *   independent from the data layer. However, DocumentEntity is simple enough
 *   that creating a separate Document domain model would be over-engineering for v1.
 * - In a larger app or after adding business logic (e.g., filtering, sorting rules),
 *   you'd introduce a Document domain model and a mapper/use case.
 */
data class DocumentsState(
    val documents: List<DocumentEntity> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * ViewModel for the Documents screen.
 *
 * WHY inject DocumentDao directly instead of going through a repository + use case?
 * - For simple CRUD screens with no extra business logic, the full Clean Architecture
 *   stack (DAO → Repository → UseCase → ViewModel) can be unnecessary complexity.
 * - DocumentsViewModel currently does two things: observe documents and delete one.
 *   Both are direct database operations with no validation needed.
 * - Trade-off: this couples the ViewModel to the data layer. If the data source changes
 *   (e.g., move documents to a cloud API), this ViewModel must change too.
 *   For v1, this is an acceptable trade-off for simplicity.
 *
 * WHY does init use viewModelScope.launch instead of making init itself a suspend?
 * - init blocks cannot be suspend functions in Kotlin.
 *   Using viewModelScope.launch starts a coroutine that runs the Flow collection.
 *   The init block returns immediately; the Flow collection continues in the background.
 *
 * WHY collect inside init instead of a separate private function?
 * - For a simple single-operation setup, inline collection in init is readable.
 *   Compare to HomeViewModel where observeTrusts() is a named private function —
 *   that separation makes sense when there are multiple init operations.
 */
@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val documentDao: DocumentDao
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentsState())
    val state: StateFlow<DocumentsState> = _state.asStateFlow()

    init {
        // Subscribe to the Room-backed Flow of all documents.
        // Every time a document is inserted or deleted, Room emits a new list
        // and this lambda updates the state, causing the UI to recompose.
        viewModelScope.launch {
            documentDao.getAllDocuments().collect { docs ->
                _state.value = _state.value.copy(documents = docs)
            }
        }
    }

    /**
     * Deletes a document from the local Room database.
     *
     * WHY does the UI immediately reflect the deletion?
     * - The DAO's deleteDocument() changes the Room database.
     *   Room detects the change and re-emits a new list through getAllDocuments() Flow.
     *   The init block's collect {} receives the new list and updates state.
     *   The UI automatically recomposes without any manual list manipulation here.
     *   This is the "single source of truth" principle: Room drives the UI, not the ViewModel.
     *
     * WHY no error handling for deleteDocument?
     * - Room's delete on a local SQLite database effectively never fails for a valid entity.
     *   Adding try/catch here would be error handling for a scenario that can't realistically happen.
     */
    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch {
            documentDao.deleteDocument(document)
            // No manual state update needed — the Flow subscription above handles it
        }
    }
}
