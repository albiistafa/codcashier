package dev.codcow.kasirku.features.kategoriManager.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.codcow.kasirku.core.data.model.kategori.CreateKategoriRequest
import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import dev.codcow.kasirku.core.domain.repository.KategoriRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import dev.codcow.kasirku.core.utils.Result
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class KategoriManagerViewModel @Inject constructor(
    private val kategoriRepository: KategoriRepository
) : ViewModel() {
    private val _kategoriItems = MutableStateFlow<List<DataKategori>>(emptyList())
    val kategoriItems = _kategoriItems.asStateFlow()

    private val _selectedKategori = MutableStateFlow<DataKategori?>(null)
    val selectedKategori = _selectedKategori.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var allKategoriItems = listOf<DataKategori>()

    fun fetchKategoriItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            kategoriRepository.getAllKategori().onSuccess { kategoris ->
                _kategoriItems.value = kategoris
                allKategoriItems = kategoris
                _isLoading.value = false
            }.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun getKategoriById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            kategoriRepository.getKategoriById(id).onSuccess { response ->
                _selectedKategori.value = response.data
                _isLoading.value = false
            }.onFailure { exception ->
                _error.value = exception.message
                _selectedKategori.value = null
                _isLoading.value = false
            }
        }
    }

    fun deleteKategoriItem(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            kategoriRepository.deleteKategori(id).onSuccess {
                // Refresh the list after deletion
                fetchKategoriItems()
            }.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun createKategori(request: CreateKategoriRequest) = flow {
        _isLoading.value = true
        _error.value = null

        kategoriRepository.createKategori(request)
            .onSuccess {
                emit(Result.Success(Unit))
            }
            .onFailure { exception ->
                _error.value = exception.message
                emit(Result.Error(exception))
            }

        _isLoading.value = false
    }.flowOn(Dispatchers.IO)

    fun updateKategori(id: Int, kategori: DataKategori) {
        viewModelScope.launch {
            _isLoading.value = true
            kategoriRepository.updateKategori(id, kategori).onSuccess {
                _isSuccess.value = true
                fetchKategoriItems()
            }.onFailure { exception ->
                _error.value = exception.message
                _isLoading.value = false
            }
        }
    }

    fun searchKategoriItems(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            kategoriRepository.searchKategori(query).onSuccess { searchResult ->
                var filteredKategori = searchResult

                _kategoriItems.value = filteredKategori
                _isLoading.value = false

            }.onFailure { exception ->
                _error.value = "Search failed: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

}