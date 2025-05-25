package dev.codcow.kasirku.core.data.repository

import android.util.Log
import dev.codcow.kasirku.core.data.model.SearchRekap.DataSearch
import dev.codcow.kasirku.core.data.model.SearchRekap.SearchRekapResponse
import dev.codcow.kasirku.core.data.model.addToCart.TransactionRequest
import dev.codcow.kasirku.core.data.model.pemasukan.DataPemasukan
import dev.codcow.kasirku.core.data.model.pemasukan.PemasukanResponse
import dev.codcow.kasirku.core.data.model.pemasukan.TransactionPemasukan
import dev.codcow.kasirku.core.data.model.pengeluaran.DataPengeluaran
import dev.codcow.kasirku.core.data.model.pengeluaran.PengeluaranRequest
import dev.codcow.kasirku.core.data.model.pengeluaran.PengeluaranResponse
import dev.codcow.kasirku.core.data.model.pengeluaran.TransactionPengeluaran
import dev.codcow.kasirku.core.data.model.transaksi.CreateTransactionResponse
import dev.codcow.kasirku.core.data.model.transaksi.DataSearchTransaksi
import dev.codcow.kasirku.core.data.model.transaksi.Transaction
import dev.codcow.kasirku.core.data.model.transaksi.TransaksiResponses
import dev.codcow.kasirku.core.data.model.transaksi.DataTransaksi
import dev.codcow.kasirku.core.data.model.transaksi.Pagination
import dev.codcow.kasirku.core.data.model.transaksi.SearchTransaksiResponse
import dev.codcow.kasirku.core.data.model.transaksi.TransactionGetByIdResponse
import dev.codcow.kasirku.core.data.model.transaksi.TransactionUpdateRequest
import dev.codcow.kasirku.core.data.model.transaksi.TransactionUpdateStatus
import dev.codcow.kasirku.core.data.model.transaksi.TransactionX
import dev.codcow.kasirku.core.data.remote.TransaksiApiService
import dev.codcow.kasirku.core.domain.repository.TokenRepository
import dev.codcow.kasirku.core.domain.repository.TransaksiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransaksiRepoImpl @Inject constructor(
    private val transaksiApiService: TransaksiApiService,
    private val tokenManager: TokenRepository
) : TransaksiRepository {

    private suspend fun getAuthToken(): String {
        return tokenManager.getToken() ?: throw IllegalStateException("No authentication token available")
    }

    override suspend fun getAllTransaksi(): Result<DataTransaksi> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.getAllTransaksi(token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getTransaksiById(id: Int): Result<TransactionGetByIdResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.getTransaksiById(id, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }


    override suspend fun createTransaksi(transaction: TransactionRequest): Result<CreateTransactionResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.createTransaksi(transaction, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun updateTransaksi(id: Int, updateData: TransactionUpdateRequest): Result<TransactionGetByIdResponse> {
        return safeApiCall {
            val token = getAuthToken()
            Log.d("TransaksiRepo", "Updating transaction $id with data: $updateData")
            val response = transaksiApiService.updateTransaksi(id, updateData, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TransaksiRepo", "Failed to update transaction: $errorBody")
                throw HttpException(response)
            }
        }
    }

    override suspend fun updateStatus(id: Int, updateStatus: TransactionUpdateStatus): Result<TransactionGetByIdResponse> {
        return safeApiCall {
            val token = getAuthToken()
            Log.d("TransaksiRepo", "Updating transaction $id with data: $updateStatus")
            val response = transaksiApiService.updateStatus(id, updateStatus, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TransaksiRepo", "Failed to update transaction: $errorBody")
                throw HttpException(response)
            }
        }
    }

    override suspend fun deleteTransaksi(id: Int): Result<Unit> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.deleteTransaksi(id, token)
            if (response.isSuccessful) {
                Unit
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getTransaksiByStatus(status: String): Result<DataTransaksi> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.getTransaksiByStatus(status, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getTransaksiByDate(startDate: String, endDate: String): Result<DataTransaksi> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.getTransaksiByDate(startDate, endDate, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getTransaksiByPaymentMethod(paymentMethod: String): Result<DataTransaksi> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.getTransaksiByPaymentMethod(paymentMethod, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(apiCall())
            } catch (throwable: Throwable) {
                when (throwable) {
                    is IOException -> {
                        Log.e("TransaksiRepo", "Network Error", throwable)
                        Result.failure(NetworkException("Network error occurred", throwable))
                    }
                    is HttpException -> {
                        val errorBody = throwable.response()?.errorBody()?.string()
                        Log.e("TransaksiRepo", "HTTP Error ${throwable.code()}: $errorBody", throwable)
                        Result.failure(ApiException("API error: ${throwable.code()}", throwable))
                    }
                    is IllegalStateException -> {
                        Log.e("TransaksiRepo", "Authentication Error", throwable)
                        Result.failure(AuthenticationException("No authentication token", throwable))
                    }
                    else -> {
                        Log.e("TransaksiRepo", "Unknown Error", throwable)
                        Result.failure(throwable)
                    }
                }
            }
        }
    }

    override suspend fun getTransactionsPaginated(
        paymentMethod: String,
        status: String,
        startDate: String,
        endDate: String,
        page: Int,
        limit: Int): Result<TransaksiResponses> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.getTransactionsPaginated(paymentMethod, status, startDate, endDate, page, limit, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getTransactionsNoFilter(
        page: Int,
        limit: Int): Result<TransaksiResponses> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.getTransactionsNoFilter(page, limit, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }


    override suspend fun getPengeluaranPaginated(
        startDate: String,
        endDate: String,
        page: Int,
        limit: Int
    ): Result<PengeluaranResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.getPengeluaranPaginated(startDate, endDate, page, limit, token)
            if (response.isSuccessful && response.body() != null){
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getPengeluaranAcrossPage(
        startDate: String,
        endDate: String
    ): Result<DataPengeluaran> {
        return safeApiCall {
            val token = getAuthToken()
            val allPengeluaran = mutableListOf<TransactionPengeluaran>()

            var currentPage = 1
            val pageSize = 50
            var hasMoreData = true

            while (hasMoreData){
                Log.d("Transaksi Repo", "Fetching page $currentPage")
                val response = transaksiApiService.getPengeluaranPaginated(startDate, endDate, currentPage, pageSize, token)
                if (!response.isSuccessful || response.body() == null){
                    throw HttpException(response)
                }

                val pageRespone = response.body()!!
                val pageData = pageRespone.data

                Log.d("Transaksi Repo", "Retrieved ${pageData.transaction_pengeluaran.size} pengeluaran form page $currentPage")

                allPengeluaran.addAll(pageData.transaction_pengeluaran)

                hasMoreData = pageData.transaction_pengeluaran.size >= pageSize
                currentPage++

                if (currentPage > 10) {
                    Log.w("TransaksiRepo", "Stopped after 10 pages as a safety measure")
                    break
                }
            }

            val combinedPagination = Pagination(
                limit = pageSize,
                page = 1,
                total = allPengeluaran.size.toString(),
                totalPages = 1
            )

            DataPengeluaran(
                transactions = allPengeluaran,
                pagination = combinedPagination
            )
        }
    }

    override suspend fun getPemasukanPaginated(
        startDate: String,
        endDate: String,
        page: Int,
        limit: Int,
    ): Result<PemasukanResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.getPemasukanPaginated(startDate, endDate, page, limit,  token)
            if (response.isSuccessful && response.body() != null){
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getPemasukanAcrossPage(
        startDate: String,
        endDate: String
    ): Result<DataPemasukan> {
        return safeApiCall {
            val token = getAuthToken()
            val allPemasukan = mutableListOf<TransactionPemasukan>()

            var currentPage = 1
            val pageSize = 50
            var hasMoreData = true

            while (hasMoreData){
                Log.d("Transaksi Repo", "Fetching pemasukan page $currentPage with date range: $startDate to $endDate")
                val response = transaksiApiService.getPemasukanPaginated(
                    startDate,
                    endDate,
                    currentPage,
                    pageSize,
                    token
                )

                if (!response.isSuccessful || response.body() == null){
                    throw HttpException(response)
                }

                val pageResponse = response.body()!!
                val pageData = pageResponse.data

                Log.d("Transaksi Repo", "Retrieved ${pageData.transaction_pemasukan.size} pemasukan from page $currentPage")

                allPemasukan.addAll(pageData.transaction_pemasukan)

                hasMoreData = pageData.transaction_pemasukan.size >= pageSize
                currentPage++

                if (currentPage > 10) {
                    Log.w("TransaksiRepo", "Stopped after 10 pages as a safety measure")
                    break
                }
            }

            val combinedPagination = Pagination(
                limit = pageSize,
                page = 1,
                total = allPemasukan.size.toString(),
                totalPages = 1
            )

            DataPemasukan(
                transactions = allPemasukan,
                pagination = combinedPagination
            )
        }
    }

    override suspend fun getAllTransaksiAcrossPages(
        paymentMethod: String,
        status: String,
        startDate: String,
        endDate: String
    ): Result<DataTransaksi> {
        return safeApiCall {
            val token = getAuthToken()
            val allTransactions = mutableListOf<Transaction>()

            // Start with first page
            var currentPage = 1
            val pageSize = 50  // Increase page size to get more data per request
            var hasMoreData = true

            while (hasMoreData) {
                // Fetch current page
                Log.d("TransaksiRepo", "Fetching page $currentPage")
                val response = transaksiApiService.getTransactionsPaginated(paymentMethod, status, startDate, endDate, currentPage, pageSize, token)
                if (!response.isSuccessful || response.body() == null) {
                    throw HttpException(response)
                }

                // Extract data from response
                val pageResponse = response.body()!!
                val pageData = pageResponse.data

                // Log how many transactions were retrieved
                Log.d("TransaksiRepo", "Retrieved ${pageData.transactions.size} transactions from page $currentPage")

                // Add transactions from this page to our collection
                allTransactions.addAll(pageData.transactions)

                // Determine if there are more pages to fetch
                hasMoreData = pageData.transactions.size >= pageSize

                // Move to next page
                currentPage++

                // Safety check to prevent infinite loops
                if (currentPage > 10) {  // Increased from 100 to make debugging easier
                    Log.w("TransaksiRepo", "Stopped after 10 pages as a safety measure")
                    break
                }
            }

            Log.d("TransaksiRepo", "Total transactions fetched: ${allTransactions.size}")

            // Create a simplified Pagination object
            val combinedPagination = Pagination(
                limit = pageSize,
                page = 1,
                total = allTransactions.size.toString(),
                totalPages = 1
            )

            // Create a combined DataTransaksi object with all transactions
            DataTransaksi(
                transactions = allTransactions,
                pagination = combinedPagination
            )
        }
    }

    override suspend fun getAllTransaksiAcrossPagesNoFilter(
    ): Result<DataTransaksi> {
        return safeApiCall {
            val token = getAuthToken()
            val allTransactions = mutableListOf<Transaction>()

            // Start with first page
            var currentPage = 1
            val pageSize = 50  // Increase page size to get more data per request
            var hasMoreData = true

            while (hasMoreData) {
                // Fetch current page
                Log.d("TransaksiRepo", "Fetching page $currentPage")
                val response = transaksiApiService.getTransactionsNoFilter(currentPage, pageSize, token)
                if (!response.isSuccessful || response.body() == null) {
                    throw HttpException(response)
                }

                // Extract data from response
                val pageResponse = response.body()!!
                val pageData = pageResponse.data

                // Log how many transactions were retrieved
                Log.d("TransaksiRepo", "Retrieved ${pageData.transactions.size} transactions from page $currentPage")

                // Add transactions from this page to our collection
                allTransactions.addAll(pageData.transactions)

                // Determine if there are more pages to fetch
                hasMoreData = pageData.transactions.size >= pageSize

                // Move to next page
                currentPage++

                // Safety check to prevent infinite loops
                if (currentPage > 10) {  // Increased from 100 to make debugging easier
                    Log.w("TransaksiRepo", "Stopped after 10 pages as a safety measure")
                    break
                }
            }

            Log.d("TransaksiRepo", "Total transactions fetched: ${allTransactions.size}")

            // Create a simplified Pagination object
            val combinedPagination = Pagination(
                limit = pageSize,
                page = 1,
                total = allTransactions.size.toString(),
                totalPages = 1
            )

            // Create a combined DataTransaksi object with all transactions
            DataTransaksi(
                transactions = allTransactions,
                pagination = combinedPagination
            )
        }
    }

    override suspend fun searchTransactions(
        query: String,
        paymentMethod: String,
        status: String
    ): Result<List<TransactionX>> {
        return safeApiCall {
            val token = getAuthToken()
            val response =
                transaksiApiService.searchTransactions(query, paymentMethod, status, token)
            if (!response.isSuccessful) {
                throw HttpException(response)
            }
            response.body()?.let { apiResponse ->
                apiResponse.data?.transactions?.let { transactions ->
                    transactions
                } ?: throw Exception("Transactions data is null")
            } ?: throw Exception("Empty response body")
        }
    }

    override suspend fun searchTransactionPaginated(
        page: Int,
        limit: Int
    ): Result<SearchTransaksiResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.searchTransactionsPaginated(page, limit, token)
            if (response.isSuccessful && response.body() != null){
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun searchRekapPaginated(
        page: Int,
        limit: Int
    ): Result<SearchRekapResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.searchRekapPaginated(page, limit, token)
            if (response.isSuccessful && response.body() != null){
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun searchTransactionAcrossPages(
        query: String,
        paymentMethod: String,
        status: String
    ): Result<DataSearchTransaksi> {
        return safeApiCall {
            val token = getAuthToken()
            val allSearch = mutableListOf<TransactionX>()

            var currentPage = 1
            val pageSize = 50
            var hasMoreData = true

            while (hasMoreData) {
                Log.d("TransaksiRepo", "Fetching page $currentPage for query: $query")
                val response = transaksiApiService.searchTransactions(query, paymentMethod, status, token)
                if (!response.isSuccessful || response.body() == null) {
                    throw HttpException(response)
                }

                val pageResponse = response.body()!!
                val pageData = pageResponse.data

                Log.d("TransaksiRepo", "Retrieved ${pageData.transactions.size} transactions from page $currentPage")

                allSearch.addAll(pageData.transactions)

                // Determine if there are more pages to fetch
                hasMoreData = pageData.transactions.size >= pageSize && currentPage < pageResponse.data.pagination.totalPages.toInt()

                // Move to next page
                currentPage++

                // Safety check to prevent infinite loops
                if (currentPage > 10) {
                    Log.w("TransaksiRepo", "Stopped after 10 pages as a safety measure")
                    break
                }
            }

            Log.d("TransaksiRepo", "Total search results fetched: ${allSearch.size}")

            val combinedPagination = dev.codcow.kasirku.core.data.model.transaksi.Pagination(
                limit = pageSize,
                page = 1,
                total = allSearch.size.toString(),
                totalPages = 1
            )

            // Create a combined DataSearch object with all transactions
            DataSearchTransaksi(
                transactions = allSearch,
                pagination = combinedPagination
            )
        }
    }

    override suspend fun searchRekap(
        query: String,
        startDate: String,
        endDate: String,
        type: String,
        limit: Int
    ): Result<List<dev.codcow.kasirku.core.data.model.SearchRekap.Transaction>> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.searchRekap(query, startDate, endDate, type, limit, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data.transactions
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun searchRekapAcrossPages(
        query: String,
        startDate: String,
        endDate: String,
        type: String
    ): Result<DataSearch> {
        return safeApiCall {
            val token = getAuthToken()
            val allSearch = mutableListOf<dev.codcow.kasirku.core.data.model.SearchRekap.Transaction>()

            var currentPage = 1
            val pageSize = 100
            var hasMoreData = true

            while (hasMoreData) {
                Log.d("TransaksiRepo", "Fetching page $currentPage for query: $query")
                val response = transaksiApiService.searchRekap(query, startDate, endDate, type, pageSize, token)
                if (!response.isSuccessful || response.body() == null) {
                    throw HttpException(response)
                }

                val pageResponse = response.body()!!
                val pageData = pageResponse.data

                Log.d("TransaksiRepo", "Retrieved ${pageData.transactions.size} transactions from page $currentPage")

                allSearch.addAll(pageData.transactions)

                // Determine if there are more pages to fetch
                hasMoreData = pageData.transactions.size >= pageSize && currentPage < pageResponse.data.pagination.totalPages.toInt()

                // Move to next page
                currentPage++

                // Safety check to prevent infinite loops
                if (currentPage > 10) {
                    Log.w("TransaksiRepo", "Stopped after 10 pages as a safety measure")
                    break
                }
            }

            Log.d("TransaksiRepo", "Total search results fetched: ${allSearch.size}")

            // Create a simplified Pagination object
            val combinedPagination = dev.codcow.kasirku.core.data.model.SearchRekap.Pagination(
                limit = pageSize,
                page = 1,
                total = allSearch.size.toString(),
                totalPages = 1
            )

            // Create a combined DataSearch object with all transactions
            DataSearch(
                transactions = allSearch,
                pagination = combinedPagination
            )
        }
    }

    override suspend fun createPengeluaran(request: PengeluaranRequest): Result<PengeluaranResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.createPengeluaran(request, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun downloadRekapPdf(
        startDate: String,
        endDate: String,
        type: String
    ): Result<ByteArray> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.pdfRekap(startDate, endDate, type, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.bytes()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun downloadTransaksiPdf(
        status: String,
        paymentMethod: String
    ): Result<ByteArray> {
        return safeApiCall {
            val token = getAuthToken()
            val response = transaksiApiService.pdfTransaksi(status, paymentMethod, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.bytes()
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun deletePeriode(startDate: String, endDate: String): Result<ResponseBody> {
        return safeApiCall {
            val token = getAuthToken()
            val request = TransaksiRepository.DeletePeriodeRequest(startDate, endDate)
            val response = transaksiApiService.deletePeriode(request, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    class NetworkException(message: String, cause: Throwable) : Exception(message, cause)
    class ApiException(message: String, cause: Throwable) : Exception(message, cause)
    class AuthenticationException(message: String, cause: Throwable) : Exception(message, cause)
}