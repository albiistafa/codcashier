package dev.codcow.kasirku.features.warung.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.codcow.kasirku.core.data.model.warung.Data
import dev.codcow.kasirku.core.data.model.warung.RequestWarung
import dev.codcow.kasirku.core.domain.repository.WarungRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class warungViewModel @Inject constructor(
    private val warungRepository: WarungRepository,
    private val warungPreferences: WarungPreferences
): ViewModel() {

    private val _warung = MutableStateFlow<Data?>(null)
    val warung = _warung.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val _cachedWarungName = MutableStateFlow<String?>(null)
    val cachedWarungNamee = _cachedWarungName.asStateFlow()

    val cachedWarungName = warungPreferences.warungName.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        observeCachedWarung()
    }

    private fun observeCachedWarung() {
        viewModelScope.launch {
            warungPreferences.warungName.collect { name ->
                _cachedWarungName.value = name
            }
        }
    }

    fun fetchWarungIfChanged() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            warungRepository.getWarung()
                .onSuccess { data ->
                    if (data.name != _cachedWarungName.value) {
                        warungPreferences.saveWarungName(data.name ?: "")
                        _warung.value = data
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            _isLoading.value = false
        }
    }

    fun fetchWarung() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            warungRepository.getWarung()
                .onSuccess { data ->
                    _warung.value = data
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _isLoading.value = false
        }
    }

    fun PostWarung(name: RequestWarung) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                warungRepository.postWarung(name)
                    .onSuccess { updatedName ->
                        println("Menu created successfully: $updatedName")
                        _isSuccess.value = true
                    }
                    .onFailure { exception ->
                        println("Failed to create nameWarung: ${exception.message}")
                        _error.value = exception.message ?: "Gagal membuat nama warung"
                    }
            } catch (e: Exception) {
                println("Exception in updatedWarung: ${e.message}")
                _error.value = "Terjadi kesalahan: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}