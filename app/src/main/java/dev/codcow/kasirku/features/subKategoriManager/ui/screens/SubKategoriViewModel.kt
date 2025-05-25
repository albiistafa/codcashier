package dev.codcow.kasirku.features.subKategoriManager.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.codcow.kasirku.core.data.model.subkategori.CreateSubkategoriRequest
import dev.codcow.kasirku.core.data.model.subkategori.DataSubkategori
import dev.codcow.kasirku.core.domain.repository.SubKategoriRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubKategoriViewModel @Inject constructor(
    private val subKategoriRepository: SubKategoriRepository
) : ViewModel() {
    private val _subkategoriItems = MutableStateFlow<List<DataSubkategori>>(emptyList())
    val subkategoriItems = _subkategoriItems.asStateFlow()

    private val _selectedSubKategori = MutableStateFlow<DataSubkategori?>(null)
    val selectedSubKategori: StateFlow<DataSubkategori?> = _selectedSubKategori.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Keep a copy of all items for filtering purposes
    private var allSubKategoriItems = listOf<DataSubkategori>()


    fun fetchSubKategoriItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            subKategoriRepository.getAllSubKategori().onSuccess { subKategori ->
                _subkategoriItems.value = subKategori
                allSubKategoriItems = subKategori
                _isLoading.value = false
            }.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun deleteSubKategoriItem(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            subKategoriRepository.deleteSubKategori(id).onSuccess {
                // Refresh the list after deletion
                fetchSubKategoriItems()
            }.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun searchSubKategoriItem(query: String) {
        if (query.isEmpty()) {
            _subkategoriItems.value = allSubKategoriItems
            return
        }

        val filteredItems = allSubKategoriItems.filter {
            it.name.contains(query, ignoreCase = true)
        }
        _subkategoriItems.value = filteredItems
    }

    // Additional function to create a new kategori
    fun createSubKategori(request: CreateSubkategoriRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            subKategoriRepository.createSubKategori(request).onSuccess {
                _isLoading.value = false
                _isSuccess.value = true
                fetchSubKategoriItems() // Refresh the list after creation
            }.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun getSubKategoriById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            subKategoriRepository.getSubKategoriById(id).onSuccess { response ->
                _selectedSubKategori.value = response.data
                _isLoading.value = false
            }.onFailure { exception ->
                _error.value = exception.message
                _selectedSubKategori.value = null
                _isLoading.value = false
            }
        }
    }

    // Additional function to update an existing kategori
    fun updateSubKategori(id: Int, subkategori: DataSubkategori) {
        viewModelScope.launch {
            _isLoading.value = true

            subKategoriRepository.updateSubKategori(id, subkategori).onSuccess {
                _isSuccess.value = true
                fetchSubKategoriItems()
            }.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun searchSubKategoriItems(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            subKategoriRepository.searchSub(query).onSuccess { searchResult ->
                var filteredSub = searchResult

                _subkategoriItems.value = filteredSub
                _isLoading.value = false

            }.onFailure { exception ->
                _error.value = "Search failed: ${exception.message}"
                _isLoading.value = false
            }
        }
    }
}