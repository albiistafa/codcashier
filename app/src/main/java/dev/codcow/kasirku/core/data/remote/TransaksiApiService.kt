package dev.codcow.kasirku.core.data.remote

import dev.codcow.kasirku.core.data.model.Rekap.RekapResponses
import dev.codcow.kasirku.core.data.model.SearchRekap.SearchRekapListResponse
import dev.codcow.kasirku.core.data.model.SearchRekap.SearchRekapResponse
import dev.codcow.kasirku.core.data.model.addToCart.TransactionRequest
import dev.codcow.kasirku.core.data.model.pemasukan.PemasukanResponse
import dev.codcow.kasirku.core.data.model.pengeluaran.PengeluaranRequest
//import dev.codcow.kasirku.core.data.model.pengeluaran.PengeluaranRequest
import dev.codcow.kasirku.core.data.model.pengeluaran.PengeluaranResponse
import dev.codcow.kasirku.core.data.model.transaksi.CreateTransactionResponse
import dev.codcow.kasirku.core.data.model.transaksi.DataTransaksi
import dev.codcow.kasirku.core.data.model.transaksi.SearchTransaksiResponse
import dev.codcow.kasirku.core.data.model.transaksi.Transaction
import dev.codcow.kasirku.core.data.model.transaksi.TransactionGetByIdResponse
import dev.codcow.kasirku.core.data.model.transaksi.TransactionUpdateRequest
import dev.codcow.kasirku.core.data.model.transaksi.TransactionUpdateStatus
import dev.codcow.kasirku.core.data.model.transaksi.TransaksiResponses
import dev.codcow.kasirku.core.domain.repository.TransaksiRepository
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface TransaksiApiService {

    @GET("api/transactions")
    suspend fun getAllTransaksi(
        @Header("Authorization") token: String
    ): Response<TransaksiResponses>

    @GET("api/rekap/pengeluaran/")
    suspend fun getPengeluaranPaginated(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String
    ): Response<PengeluaranResponse>

    @GET("api/rekap/pemasukan")
    suspend fun getPemasukanPaginated(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String
    ): Response<PemasukanResponse>

    @GET("api/transactions/{id}")
    suspend fun getTransaksiById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<TransactionGetByIdResponse>

    @POST("api/transactions/create")
    suspend fun createTransaksi(
        @Body transaction: TransactionRequest,
        @Header("Authorization") token: String
    ): Response<CreateTransactionResponse>

    @POST("api/transactions/pengeluaran/create")
    suspend fun createPengeluaran(
        @Body transaction: PengeluaranRequest,
        @Header("Authorization") token: String
    ): Response<PengeluaranResponse>

    @PUT("api/transactions/update/{id}")
    suspend fun updateTransaksi(
        @Path("id") id: Int,
        @Body updateData: TransactionUpdateRequest,
        @Header("Authorization") token: String
    ): Response<TransactionGetByIdResponse>

    @PATCH("api/transactions/update-status/{id}")
    suspend fun updateStatus(
        @Path("id") id: Int,
        @Body updateStatus: TransactionUpdateStatus,
        @Header("Authorization") token: String
    ): Response<TransactionGetByIdResponse>


    @DELETE("api/transactions/delete/{id}")
    suspend fun deleteTransaksi(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<TransaksiResponses>

    @GET("api/transactions/status/{status}")
    suspend fun getTransaksiByStatus(
        @Path("status") status: String,
        @Header("Authorization") token: String
    ): Response<TransaksiResponses>

    // Tambahan endpoint untuk mendapatkan transaksi berdasarkan tanggal
    @GET("api/transactions/date")
    suspend fun getTransaksiByDate(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Header("Authorization") token: String
    ): Response<TransaksiResponses>

    // Tambahan endpoint untuk mendapatkan transaksi berdasarkan metode pembayaran
    @GET("api/transactions/payment/{method}")
    suspend fun getTransaksiByPaymentMethod(
        @Path("method") paymentMethod: String,
        @Header("Authorization") token: String
    ): Response<TransaksiResponses>

    @GET("api/transactions")
    suspend fun getTransactionsPaginated(
        @Query("paymentMethod") paymentMethod: String,
        @Query("status") status: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String
    ): Response<TransaksiResponses>

    @GET("api/transactions")
    suspend fun getTransactionsNoFilter(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String
    ): Response<TransaksiResponses>

    @GET("api/transactions/search/{query}")
    suspend fun searchTransactionsPaginated(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String
    ): Response<SearchTransaksiResponse>

    @GET("api/transactions/search/{query}")
    suspend fun searchTransactions(
        @Path("query") query: String,
        @Query("status") status: String,
        @Query("paymentMethod") paymentMethod: String,
        @Header("Authorization") token: String
    ): Response<SearchTransaksiResponse>

    @GET("api/rekap/search/{query}")
    suspend fun searchRekapPaginated(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String
    ): Response<SearchRekapResponse>

    @GET("api/rekap/search/{query}")
    suspend fun searchRekap(
        @Path("query") query: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("type") type: String,
        @Query("limit") limit: Int,
        @Header("Authorization") token: String
    ): Response<SearchRekapResponse>

    @GET("api/rekap/pdf")
    suspend fun pdfRekap(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("type") type: String,
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    @GET("api/transactions/pdf")
    suspend fun pdfTransaksi(
        @Query("status") status: String,
        @Query("paymentMethod") paymentMethod: String,
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    @POST("api/transactions/delete/periode")
    suspend fun deletePeriode(
        @Body request: TransaksiRepository.DeletePeriodeRequest,
        @Header("Authorization") token: String
    ): Response<ResponseBody>

}