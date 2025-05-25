package dev.codcow.kasirku.core.data.repository

import android.util.Log
import dev.codcow.kasirku.core.data.model.warung.Data
import dev.codcow.kasirku.core.data.model.warung.RequestWarung
import dev.codcow.kasirku.core.data.model.warung.WarungResponse
import dev.codcow.kasirku.core.data.remote.WarungApiService
import dev.codcow.kasirku.core.data.repository.TransaksiRepoImpl.ApiException
import dev.codcow.kasirku.core.data.repository.TransaksiRepoImpl.AuthenticationException
import dev.codcow.kasirku.core.data.repository.TransaksiRepoImpl.NetworkException
import dev.codcow.kasirku.core.domain.repository.TokenRepository
import dev.codcow.kasirku.core.domain.repository.WarungRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class WarungRepoImpl @Inject constructor(
    private val warungApiService: WarungApiService,
    private val tokenManager: TokenRepository
) : WarungRepository{

    private suspend fun getAuthToken(): String {
        return tokenManager.getToken() ?: throw IllegalStateException("No authentication token available")
    }

    override suspend fun getWarung(): Result<Data> {
        return safeApiCall {
            val token = getAuthToken()
            val response = warungApiService.getWarung(token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun postWarung(name: RequestWarung): Result<WarungResponse> {
        return safeApiCall {
            val token = getAuthToken()
            val response = warungApiService.postWarung(name, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
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

    class NetworkException(message: String, cause: Throwable) : Exception(message, cause)
    class ApiException(message: String, cause: Throwable) : Exception(message, cause)
    class AuthenticationException(message: String, cause: Throwable) : Exception(message, cause)

}