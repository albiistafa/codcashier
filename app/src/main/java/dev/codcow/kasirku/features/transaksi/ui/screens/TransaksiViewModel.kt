package dev.codcow.kasirku.features.transaksi.ui.screens

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.codcow.kasirku.core.data.model.addToCart.TransactionRequest
import dev.codcow.kasirku.core.data.model.pemasukan.TransactionDetail
import dev.codcow.kasirku.core.data.model.pemasukan.TransactionPemasukan
import dev.codcow.kasirku.core.data.model.pengeluaran.PengeluaranRequest
import dev.codcow.kasirku.core.data.model.pengeluaran.TransactionPengeluaran
import dev.codcow.kasirku.core.data.model.transaksi.Data
import dev.codcow.kasirku.core.data.model.transaksi.DataTransaksi
import dev.codcow.kasirku.core.data.model.transaksi.MenuItemUpdate
import dev.codcow.kasirku.core.data.model.transaksi.Pagination
import dev.codcow.kasirku.core.data.model.transaksi.Transaction
import dev.codcow.kasirku.core.data.model.transaksi.TransactionGetByIdResponse
import dev.codcow.kasirku.core.data.model.transaksi.TransactionUpdateRequest
import dev.codcow.kasirku.core.data.model.transaksi.TransactionX
import dev.codcow.kasirku.core.data.model.transaksi.TransactionDetailX
import dev.codcow.kasirku.core.data.model.transaksi.TransactionUpdateStatus
import dev.codcow.kasirku.core.domain.repository.TransaksiRepository
import dev.codcow.kasirku.core.domain.repository.DepositRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.Serializable
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlin.onFailure

@HiltViewModel
class TransaksiViewModel @Inject constructor(
    private val transaksiRepository: TransaksiRepository,
    private val depositRepository: DepositRepository,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    private val _transaksiItems = MutableStateFlow<DataTransaksi?>(null)
    val transaksiItems = _transaksiItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _errorPemasukan = MutableStateFlow<String?>(null)
    val errorPemasukan = _errorPemasukan.asStateFlow()

    private val _errorPengeluaran = MutableStateFlow<String?>(null)
    val errorPengeluaran = _errorPengeluaran.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val _totalPemasukan = MutableStateFlow("Rp 0")
    val totalPemasukan = _totalPemasukan.asStateFlow()

    private val _todayLunasTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val todayLunasTransactions = _todayLunasTransactions.asStateFlow()

    // In TransaksiViewModel
    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions = _allTransactions.asStateFlow()

    private val _allPengeluaran = MutableStateFlow<List<TransactionPengeluaran>>(emptyList())
    val allPengeluaran = _allPengeluaran.asStateFlow()

    private val _allTransactionsn = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactionsn = _allTransactionsn.asStateFlow()

    // Add a state flow for transactions filtered by date
    private val _filteredTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val filteredTransactions = _filteredTransactions.asStateFlow()

    private val _filteredSearchTransactions = MutableStateFlow<List<TransactionX>>(emptyList())
    val filteredSearchTransactions = _filteredSearchTransactions.asStateFlow()

    private val _filteredTransactionsn = MutableStateFlow<List<Transaction>>(emptyList())
    val filteredTransactionsn = _filteredTransactionsn.asStateFlow()

    // Add a state flow for date filter
    private val _dateFilter = MutableStateFlow(DateFilter.TODAY)
    val dateFilter = _dateFilter.asStateFlow()

    private val _currentTransactionId = MutableStateFlow<Int?>(null)
    val currentTransactionId: StateFlow<Int?> = _currentTransactionId

    private val _currentStatusDetail = MutableStateFlow<String?>(null)
    val currentStatusDetail = _currentStatusDetail.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _currentRekapPemasukanStartDate = MutableStateFlow("")
    val currentRekapPemasukanStartDate = _currentRekapPemasukanStartDate.asStateFlow()

    private val _currentRekapPemasukanEndDate = MutableStateFlow("")
    val currentRekapPemasukanEndDate = _currentRekapPemasukanEndDate.asStateFlow()

    private val _currentRekapPengeluaranStartDate = MutableStateFlow("")
    val currentRekapPengeluaranStartDate = _currentRekapPengeluaranStartDate.asStateFlow()

    private val _currentRekapPengeluaranEndDate = MutableStateFlow("")
    val currentRekapPengeluaranEndDate = _currentRekapPengeluaranEndDate.asStateFlow()

    private val CUSTOMER_HISTORY_KEY = "customer_name_history"

    // Add this mutable state flow for customer name history
    private val _customerNameHistory = MutableStateFlow<List<String>>(emptyList())
    val customerNameHistory = _customerNameHistory.asStateFlow()

    // Initialize in init block
    init {
        loadCustomerNameHistory()
    }

    // Load customer name history from SharedPreferences
    private fun loadCustomerNameHistory() {
        val historyJson = sharedPreferences.getString(CUSTOMER_HISTORY_KEY, null)
        if (historyJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val history = Gson().fromJson<List<String>>(historyJson, type)
                _customerNameHistory.value = history
            } catch (e: Exception) {
                Log.e("TransaksiViewModel", "Error loading customer history", e)
                _customerNameHistory.value = emptyList()
            }
        }
    }

    // Add customer name to history
    fun addToCustomerNameHistory(name: String) {
        if (name.isBlank()) return

        val currentHistory = _customerNameHistory.value.toMutableList()

        // Remove if already exists to avoid duplicates
        currentHistory.remove(name)

        // Add to the beginning of the list
        currentHistory.add(0, name)

        // Limit the history size to 10 items
        val trimmedHistory = currentHistory.take(10)

        // Update the state flow
        _customerNameHistory.value = trimmedHistory

        // Save to SharedPreferences
        val historyJson = Gson().toJson(trimmedHistory)
        sharedPreferences.edit().putString(CUSTOMER_HISTORY_KEY, historyJson).apply()
    }


    fun searchRekapPemasukan(query: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorPemasukan.value = null
            _searchQuery.value = query
            _currentRekapPemasukanStartDate.value = startDate
            _currentRekapPemasukanEndDate.value = endDate

            transaksiRepository.searchRekapAcrossPages(query, startDate, endDate, "pemasukan")
                .onSuccess { dataSearch ->
                    val pemasukanList = dataSearch.transactions.map { transaction ->
                        val pemasukanTransactionDetails = transaction.transaction_details?.map { detail ->
                            TransactionDetail(
                                menu_id = detail.menu_id ?: 0,
                                menu_name = detail.menu_name ?: "Item Tidak Diketahui", // Default value untuk non-null parameter
                                menu_price = detail.menu_price ?: 0,
                                quantity = detail.quantity ?: 0,
                                subtotal = detail.subtotal ?: 0
                            )
                        } ?: emptyList()

                        TransactionPemasukan(
                            id = transaction.id,
                            nama_transaksi = transaction.nama_transaksi,
                            total_amount = transaction.total_amount,
                            payment_method = transaction.payment_method,
                            status = transaction.status,
                            created_at = transaction.created_at,
                            updated_at = transaction.updated_at,
                            transaction_details = pemasukanTransactionDetails,
                            user_id = transaction.user_id
                        )
                    }
                    _allPemasukan.value = pemasukanList
                    Log.d("TransaksiVM", "Filtered pemasukan results: ${pemasukanList.size}")
                }
                .onFailure { exception ->
                    when (exception) {
                        is JsonSyntaxException -> {
                            _errorPemasukan.value = "Tidak ada data pemasukan"
                        }
                        is IOException,
                        is UnknownHostException -> {
                            _errorPemasukan.value = "Periksa koneksi internet Anda"
                        }
                        else -> {
                            _errorPemasukan.value = exception.message ?: "Terjadi kesalahan"
                        }
                    }
                }


            _isLoading.value = false
        }
    }

    fun searchRekapPengeluaran(query: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorPengeluaran.value = null
            _searchQuery.value = query
            _currentRekapPengeluaranStartDate.value = startDate
            _currentRekapPengeluaranEndDate.value = endDate

            transaksiRepository.searchRekapAcrossPages(query, startDate, endDate, "pengeluaran")
                .onSuccess { dataSearch ->
                    val pengeluaranList = dataSearch.transactions.map { transaction ->
                        val pengeluaranTransactionDetails = transaction.transaction_details?.map { detail ->
                            dev.codcow.kasirku.core.data.model.pengeluaran.TransactionDetail(
                                menu_id = detail.menu_id ?: 0,
                                menu_name = detail.menu_name ?: "Item Tidak Diketahui", // Default value untuk non-null parameter
                                menu_price = detail.menu_price ?: 0,
                                quantity = detail.quantity ?: 0,
                                subtotal = detail.subtotal ?: 0
                            )
                        } ?: emptyList()

                        TransactionPengeluaran(
                            id = transaction.id,
                            nama_transaksi = transaction.nama_transaksi,
                            total_amount = transaction.total_amount,
                            payment_method = transaction.payment_method ,
                            status = transaction.status ,
                            created_at = transaction.created_at,
                            updated_at = transaction.updated_at ,
                            transaction_details = pengeluaranTransactionDetails,
                            user_id = transaction.user_id
                        )
                    }
                    _allPengeluaran.value = pengeluaranList
                    Log.d("TransaksiVM", "Filtered pengeluaran results: ${pengeluaranList.size}")
                }
                .onFailure { exception ->
                    when (exception) {
                        is JsonSyntaxException -> {
                            _errorPengeluaran.value = "Tidak ada data pemasukan"
                        }
                        is IOException,
                        is UnknownHostException -> {
                            _errorPengeluaran.value = "Periksa koneksi internet Anda"
                        }
                        else -> {
                            _errorPengeluaran.value = exception.message ?: "Terjadi kesalahan"
                        }
                    }
                }

            _isLoading.value = false
        }
    }

    fun applyRekapPemasukanFilters(startDate: String, endDate: String) {
        _currentRekapPemasukanStartDate.value = startDate
        _currentRekapPemasukanEndDate.value = endDate

        if (startDate.isEmpty() && endDate.isEmpty() && _searchQuery.value.isEmpty()) {
            fetchAllPemasukanAcrossPages(_currentRekapPemasukanStartDate.value, _currentRekapPemasukanEndDate.value)
        } else {
            searchRekapPemasukan(_searchQuery.value, startDate, endDate)
        }
    }

    fun applyRekapPengeluaranFilters(startDate: String, endDate: String) {
        _currentRekapPengeluaranStartDate.value = startDate
        _currentRekapPengeluaranEndDate.value = endDate

        if (startDate.isEmpty() && endDate.isEmpty() && _searchQuery.value.isEmpty()) {
            fetchAllPengeluaranAcrossPages(_currentRekapPengeluaranStartDate.value, _currentRekapPengeluaranEndDate.value)
        } else {
            searchRekapPengeluaran(_searchQuery.value, startDate, endDate)
        }
    }

    fun createTransaction(transactionData: TransactionRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            transaksiRepository.createTransaksi(transactionData)
                .onSuccess {response ->
                    _currentTransactionId.update {
                        Log.d("TransaksiViewModel", "Updating currentTransactionId from ${_currentTransactionId.value} to ${response.data.id}")
                        response.data.id }
                    _currentStatusDetail.update { response.data.status }
                    _isSuccess.update { true }
                    _isLoading.update { false }
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to create transaction"
                }

            _isLoading.value = false
        }
    }

    enum class DateFilter {
        TODAY,
        YESTERDAY,
        LAST_7_DAYS,
        THIS_MONTH,
        ALL,
        CUSTOM_RANGE
    }

    fun fetchAllTransaksiAcrossPages(paymentMethod: String, status: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            _currentPaymentMethod.value = paymentMethod
            _currentStatus.value = status

            Log.d("TransaksiVM", "Starting to fetch all transactions across pages...")

            transaksiRepository.getAllTransaksiAcrossPages(paymentMethod, status, startDate, endDate)
                .onSuccess { data ->
                    Log.d("TransaksiVM", "Successfully fetched data. Structure: ${data.javaClass.simpleName}")
                    Log.d("TransaksiVM", "Total transactions count: ${data.transactions.size}")

                    // Log first 3 transactions as sample (or all if less than 3)
                    val sampleSize = min(3, data.transactions.size)
                    data.transactions.take(sampleSize).forEachIndexed { index, transaction ->
                        Log.d("TransaksiVM", """
                    |Transaction #${index + 1}:
                    |ID: ${transaction.id}
                    |Name: ${transaction.nama_transaksi}
                    |Payment Method: ${transaction.payment_method}
                    |Status: ${transaction.status}
                    |Amount: ${transaction.total_amount}
                    |Details Count: ${transaction.transaction_details.size}
                    """.trimMargin())
                    }

                    _allTransactions.value = data.transactions
                    Log.d("TransaksiVM", "Updated _allTransactions with ${data.transactions.size} items")
                }
                .onFailure { exception ->
                    when (exception) {
                        is JsonSyntaxException -> {
                            _error.value = "Tidak ada data transaksi"
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

            _isLoading.value = false
            Log.d("TransaksiVM", "Fetch operation completed")
        }
    }

    fun fetchAllTransaksiNoFilter() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            Log.d("TransaksiVM", "Starting to fetch all transactions across pages...")

            transaksiRepository.getAllTransaksiAcrossPagesNoFilter()
                .onSuccess { data ->
                    Log.d("TransaksiVM", "Successfully fetched data. Structure: ${data.javaClass.simpleName}")
                    Log.d("TransaksiVM", "Total transactions count: ${data.transactions.size}")

                    // Log first 3 transactions as sample (or all if less than 3)
                    val sampleSize = min(3, data.transactions.size)
                    data.transactions.take(sampleSize).forEachIndexed { index, transaction ->
                        Log.d("TransaksiVM", """
                    |Transaction #${index + 1}:
                    |ID: ${transaction.id}
                    |Name: ${transaction.nama_transaksi}
                    |Payment Method: ${transaction.payment_method}
                    |Status: ${transaction.status}
                    |Amount: ${transaction.total_amount}
                    |Details Count: ${transaction.transaction_details.size}
                    """.trimMargin())
                    }

                    _allTransactions.value = data.transactions
                    Log.d("TransaksiVM", "Updated _allTransactions with ${data.transactions.size} items")
                }
                .onFailure { exception ->
                    when (exception) {
                        is JsonSyntaxException -> {
                            _error.value = "Tidak ada data transaksi"
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

            _isLoading.value = false
            Log.d("TransaksiVM", "Fetch operation completed")
        }
    }

    fun fetchAllPengeluaranAcrossPages(
        startDate: String,
        endDate: String
    ){
        viewModelScope.launch {
            _isLoading.value = true
            _errorPengeluaran.value = null

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            val finalStartDate = if (startDate.isNotEmpty()) startDate else today
            val finalEndDate = if (endDate.isNotEmpty()) endDate else today

            _currentRekapPengeluaranStartDate.value = finalStartDate
            _currentRekapPengeluaranEndDate.value = finalEndDate

            transaksiRepository.getPengeluaranAcrossPage(finalStartDate, finalEndDate)
                .onSuccess { data ->
                    _allPengeluaran.value = data.transactions
                    Log.d("TransaksiVM", "Fetched ${data.transactions.size} total pengeluaran")
                }
                .onFailure { exception ->
                    when (exception) {
                        is JsonSyntaxException -> {
                            _errorPengeluaran.value = "Tidak ada data pengeluaran"
                        }
                        is IOException,
                        is UnknownHostException -> {
                            _errorPengeluaran.value = "Periksa koneksi internet Anda"
                        }
                        else -> {
                            _errorPengeluaran.value = exception.message ?: "Terjadi kesalahan, silahkan coba lagi"
                        }
                    }
                }
            _isLoading.value = false
        }
    }

    private val _startDateFilter = MutableStateFlow<LocalDate?>(null)
    val startDateFilter = _startDateFilter.asStateFlow()

    private val _endDateFilter = MutableStateFlow<LocalDate?>(null)
    val endDateFilter = _endDateFilter.asStateFlow()


    private suspend fun findCustomerIdByTransactionName(transactionName: String): Int? {
        return try {
            depositRepository.searchDeposit(transactionName)
                .getOrNull()
                ?.firstOrNull()
                ?.customer_id
        } catch (e: Exception) {
            Log.e("TransaksiViewModel", "Error searching customer: ${e.message}")
            null
        }
    }

    fun updateTransaction(
        id: Int,
        status: String,
        customer_id: Int?,
        is_delivered: String,
        paymentMethod: String,
        items: List<MenuItemUpdate>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            // Update state lokal terlebih dahulu
            _allTransactions.update { currentList ->
                currentList.map { transaction ->
                    if (transaction.id == id) {
                        transaction.copy(status = status)
                    } else {
                        transaction
                    }
                }
            }

            // Jika metode pembayaran adalah deposit, cari customer_id berdasarkan nama transaksi
            val customerId = if (paymentMethod.lowercase() == "deposit") {
                val transaction = _allTransactions.value.find { it.id == id }
                transaction?.let { findCustomerIdByTransactionName(it.nama_transaksi) }
            } else {
                null
            }

            Log.d("TransaksiViewModel", "Found customer_id: $customerId for transaction $id")

            val updateRequest = TransactionUpdateRequest(
                status = status,
                payment_method = paymentMethod,
                items = items,
                customer_id = customer_id,
                is_delivered = is_delivered
            )

            transaksiRepository.updateTransaksi(id, updateRequest)
                .onSuccess {
                    _isSuccess.value = true
                    fetchAllTransaksiNoFilter()
                }
                .onFailure { exception ->
                    _allTransactions.update { currentList ->
                        currentList.map { transaction ->
                            if (transaction.id == id) {
                                transaction.copy(status = "pending")
                            } else {
                                transaction
                            }
                        }
                    }
                    _error.value = exception.message ?: "Gagal update transaksi"
                }

            _isLoading.value = false
        }
    }

    fun updateStatus(
        id: Int,
        status: String,
        is_delivered: String,
        paymentMethod: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            _allTransactions.update { currentList ->
                currentList.map { transaction ->
                    if (transaction.id == id) {
                        transaction.copy(status = status)
                        transaction.copy(is_delivered = is_delivered)
                    } else {
                        transaction
                    }
                }
            }

            // Jika metode pembayaran adalah deposit, cari customer_id berdasarkan nama transaksi
            val customerId = if (paymentMethod?.lowercase() == "deposit") {
                val transaction = _allTransactions.value.find { it.id == id }
                transaction?.let { findCustomerIdByTransactionName(it.nama_transaksi) }
            } else {
                null
            }

            Log.d("TransaksiViewModel", "Found customer_id: $customerId for transaction $id")

            val updateStatus = TransactionUpdateStatus(
                status = status,
                is_delivered = is_delivered
            )

            transaksiRepository.updateStatus(id, updateStatus)
                .onSuccess {
                    _isSuccess.value = true
                    fetchAllTransaksiNoFilter()
                }
                .onFailure { exception ->
                    _allTransactions.update { currentList ->
                        currentList.map { transaction ->
                            if (transaction.id == id) {
                                transaction.copy(status = "pending")
                                transaction.copy(is_delivered = "no")
                            } else {
                                transaction
                            }
                        }
                    }
                    when (exception) {
                        is JsonSyntaxException -> {
                            _error.value = null
                        }

                        is IOException,
                        is UnknownHostException -> {
                            _error.value = "Periksa koneksi internet Anda"
                        }

                        else -> {
                            _error.value =
                                exception.message ?: "Terjadi kesalahan, silahkan coba lagi"
                        }
                    }
                }
            _isSuccess.value = true
            _isLoading.value = false
        }
    }



    private val _currentTransaction = MutableStateFlow<Data?>(null)
    val currentTransaction = _currentTransaction.asStateFlow()

    fun getTransactionById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            transaksiRepository.getTransaksiById(id)
                .onSuccess { response ->
                    _currentTransaction.value = response.data
                    Log.d("TRANSACTION_DETAILS", "Details: ${response.data}")
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Failed to fetch transaction details"
                }

            _isLoading.value = false
        }
    }

    fun resetState() {
        _isSuccess.value = false
        _error.value = null
    }

    private val _totalPengeluaran = MutableStateFlow("Rp 0")
    val totalPengeluaran = _totalPengeluaran.asStateFlow()

    private val _filteredPengeluaran = MutableStateFlow<List<TransactionPengeluaran>>(emptyList())
    val filteredPengeluaran = _filteredPengeluaran.asStateFlow()

    private fun calculateTotalPengeluaran(pengeluarans: List<TransactionPengeluaran>) {
        val total = pengeluarans.sumOf {
            it.total_amount.replace("Rp ", "").replace(".", "").toIntOrNull() ?: 0
        }
        _totalPengeluaran.value = "Rp ${total.toString().chunked(3).joinToString(".")}"
    }

    private val _allPemasukan = MutableStateFlow<List<TransactionPemasukan>>(emptyList())
    val allPemasukan: StateFlow<List<TransactionPemasukan>> = _allPemasukan

    private val _filteredPemasukan = MutableStateFlow<List<TransactionPemasukan>>(emptyList())
    val filteredPemasukan: StateFlow<List<TransactionPemasukan>> = _filteredPemasukan


    fun fetchAllPemasukanAcrossPages(
        startDate: String,
        endDate: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorPemasukan.value = null

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            val finalStartDate = if (startDate.isNotEmpty()) startDate else today
            val finalEndDate = if (endDate.isNotEmpty()) endDate else today

            _currentRekapPemasukanStartDate.value = finalStartDate
            _currentRekapPemasukanEndDate.value = finalEndDate


            transaksiRepository.getPemasukanAcrossPage(finalStartDate, finalEndDate)
                .onSuccess { data ->
                    _allPemasukan.value = data.transactions
                    Log.d("TransaksiVM", "Fetched ${data.transactions.size} total pemasukan")
                }
                .onFailure { exception ->
                    when (exception) {
                        is JsonSyntaxException -> {
                            _errorPemasukan.value = "Tidak ada data pemasukan"
                        }
                        is IOException,
                        is UnknownHostException -> {
                            _errorPemasukan.value = "Periksa koneksi internet Anda"
                        }
                        else -> {
                            _errorPemasukan.value = exception.message ?: "Terjadi kesalahan, silahkan coba lagi"
                        }
                    }
                }

            _isLoading.value = false
        }
    }

    private val _pengeluaranSuccess = MutableStateFlow(false)
    val pengeluaranSuccess = _pengeluaranSuccess.asStateFlow()

    fun createPengeluaran(namaNota: String, hargaNota: Int, tanggalPengeluaran: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _pengeluaranSuccess.value = false


            val request = PengeluaranRequest(
                nama_barang = namaNota.trim(),
                harga_barang = hargaNota,
                tanggal_pengeluaran = tanggalPengeluaran.trim()
            )

            transaksiRepository.createPengeluaran(request)
                .onSuccess {
                    _pengeluaranSuccess.value = true
                    fetchAllPengeluaranAcrossPages(_currentRekapPengeluaranStartDate.value, _currentRekapPengeluaranEndDate.value) // Refresh pengeluaran data
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Gagal menambahkan pengeluaran"
                }

            _isLoading.value = false
        }
    }

    private val _isFilterVisible = MutableStateFlow(false)
    val isFilterVisible: StateFlow<Boolean> = _isFilterVisible

    // Current search parameters
    private val _currentSearchQuery = MutableStateFlow("")
    val currentSearchQuery: StateFlow<String> = _currentSearchQuery

    private val _currentPaymentMethod = MutableStateFlow("")
    val currentPaymentMethod: StateFlow<String> = _currentPaymentMethod

    private val _currentStatus = MutableStateFlow("")
    val currentStatus: StateFlow<String> = _currentStatus

    private val _currentStartDate = MutableStateFlow("")
    val currentStartDate: StateFlow<String> = _currentStartDate

    private val _currentEndDate = MutableStateFlow("")
    val currentEndDate: StateFlow<String> = _currentEndDate

    fun toggleFilterSidebar() {
        _isFilterVisible.value = !_isFilterVisible.value
    }

    fun closeFilterSidebar() {
        _isFilterVisible.value = false
    }

    fun updateSearchQuery(query: String) {
        _currentSearchQuery.value = query
    }

    fun resetFilters() {
        _currentSearchQuery.value = ""
        _currentPaymentMethod.value = ""
        _currentStatus.value = ""
        searchTransactions("", "", "")
    }

    fun searchTransactions(query: String, paymentMethod: String, status: String) {
        _currentSearchQuery.value = query
        _currentPaymentMethod.value = paymentMethod
        _currentStatus.value = status

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            transaksiRepository.searchTransactionAcrossPages(query, paymentMethod, status)
                .onSuccess { dataSearch ->
                    val pemasukanList = dataSearch.transactions.map { transaction ->
                        val pemasukanTransactionDetails = transaction.transaction_details?.map { detail ->
                            TransactionDetail(
                                menu_id = detail.menu_id ?: 0,
                                menu_name = detail.menu_name ?: "Item Tidak Diketahui", // Default value untuk non-null parameter
                                menu_price = detail.menu_price ?: 0,
                                quantity = detail.quantity ?: 0,
                                subtotal = detail.subtotal ?: 0
                            )
                        } ?: emptyList()

                        TransactionPemasukan(
                            id = transaction.id,
                            nama_transaksi = transaction.nama_transaksi,
                            total_amount = transaction.total_amount,
                            payment_method = transaction.payment_method,
                            status = transaction.status,
                            created_at = transaction.created_at,
                            updated_at = transaction.updated_at,
                            transaction_details = pemasukanTransactionDetails,
                            user_id = transaction.user_id
                        )
                    }
                    _allPemasukan.value = pemasukanList
                    Log.d("TransaksiVM", "Filtered pemasukan results: ${pemasukanList.size}")
                }
                .onFailure { exception ->
                    when (exception) {
                        is JsonSyntaxException -> {
                            _error.value = "Tidak ada data transaksi"
                        }
                        is IOException,
                        is UnknownHostException -> {
                            _error.value = "Periksa koneksi internet Anda"
                        }
                        else -> {
                            _error.value = exception.message ?: "Terjadi kesalahan, silahkan coba lagi"
                        }
                    }
                }

            _isLoading.value = false
        }
    }
//
    fun applyFiltersSide(
        query: String = _currentSearchQuery.value,
        paymentMethod: String = _currentPaymentMethod.value,
        status: String = _currentStatus.value
    ) {
        _currentSearchQuery.value = query
        _currentPaymentMethod.value = paymentMethod
        _currentStatus.value = status

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val filtered = _allTransactions.value.filter { transaction ->
                    val matchesQuery = if (query.isNotEmpty()) {
                        transaction.nama_transaksi?.contains(query, ignoreCase = true) ?: false
                    } else true

                    val matchesPaymentMethod = if (paymentMethod.isNotEmpty()) {
                        transaction.payment_method == paymentMethod
                    } else true

                    val matchesStatus = if (status.isNotEmpty()) {
                        transaction.status == status
                    } else true

                    matchesQuery && matchesPaymentMethod && matchesStatus
                }

                _filteredTransactions.value = filtered
                Log.d("TransaksiVM", "Filtered to ${filtered.size} transactions with query: $query, " +
                        "payment method: $paymentMethod, status: $status, date filter: ${_dateFilter.value}")
            } catch (e: Exception) {
                Log.e("TransaksiViewModel", "Error applying filters: ${e.message}", e)
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _pdfFile = MutableStateFlow<File?>(null)
    val pdfFile = _pdfFile.asStateFlow()

    private val _isPdfLoading = MutableStateFlow(false)
    val isPdfLoading = _isPdfLoading.asStateFlow()

    private val _pdfError = MutableStateFlow<String?>(null)
    val pdfError = _pdfError.asStateFlow()

    private val _isPdfSuccess = MutableStateFlow(false)
    val isPdfSuccess = _isPdfSuccess.asStateFlow()

    fun downloadRekapPdf(startDate: String, endDate: String, type: String, fileName: String) {
        viewModelScope.launch {
            _isPdfLoading.value = true
            _pdfError.value = null
            _isPdfSuccess.value = false
            _pdfFile.value = null

            transaksiRepository.downloadRekapPdf(startDate, endDate, type)
                .onSuccess { byteArray ->
                    try {
                        val file = saveByteArrayToFile(byteArray, fileName)
                        _pdfFile.value = file
                        _isPdfSuccess.value = true
                    } catch (e: Exception) {
                        _pdfError.value = "Failed to save PDF: ${e.message}"
                    }
                }
                .onFailure { exception ->
                    _pdfError.value = exception.message ?: "Failed to download PDF"
                }

            _isPdfLoading.value = false
        }
    }

    fun downloadTransaksiPdf(status: String, paymentMethod: String, fileName: String) {
        viewModelScope.launch {
            _isPdfLoading.value = true
            _pdfError.value = null
            _isPdfSuccess.value = false
            _pdfFile.value = null

            transaksiRepository.downloadTransaksiPdf(status, paymentMethod)
                .onSuccess { byteArray ->
                    try {
                        val file = saveByteArrayToFile(byteArray, fileName)
                        _pdfFile.value = file
                        _isPdfSuccess.value = true
                    } catch (e: Exception) {
                        _pdfError.value = "Failed to save PDF: ${e.message}"
                    }
                }
                .onFailure { exception ->
                    _pdfError.value = exception.message ?: "Failed to download PDF"
                }

            _isPdfLoading.value = false
        }
    }

    private fun saveByteArrayToFile(byteArray: ByteArray, fileName: String): File {
        val file = File.createTempFile(fileName, ".pdf")
        FileOutputStream(file).use { outputStream ->
            outputStream.write(byteArray)
        }
        return file
    }

    fun resetPdfState() {
        _isPdfLoading.value = false
        _pdfError.value = null
        _isPdfSuccess.value = false
        _pdfFile.value = null

    }

    fun deleteTransaksi(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            transaksiRepository.deleteTransaksi(id)
                .onSuccess {
                    _isSuccess.value = true
                    fetchAllTransaksiNoFilter()
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Gagal menghapus transaksi"
                }

            _isLoading.value = false
        }
    }

    fun deletePeriode(startDate: String, endDate: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _isSuccess.value = false

            transaksiRepository.deletePeriode(startDate, endDate)
                .onSuccess {
                    _isSuccess.value = true
                    fetchAllTransaksiNoFilter()
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Gagal menghapus periode transaksi"
                }

            _isLoading.value = false
        }
    }
}