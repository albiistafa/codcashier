package dev.codcow.kasirku.core.data.repository

import android.util.Log
import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import dev.codcow.kasirku.core.data.model.subkategori.CreateSubkategoriRequest
import dev.codcow.kasirku.core.data.model.subkategori.DataSubkategori
import dev.codcow.kasirku.core.data.model.subkategori.SubkategoriResponses
import dev.codcow.kasirku.core.data.model.subkategori.SubkategoriResponse
import dev.codcow.kasirku.core.data.remote.SubkategoriApiService
import dev.codcow.kasirku.core.domain.repository.SubKategoriRepository
import dev.codcow.kasirku.core.domain.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubKategoriIRepoImpl @Inject constructor(
    private val subkategoriApiService: SubkategoriApiService,
    private val tokenManager: TokenRepository
) : SubKategoriRepository {

    private suspend fun getAuthToken(): String {
        return tokenManager.getToken() ?: throw IllegalStateException("No authentication token available")
    }

    override suspend fun getAllSubKategori(): Result<List<DataSubkategori>> {
        return safeApiCall {
            val token = getAuthToken()
            val response = subkategoriApiService.getAllSubKategori(token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getSubKategoriById(id: Int): Result<SubkategoriResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = subkategoriApiService.getSubKategoriById(id, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun createSubKategori(subkategori: CreateSubkategoriRequest): Result<SubkategoriResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = subkategoriApiService.createSubKategori(subkategori, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun updateSubKategori(id: Int, subkategori: DataSubkategori): Result<SubkategoriResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = subkategoriApiService.updateSubKategori(id, subkategori, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun deleteSubKategori(id: Int): Result<Unit> {
        return safeApiCall {
            val token = getAuthToken()
            val response = subkategoriApiService.deleteSubKategori(id, token)
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
                        Log.e("SubKategoriRepo", "Network Error", throwable)
                        Result.failure(NetworkException("Network error occurred", throwable))
                    }
                    is HttpException -> {
                        val errorBody = throwable.response()?.errorBody()?.string()
                        Log.e("SubKategoriRepo", "HTTP Error ${throwable.code()}: $errorBody", throwable)
                        Result.failure(ApiException("API error: ${throwable.code()}", throwable))
                    }
                    is IllegalStateException -> {
                        Log.e("SubKategoriRepo", "Authentication Error", throwable)
                        Result.failure(AuthenticationException("No authentication token", throwable))
                    }
                    else -> {
                        Log.e("SubKategoriRepo", "Unknown Error", throwable)
                        Result.failure(throwable)
                    }
                }
            }
        }
    }

    override suspend fun searchSub(query: String): Result<List<DataSubkategori>> {
        return safeApiCall {
            val token = getAuthToken()
            val response = subkategoriApiService.searchSub(query, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    // Custom exception classes for more specific error handling
    class NetworkException(message: String, cause: Throwable) : Exception(message, cause)
    class ApiException(message: String, cause: Throwable) : Exception(message, cause)
    class AuthenticationException(message: String, cause: Throwable) : Exception(message, cause)
}