package dev.codcow.kasirku.core.data.repository

import android.util.Log
import dev.codcow.kasirku.core.data.model.deposit.CreateDataDeposit
import dev.codcow.kasirku.core.data.model.deposit.DataDeposit
import dev.codcow.kasirku.core.data.model.deposit.DepositResponse
import dev.codcow.kasirku.core.data.model.deposit.DepositResponses
import dev.codcow.kasirku.core.data.model.deposit.TopUpData
import dev.codcow.kasirku.core.data.model.deposit.UpdateDataDeposit
import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import dev.codcow.kasirku.core.data.remote.DepositApiService
import dev.codcow.kasirku.core.domain.repository.DepositRepository
import dev.codcow.kasirku.core.domain.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DepositRepoImpl @Inject constructor(
    private val depositApiService: DepositApiService,
    private val tokenManager: TokenRepository
) : DepositRepository {

    private suspend fun getAuthToken(): String {
        return tokenManager.getToken() ?: throw IllegalStateException("No authentication token available")
    }

    override suspend fun getAllDeposit(): Result<DepositResponses> {
        return safeApiCall {
            val token = "Bearer ${getAuthToken()}"
            val response = depositApiService.getAllDeposit(token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun createDeposit(request: CreateDataDeposit): Result<DepositResponse> {
        return safeApiCall {
            val token = "Bearer ${getAuthToken()}"
            val response = depositApiService.createDeposit(request, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getCustomerById(customerId: Int): Result<DepositResponse> {
        return safeApiCall {
            val token = "Bearer ${getAuthToken()}"
            val response = depositApiService.getCustomerById(customerId, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun updateCustomerById(
        id: Int,
        updateDeposit: UpdateDataDeposit
    ): Result<DepositResponse> {
        return safeApiCall {
            val token = "Bearer ${getAuthToken()}"
            val response = depositApiService.updateCustomerById(id, updateDeposit, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun topUpByCustomerId(
        id: Int,
        topUpData: TopUpData
    ): Result<DepositResponses> {
        return safeApiCall {
            val token = "Bearer ${getAuthToken()}"
            val response = depositApiService.topUpByCustomerId(id, topUpData, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun deleteDeposit(id: Int): Result<Unit> {
        return safeApiCall {
            val token = "Bearer ${getAuthToken()}"
            val response = depositApiService.deleteDeposit(id, token)
            if (response.isSuccessful) {
                Unit
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
                        Log.e("DepositRepo", "Network Error", throwable)
                        Result.failure(NetworkException("Terjadi kesalahan jaringan", throwable))
                    }
                    is HttpException -> {
                        val errorBody = throwable.response()?.errorBody()?.string()
                        Log.e("DepositRepo", "HTTP Error ${throwable.code()}: $errorBody", throwable)
                        Result.failure(ApiException("Error API: ${throwable.code()} - $errorBody", throwable))
                    }
                    is IllegalStateException -> {
                        Log.e("DepositRepo", "Authentication Error", throwable)
                        Result.failure(AuthenticationException("Token tidak valid", throwable))
                    }
                    else -> {
                        Log.e("DepositRepo", "Unknown Error", throwable)
                        Result.failure(Exception("Terjadi kesalahan: ${throwable.message}"))
                    }
                }
            }
        }


    }

    override suspend fun searchDeposit(query: String): Result<List<DataDeposit>> {
        return safeApiCall {
            val token = getAuthToken()
            val response = depositApiService.searchDeposit(query, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    // Custom exception classes
    class NetworkException(message: String, cause: Throwable) : Exception(message, cause)
    class ApiException(message: String, cause: Throwable) : Exception(message, cause)
    class AuthenticationException(message: String, cause: Throwable) : Exception(message, cause)
}