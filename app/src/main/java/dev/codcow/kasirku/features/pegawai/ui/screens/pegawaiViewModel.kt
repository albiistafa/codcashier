package dev.codcow.kasirku.features.pegawai.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.codcow.kasirku.core.data.model.pegawai.CreatePegawaiRequest
import dev.codcow.kasirku.core.data.model.pegawai.Data
import dev.codcow.kasirku.core.domain.repository.PegawaiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PegawaiManagerViewModel @Inject constructor(
    private val pegawaiRepository: PegawaiRepository
) : ViewModel() {
    private val _pegawaiItems = MutableStateFlow<List<Data>>(emptyList())
    val pegawaiItems = _pegawaiItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _selectedPegawai = MutableStateFlow<Data?>(null)
    val selectedPegawai: StateFlow<Data?> = _selectedPegawai.asStateFlow()

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: String? get() = _errorMessage.value

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun fetchPegawaiItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            pegawaiRepository.getAllPegawai().onSuccess { pegawais ->
                _pegawaiItems.value = pegawais
                _isLoading.value = false
            }.onFailure { exception ->
                _error.value = "Failed to load employee data: ${exception.message}"
                _isLoading.value = false
            }
        }
    }

    fun deletePegawaiItem(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            pegawaiRepository.deletePegawai(id)
                .onSuccess {
                    _pegawaiItems.value = _pegawaiItems.value.filterNot { it.id == id }
                    _isSuccess.value = true
                    fetchPegawaiItems() // Refresh the list after deletion
                }
                .onFailure { exception ->
                    _error.value = "Failed to delete employee: ${exception.message}"
                }

            _isLoading.value = false
        }
    }

    fun createPegawai(request: CreatePegawaiRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                pegawaiRepository.createPegawai(request)
                    .onSuccess {
                        _isSuccess.value = true
                        // Refresh employee list
                        fetchPegawaiItems()
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Failed to create employee"
                    }
            } catch (e: Exception) {
                _error.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getPegawaiById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            pegawaiRepository.getPegawaiById(id)
                .onSuccess { response ->
                    _selectedPegawai.value = response.data
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _isLoading.value = false
        }
    }

    fun updatePegawai(id: Int, pegawai: Data) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                pegawaiRepository.updatePegawai(id, pegawai)
                    .onSuccess {
                        _isSuccess.value = true
                        fetchPegawaiItems() // Refresh the list after update
                    }
                    .onFailure { exception ->
                        _error.value = exception.message ?: "Failed to update employee"
                    }
            } catch (e: Exception) {
                _error.value = "An error occurred: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchPegawaiItems(query: String) {
        viewModelScope.launch {
            _isLoading.value = true

            if (query.isEmpty()) {
                fetchPegawaiItems()
                return@launch
            }

            pegawaiRepository.searchPegawai(query)
                .onSuccess { searchResult ->
                    _pegawaiItems.value = searchResult
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = "Search failed: ${exception.message}"
                    _isLoading.value = false
                }
        }
    }

    // Reset success state
    fun resetSuccessState() {
        _isSuccess.value = false
    }
}