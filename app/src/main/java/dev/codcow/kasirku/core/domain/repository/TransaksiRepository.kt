package dev.codcow.kasirku.core.domain.repository

import dev.codcow.kasirku.core.data.model.SearchRekap.DataSearch
import dev.codcow.kasirku.core.data.model.SearchRekap.SearchRekapResponse
import dev.codcow.kasirku.core.data.model.addToCart.TransactionRequest
import dev.codcow.kasirku.core.data.model.pemasukan.DataPemasukan
import dev.codcow.kasirku.core.data.model.pemasukan.PemasukanResponse
import dev.codcow.kasirku.core.data.model.pengeluaran.DataPengeluaran
import dev.codcow.kasirku.core.data.model.pengeluaran.PengeluaranRequest
import dev.codcow.kasirku.core.data.model.pengeluaran.PengeluaranResponse
import dev.codcow.kasirku.core.data.model.transaksi.CreateTransactionResponse
import dev.codcow.kasirku.core.data.model.transaksi.DataSearchTransaksi
import dev.codcow.kasirku.core.data.model.transaksi.TransaksiResponses
import dev.codcow.kasirku.core.data.model.transaksi.DataTransaksi
import dev.codcow.kasirku.core.data.model.transaksi.SearchTransaksiResponse
import dev.codcow.kasirku.core.data.model.transaksi.TransactionGetByIdResponse
import dev.codcow.kasirku.core.data.model.transaksi.TransactionUpdateRequest
import dev.codcow.kasirku.core.data.model.transaksi.TransactionUpdateStatus
import dev.codcow.kasirku.core.data.model.transaksi.TransactionX
import okhttp3.ResponseBody

interface TransaksiRepository {
    // Basic CRUD Operations
    suspend fun getAllTransaksi(): Result<DataTransaksi>
    suspend fun getTransaksiById(id: Int): Result<TransactionGetByIdResponse>
    suspend fun createTransaksi(transaction: TransactionRequest): Result<CreateTransactionResponse>
    suspend fun updateTransaksi(id: Int, updateData: TransactionUpdateRequest): Result<TransactionGetByIdResponse>
    suspend fun updateStatus(id: Int, updateStatus: TransactionUpdateStatus): Result<TransactionGetByIdResponse>
    suspend fun deleteTransaksi(id: Int): Result<Unit>

    suspend fun getPengeluaranPaginated(startDate: String, endDate: String, page: Int, limit: Int): Result<PengeluaranResponse>
    suspend fun getPengeluaranAcrossPage(startDate: String, endDate: String): Result<DataPengeluaran>

    suspend fun getPemasukanPaginated(startDate: String, endDate: String, page: Int, limit: Int ): Result<PemasukanResponse>
    suspend fun getPemasukanAcrossPage(startDate: String, endDate: String): Result<DataPemasukan>

    // Additional Operations
    suspend fun getTransaksiByStatus(status: String): Result<DataTransaksi>
    suspend fun getTransaksiByDate(startDate: String, endDate: String): Result<DataTransaksi>
    suspend fun getTransaksiByPaymentMethod(paymentMethod: String): Result<DataTransaksi>

    suspend fun getTransactionsPaginated(paymentMethod: String, status: String, startDate: String, endDate: String, page: Int, limit: Int): Result<TransaksiResponses>
    suspend fun getTransactionsNoFilter(page: Int, limit: Int): Result<TransaksiResponses>
    suspend fun getAllTransaksiAcrossPages(paymentMethod: String, status: String, startDate: String, endDate: String): Result<DataTransaksi>
    suspend fun getAllTransaksiAcrossPagesNoFilter(): Result<DataTransaksi>

    suspend fun searchTransactionPaginated(page: Int, limit: Int): Result<SearchTransaksiResponse>
    suspend fun searchTransactions(query: String, paymentMethod: String, status: String): Result<List<TransactionX>>
    suspend fun searchTransactionAcrossPages(query: String,paymentMethod: String, status: String): Result<DataSearchTransaksi>

    suspend fun searchRekapPaginated(page: Int, limit: Int): Result<SearchRekapResponse>
    suspend fun searchRekap(query: String, startDate: String, endDate: String, type: String, limit: Int): Result<List<dev.codcow.kasirku.core.data.model.SearchRekap.Transaction>>
    suspend fun searchRekapAcrossPages(query: String, startDate: String, endDate: String, type: String): Result<DataSearch>

    suspend fun createPengeluaran(request: PengeluaranRequest): Result<PengeluaranResponse>

    suspend fun downloadRekapPdf(startDate: String, endDate: String, type: String): Result<ByteArray>
    suspend fun downloadTransaksiPdf(paymentMethod: String, status: String): Result<ByteArray>
    suspend fun deletePeriode(startDate: String, endDate: String): Result<ResponseBody>

    data class DeletePeriodeRequest(
        val startDate: String,
        val endDate: String
    )
}