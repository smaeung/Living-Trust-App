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

data class DocumentsState(
    val documents: List<DocumentEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val documentDao: DocumentDao
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentsState())
    val state: StateFlow<DocumentsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            documentDao.getAllDocuments().collect { docs ->
                _state.value = _state.value.copy(documents = docs)
            }
        }
    }

    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch {
            documentDao.deleteDocument(document)
        }
    }
}
