package dev.codcow.kasirku.features.warung.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.codcow.kasirku.core.data.local.NotAnImageException
import dev.codcow.kasirku.core.data.local.QrisStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrisViewModel @Inject constructor(private val store: QrisStore) : ViewModel() {
    val path = store.path.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun save(uri: Uri) {
        viewModelScope.launch {
            _saving.value = true
            _error.value = null
            runCatching { store.save(uri) }.onFailure {
                _error.value = if (it is NotAnImageException) "Gambar ini tidak bisa dibuka. Pilih foto QRIS lain dari galeri."
                else "QRIS belum tersimpan. Pastikan memori HP masih cukup, lalu coba lagi."
            }
            _saving.value = false
        }
    }

    fun clear() {
        viewModelScope.launch { store.clear() }
    }
}
