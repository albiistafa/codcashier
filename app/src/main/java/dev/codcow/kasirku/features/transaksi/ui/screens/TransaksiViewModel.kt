package dev.codcow.kasirku.features.transaksi.ui.screens

import dev.codcow.kasirku.ui.util.toUserMessage

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
import dev.codcow.kasirku.core.data.model.transaksi.toPaymentUpdate
import dev.codcow.kasirku.core.data.model.transaksi.TransactionUpdateStatus
import dev.codcow.kasirku.core.domain.repository.TransaksiRepository
import dev.codcow.kasirku.core.domain.repository.DepositRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
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


    // In TransaksiViewModel
    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions = _allTransactions.asStateFlow()

    private val _allPengeluaran = MutableStateFlow<List<TransactionPengeluaran>>(emptyList())
    val allPengeluaran = _allPengeluaran.asStateFlow()






    private val _currentTransactionId = MutableStateFlow<Int?>(null)
    val currentTransactionId: StateFlow<Int?> = _currentTransactionId

    private val _currentStatusDetail = MutableStateFlow<String?>(null)
    val currentStatusDetail = _currentStatusDetail.asStateFlow()

    // Unpaid ("pending") orders, shown as a shortcut on the cashier screen.
    // Own flow, so it never flips the checkout's isLoading/error.
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    fun refreshPendingCount() {
        viewModelScope.launch {
            // /transactions/status/{status} 404s on the server; use the same filtered list as the Transaksi tab
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            transaksiRepository.getAllTransaksiAcrossPages("", "pending", today, today).onSuccess { data ->
                _pendingCount.value = data.transactions.size
            }
        }
    }

    /** The date range a Rekap tab last loaded; empty strings until the first load. */
    data class RekapPeriod(val start: String = "", val end: String = "")

    private val _pemasukanPeriod = MutableStateFlow(RekapPeriod())
    val pemasukanPeriod = _pemasukanPeriod.asStateFlow()

    private val _pengeluaranPeriod = MutableStateFlow(RekapPeriod())
    val pengeluaranPeriod = _pengeluaranPeriod.asStateFlow()

    // One load per Rekap tab: a search per keystroke or a reopened screen cancels the load before it,
    // so a slow older search can never land after (and overwrite) the period now on screen
    private var pemasukanLoad: Job? = null
    private var transaksiLoad: Job? = null
    private var pengeluaranLoad: Job? = null

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
        pemasukanLoad?.cancel()
        pemasukanLoad = viewModelScope.launch {
            delay(300) // a pause in typing; the next keystroke cancels this load first
            _isLoading.value = true
            _errorPemasukan.value = null
            _pemasukanPeriod.value = RekapPeriod(startDate, endDate)

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
                    // An empty day comes back as a failure too: never keep the previous period or search on screen
                    _allPemasukan.value = emptyList()
                    when (exception) {
                        is JsonSyntaxException -> {
                            _errorPemasukan.value = "Tidak ada data pemasukan"
                        }
                        is IOException,
                        is UnknownHostException -> {
                            _errorPemasukan.value = "Periksa koneksi internet Anda"
                        }
                        else -> {
                            _errorPemasukan.value = exception.toUserMessage("Pencarian pemasukan belum berhasil")
                        }
                    }
                }


            _isLoading.value = false
        }
    }

    fun searchRekapPengeluaran(query: String, startDate: String, endDate: String) {
        pengeluaranLoad?.cancel()
        pengeluaranLoad = viewModelScope.launch {
            delay(300) // a pause in typing; the next keystroke cancels this load first
            _isLoading.value = true
            _errorPengeluaran.value = null
            _pengeluaranPeriod.value = RekapPeriod(startDate, endDate)

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
                    // An empty day comes back as a failure too: never keep the previous period or search on screen
                    _allPengeluaran.value = emptyList()
                    when (exception) {
                        is JsonSyntaxException -> {
                            _errorPengeluaran.value = "Tidak ada data pengeluaran"
                        }
                        is IOException,
                        is UnknownHostException -> {
                            _errorPengeluaran.value = "Periksa koneksi internet Anda"
                        }
                        else -> {
                            _errorPengeluaran.value = exception.toUserMessage("Pencarian pengeluaran belum berhasil")
                        }
                    }
                }

            _isLoading.value = false
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
                    _error.value = exception.toUserMessage("Transaksi belum tersimpan")
                }

            _isLoading.value = false
        }
    }


    fun fetchAllTransaksiAcrossPages(paymentMethod: String, status: String, startDate: String, endDate: String) {
        transaksiLoad?.cancel()
        transaksiLoad = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

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
                            _error.value = exception.toUserMessage("Daftar transaksi belum bisa dimuat")
                        }
                    }
                }

            _isLoading.value = false
            Log.d("TransaksiVM", "Fetch operation completed")
        }
    }

    fun fetchAllTransaksiNoFilter() {
        transaksiLoad?.cancel()
        transaksiLoad = viewModelScope.launch {
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
                            _error.value = exception.toUserMessage("Daftar transaksi belum bisa dimuat")
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
        pengeluaranLoad?.cancel()
        pengeluaranLoad = viewModelScope.launch {
            _isLoading.value = true
            _errorPengeluaran.value = null

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            val finalStartDate = if (startDate.isNotEmpty()) startDate else today
            val finalEndDate = if (endDate.isNotEmpty()) endDate else today

            _pengeluaranPeriod.value = RekapPeriod(finalStartDate, finalEndDate)

            transaksiRepository.getPengeluaranAcrossPage(finalStartDate, finalEndDate)
                .onSuccess { data ->
                    _allPengeluaran.value = data.transactions
                    Log.d("TransaksiVM", "Fetched ${data.transactions.size} total pengeluaran")
                }
                .onFailure { exception ->
                    // An empty day comes back as a failure too: never keep the previous period or search on screen
                    _allPengeluaran.value = emptyList()
                    when (exception) {
                        is JsonSyntaxException -> {
                            _errorPengeluaran.value = "Tidak ada data pengeluaran"
                        }
                        is IOException,
                        is UnknownHostException -> {
                            _errorPengeluaran.value = "Periksa koneksi internet Anda"
                        }
                        else -> {
                            _errorPengeluaran.value = exception.toUserMessage("Data pengeluaran belum bisa dimuat")
                        }
                    }
                }
            _isLoading.value = false
        }
    }




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

    fun payTransaction(paymentMethod: String, customerId: Int?) {
        if (_isLoading.value) return
        val transaction = _currentTransaction.value ?: return
        val request = try {
            transaction.toPaymentUpdate(paymentMethod, customerId)
        } catch (e: IllegalArgumentException) {
            _error.value = e.message
            return
        }
        _isLoading.value = true
        _error.value = null
        _isSuccess.value = false
        viewModelScope.launch {
            transaksiRepository.updateTransaksi(transaction.id, request)
                .onSuccess { response ->
                    _currentTransaction.value = response.data
                    _isSuccess.value = true
                }
                .onFailure { exception ->
                    _error.value = exception.toUserMessage("Pembayaran belum tersimpan")
                }
            _isLoading.value = false
        }
    }

    // Guards a second tap on the same update only: a list or detail load in progress must not
    // swallow the update (confirming "Sudah diantar" used to load the order first, so nothing was sent)
    private var statusUpdate: Job? = null

    fun updateStatus(
        id: Int,
        status: String,
        is_delivered: String,
        paymentMethod: String?
    ) {
        if (statusUpdate?.isActive == true) return
        _isLoading.value = true
        _error.value = null
        _isSuccess.value = false
        statusUpdate = viewModelScope.launch {
            transaksiRepository.updateStatus(id, TransactionUpdateStatus(status, is_delivered))
                .onSuccess {
                    _allTransactions.update { transactions ->
                        transactions.map { transaction ->
                            if (transaction.id == id) transaction.copy(status = status, is_delivered = is_delivered)
                            else transaction
                        }
                    }
                    _isSuccess.value = true
                }
                .onFailure { exception ->
                    _error.value = exception.toUserMessage("Status pesanan belum tersimpan")
                }
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
                    _error.value = exception.toUserMessage("Detail transaksi belum bisa dimuat")
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


    private fun calculateTotalPengeluaran(pengeluarans: List<TransactionPengeluaran>) {
        val total = pengeluarans.sumOf {
            it.total_amount.replace("Rp ", "").replace(".", "").toIntOrNull() ?: 0
        }
        _totalPengeluaran.value = "Rp ${total.toString().chunked(3).joinToString(".")}"
    }

    private val _allPemasukan = MutableStateFlow<List<TransactionPemasukan>>(emptyList())
    val allPemasukan: StateFlow<List<TransactionPemasukan>> = _allPemasukan



    fun fetchAllPemasukanAcrossPages(
        startDate: String,
        endDate: String
    ) {
        pemasukanLoad?.cancel()
        pemasukanLoad = viewModelScope.launch {
            _isLoading.value = true
            _errorPemasukan.value = null

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
            val finalStartDate = if (startDate.isNotEmpty()) startDate else today
            val finalEndDate = if (endDate.isNotEmpty()) endDate else today

            _pemasukanPeriod.value = RekapPeriod(finalStartDate, finalEndDate)


            transaksiRepository.getPemasukanAcrossPage(finalStartDate, finalEndDate)
                .onSuccess { data ->
                    _allPemasukan.value = data.transactions
                    Log.d("TransaksiVM", "Fetched ${data.transactions.size} total pemasukan")
                }
                .onFailure { exception ->
                    // An empty day comes back as a failure too: never keep the previous period or search on screen
                    _allPemasukan.value = emptyList()
                    when (exception) {
                        is JsonSyntaxException -> {
                            _errorPemasukan.value = "Tidak ada data pemasukan"
                        }
                        is IOException,
                        is UnknownHostException -> {
                            _errorPemasukan.value = "Periksa koneksi internet Anda"
                        }
                        else -> {
                            _errorPemasukan.value = exception.toUserMessage("Data pemasukan belum bisa dimuat")
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
                    _pengeluaranPeriod.value.let { fetchAllPengeluaranAcrossPages(it.start, it.end) } // Refresh pengeluaran data
                }
                .onFailure { exception ->
                    _error.value = exception.toUserMessage("Pengeluaran belum tersimpan")
                }

            _isLoading.value = false
        }
    }

    /** One value instead of four flags, so "loading and failed at once" cannot happen. */
    sealed interface PdfState {
        data object Idle : PdfState
        data object Loading : PdfState
        data class Ready(val file: File) : PdfState
        data class Failed(val message: String) : PdfState
    }

    private val _pdf = MutableStateFlow<PdfState>(PdfState.Idle)
    val pdf = _pdf.asStateFlow()

    fun downloadRekapPdf(startDate: String, endDate: String, type: String, fileName: String) =
        downloadPdf(fileName) { transaksiRepository.downloadRekapPdf(startDate, endDate, type) }

    fun downloadTransaksiPdf(status: String, paymentMethod: String, fileName: String) =
        downloadPdf(fileName) { transaksiRepository.downloadTransaksiPdf(status, paymentMethod) }

    private fun downloadPdf(fileName: String, fetch: suspend () -> Result<ByteArray>) {
        if (_pdf.value == PdfState.Loading) return // a second tap while the first PDF is on its way
        _pdf.value = PdfState.Loading
        viewModelScope.launch {
            _pdf.value = fetch().fold(
                onSuccess = { bytes ->
                    try {
                        PdfState.Ready(saveByteArrayToFile(bytes, fileName))
                    } catch (e: Exception) {
                        PdfState.Failed(e.toUserMessage("PDF belum bisa disimpan di HP"))
                    }
                },
                onFailure = { PdfState.Failed(it.toUserMessage("PDF belum bisa dibuat")) }
            )
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
        _pdf.value = PdfState.Idle
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
                    _error.value = exception.toUserMessage("Transaksi belum terhapus")
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
                    _error.value = exception.toUserMessage("Transaksi pada periode itu belum terhapus")
                }

            _isLoading.value = false
        }
    }
}