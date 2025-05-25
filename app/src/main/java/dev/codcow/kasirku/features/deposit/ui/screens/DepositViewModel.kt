package dev.codcow.kasirku.features.deposit.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.codcow.kasirku.core.data.model.deposit.CreateDataDeposit
import dev.codcow.kasirku.core.data.model.deposit.DataDeposit
import dev.codcow.kasirku.core.data.model.deposit.EditDeposit
import dev.codcow.kasirku.core.data.model.deposit.TopUpData
import dev.codcow.kasirku.core.data.model.deposit.UpdateDataDeposit
import dev.codcow.kasirku.core.domain.repository.DepositRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import dev.codcow.kasirku.core.utils.Result
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class DepositViewModel @Inject constructor(
    private val depositRepository: DepositRepository
) : ViewModel() {

    private val _depositItems = MutableStateFlow<List<DataDeposit>>(emptyList())
    val depositItem = _depositItems.asStateFlow()

    private val _editDepoItems = MutableStateFlow<List<EditDeposit>>(emptyList())
    val editItem = _editDepoItems.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedDeposit = MutableStateFlow<DataDeposit?>(null)
    val selectedDeposit: StateFlow<DataDeposit?> = _selectedDeposit.asStateFlow()

    private val _selectedEditDeposit = MutableStateFlow<EditDeposit?>(null)
    val selectedEditDeposit: StateFlow<EditDeposit?> = _selectedEditDeposit.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage = _actionMessage.asStateFlow()


    fun getAllDeposits() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            depositRepository.getAllDeposit().fold(
                onSuccess = { response ->
                    _depositItems.value = response.data ?: emptyList()
                    _isSuccess.value = true
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _error.value = error.message ?: "Unknown error occurred"
                    _isLoading.value = false
                }
            )
        }
    }

    fun createDeposit(createDataDeposit: CreateDataDeposit) = flow {
        _isLoading.value = true
        _error.value = null

        depositRepository.createDeposit(createDataDeposit)
            .onSuccess {
                _isSuccess.value = true
                _actionMessage.value = "Deposit created successfully"
                emit(Result.Success(Unit))
            }
            .onFailure { exception ->
                _error.value = exception.message ?: "Failed to create deposit"
                emit(Result.Error(exception))
            }

        _isLoading.value = false
    }.flowOn(Dispatchers.IO)

    fun getCustomerById(customerId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            depositRepository.getCustomerById(customerId).fold(
                onSuccess = { editDeposit ->
                    Log.d("DepositViewModel", "Success: ${editDeposit}")
                    _selectedEditDeposit.value = editDeposit.data
                    _isLoading.value = false
                },
                onFailure = { error ->
                    _error.value = error.message ?: "Failed to get customer details"
                    _isLoading.value = false
                }
            )
        }
    }

    fun updateCustomer(id: Int, name: String, phoneNumber: String, initialBalance: Double = 0.0) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val updateData = UpdateDataDeposit(
                customer_name = name,
                phone_number = phoneNumber,
                balance = initialBalance.toString(),
            )

            depositRepository.updateCustomerById(id, updateData).fold(
                onSuccess = {
                    _isSuccess.value = true
                    _actionMessage.value = "Customer updated successfully"
                    _isLoading.value = false
                    getCustomerById(id)
                },
                onFailure = { error ->
                    _error.value = error.message ?: "Failed to update customer"
                    _isLoading.value = false
                }
            )
        }
    }

    fun topUpBalance(id: Int, amount: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val topUpData = TopUpData(amount)

            try {
                // Tangkap semua kemungkinan error dari repository
                val result = depositRepository.topUpByCustomerId(id, topUpData)

                result.fold(
                    onSuccess = {
                        _isSuccess.value = true
                        _actionMessage.value = "Top up successful"
                        getCustomerById(id)
                    },
                    onFailure = { exception ->
                        when (exception) {
                            is JsonSyntaxException -> {
                                // Misalnya parsing gagal karena format JSON tidak sesuai ekspektasi
                                _error.value = "Top Up berhasil (respon tidak standar)"
                            }
                            is IOException,
                            is UnknownHostException -> {
                                _error.value = "Periksa koneksi internet Anda"
                            }
                            else -> {
                                _error.value = exception.message ?: "Terjadi kesalahan"
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                // Menangani error parsing dari Retrofit seperti IllegalStateException / JsonSyntaxException
                when (e) {
                    is JsonSyntaxException, is IllegalStateException -> {
                        _error.value = "Top Up berhasil (respon tidak standar)"
                        _isSuccess.value = true
                        _actionMessage.value = "Top up successful"
                        getCustomerById(id)
                    }
                    is IOException, is UnknownHostException -> {
                        _error.value = "Periksa koneksi internet Anda"
                    }
                    else -> {
                        _error.value = e.message ?: "Terjadi kesalahan, silakan coba lagi"
                    }
                }
            }

            _isLoading.value = false
        }
    }


    fun deleteDeposit(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            depositRepository.deleteDeposit(id).fold(
                onSuccess = {
                    _isSuccess.value = true
                    _actionMessage.value = "Deposit deleted successfully"
                    _isLoading.value = false
                    getAllDeposits()
                },
                onFailure = { error ->
                    _error.value = error.message ?: "Failed to delete deposit"
                    _isLoading.value = false
                }
            )
        }
    }

    fun clearMessages() {
        _error.value = null
        _actionMessage.value = null
        _isSuccess.value = false
    }

    fun resetSelectedDeposit() {
        _selectedDeposit.value = null
    }

    fun searchDepositItems(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            depositRepository.searchDeposit(query).onSuccess { searchResult ->
                var filteredDeposit = searchResult

                _depositItems.value = filteredDeposit
                _isLoading.value = false

            }.onFailure { exception ->
                _error.value = "Search failed: ${exception.message}"
                _isLoading.value = false
            }
        }
    }
}
