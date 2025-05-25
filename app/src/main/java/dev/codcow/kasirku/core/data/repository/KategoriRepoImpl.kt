package dev.codcow.kasirku.core.data.repository

import android.util.Log
import dev.codcow.kasirku.core.data.model.kategori.CreateKategoriRequest
import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import dev.codcow.kasirku.core.data.model.kategori.KategoriResponses
import dev.codcow.kasirku.core.data.model.kategori.KategoriResponse
import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.core.data.remote.KategoriApiService
import dev.codcow.kasirku.core.domain.repository.KategoriRepository
import dev.codcow.kasirku.core.domain.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KategoriRepoImpl @Inject constructor(
    private val kategoriApiService: KategoriApiService,
    private val tokenManager: TokenRepository
) : KategoriRepository {

    private suspend fun getAuthToken(): String {
        return tokenManager.getToken() ?: throw IllegalStateException("No authentication token available")
    }

    override suspend fun getAllKategori(): Result<List<DataKategori>> {
        return safeApiCall {
            val token = getAuthToken()
            val response = kategoriApiService.getAllKategori(token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getKategoriById(id: Int): Result<KategoriResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = kategoriApiService.getKategoriById(id, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun createKategori(request: CreateKategoriRequest): Result<KategoriResponses> {
        return safeApiCall {
            val token = getAuthToken()
            val response = kategoriApiService.createKategori(request, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun updateKategori(id: Int, kategori: DataKategori): Result<KategoriResponses> {
        return safeApiCall {
            val token = getAuthToken()
            val response = kategoriApiService.updateKategori(id, kategori, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun deleteKategori(id: Int): Result<Unit> {
        return safeApiCall {
            val token = getAuthToken()
            val response = kategoriApiService.deleteKategori(id, token)
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
                        Log.e("KategoriRepo", "Network Error", throwable)
                        Result.failure(NetworkException("Network error occurred", throwable))
                    }
                    is HttpException -> {
                        val errorBody = throwable.response()?.errorBody()?.string()
                        Log.e("KategoriRepo", "HTTP Error ${throwable.code()}: $errorBody", throwable)
                        Result.failure(ApiException("API error: ${throwable.code()}", throwable))
                    }
                    is IllegalStateException -> {
                        Log.e("KategoriRepo", "Authentication Error", throwable)
                        Result.failure(AuthenticationException("No authentication token", throwable))
                    }
                    else -> {
                        Log.e("KategoriRepo", "Unknown Error", throwable)
                        Result.failure(throwable)
                    }
                }
            }
        }
    }

    override suspend fun searchKategori(query: String): Result<List<DataKategori>> {
        return safeApiCall {
            val token = getAuthToken()
            val response = kategoriApiService.searchkategori(query, token)
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