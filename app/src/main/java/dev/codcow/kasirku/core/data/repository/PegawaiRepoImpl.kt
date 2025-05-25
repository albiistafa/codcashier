package dev.codcow.kasirku.core.data.repository

import android.util.Log
import dev.codcow.kasirku.core.data.model.pegawai.CreatePegawaiRequest
import dev.codcow.kasirku.core.data.model.pegawai.Data
import dev.codcow.kasirku.core.data.model.pegawai.PegawaiResponse
import dev.codcow.kasirku.core.data.model.pegawai.PegawaiResponses
import dev.codcow.kasirku.core.data.remote.PegawaiApiService
import dev.codcow.kasirku.core.domain.repository.PegawaiRepository
import dev.codcow.kasirku.core.domain.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PegawaiRepoImpl @Inject constructor(
    private val pegawaiApiService: PegawaiApiService,
    private val tokenManager: TokenRepository
) : PegawaiRepository {

    private suspend fun getAuthToken(): String {
        return tokenManager.getToken() ?: throw IllegalStateException("No authentication token available")
    }

    override suspend fun getAllPegawai(): Result<List<Data>> {
        return safeApiCall {
            val token = getAuthToken()
            val response = pegawaiApiService.getAllPegawai(token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getPegawaiById(id: Int): Result<PegawaiResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = pegawaiApiService.getPegawaiById(id, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun createPegawai(request: CreatePegawaiRequest): Result<PegawaiResponses> {
        return safeApiCall {
            val token = getAuthToken()
            val response = pegawaiApiService.createPegawai(request, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun updatePegawai(id: Int, pegawai: Data): Result<PegawaiResponses> {
        return safeApiCall {
            val token = getAuthToken()
            val response = pegawaiApiService.updatePegawai(id, pegawai, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun deletePegawai(id: Int): Result<Unit> {
        return safeApiCall {
            val token = getAuthToken()
            val response = pegawaiApiService.deletePegawai(id, token)
            if (response.isSuccessful) {
                Unit
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun searchPegawai(query: String): Result<List<Data>> {
        return safeApiCall {
            val token = getAuthToken()
            val response = pegawaiApiService.searchPegawai(query, token)
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
                        Log.e("PegawaiRepo", "Network Error", throwable)
                        Result.failure(NetworkException("Network error occurred", throwable))
                    }
                    is HttpException -> {
                        val errorBody = throwable.response()?.errorBody()?.string()
                        Log.e("PegawaiRepo", "HTTP Error ${throwable.code()}: $errorBody", throwable)
                        Result.failure(ApiException("API error: ${throwable.code()}", throwable))
                    }
                    is IllegalStateException -> {
                        Log.e("PegawaiRepo", "Authentication Error", throwable)
                        Result.failure(AuthenticationException("No authentication token", throwable))
                    }
                    else -> {
                        Log.e("PegawaiRepo", "Unknown Error", throwable)
                        Result.failure(throwable)
                    }
                }
            }
        }
    }

    // Custom exception classes for more specific error handling
    class NetworkException(message: String, cause: Throwable) : Exception(message, cause)
    class ApiException(message: String, cause: Throwable) : Exception(message, cause)
    class AuthenticationException(message: String, cause: Throwable) : Exception(message, cause)
}