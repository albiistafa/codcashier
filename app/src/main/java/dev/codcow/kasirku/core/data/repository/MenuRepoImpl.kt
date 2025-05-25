package dev.codcow.kasirku.core.data.repository

import android.util.Log
import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.core.data.model.menu.MenuResponses
import dev.codcow.kasirku.core.data.model.menu.RequestMenu
import dev.codcow.kasirku.core.data.remote.MenuApiService
import dev.codcow.kasirku.core.domain.repository.TokenRepository
import dev.codcow.kasirku.data.repository.MenuRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class MenuRepoImpl @Inject constructor(
    private val menuApiService: MenuApiService,
    private val tokenManager: TokenRepository
) : MenuRepository {

    private suspend fun getAuthToken(): String {
        return tokenManager.getToken() ?: throw IllegalStateException("No authentication token available")
    }

    override suspend fun getAllMenus(): Result<List<DataMenu>> {
        return safeApiCall {
            val token = getAuthToken()
            val response = menuApiService.getAllMenus(token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun getMenuById(id: Int): Result<DataMenu> {
        return safeApiCall {
            val token = getAuthToken()
            val response = menuApiService.getMenuById(id, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun createMenu(menu: RequestMenu): Result<DataMenu> {
        return safeApiCall {
            val token = getAuthToken()
            val response = menuApiService.createMenu(menu, token)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun createMenuWithImage(
        name: String,
        price: String,
        categoryId: Int,
        subCategoryId: Int?,
        imageFile: File?
    ): Result<DataMenu> {
        return safeApiCall {
            val token = getAuthToken()

            // Buat parts untuk form-data
            val namePart = name.toRequestBody("text/plain".toMediaType())
            val pricePart = price.toRequestBody("text/plain".toMediaType())
            val categoryPart = categoryId.toString().toRequestBody("text/plain".toMediaType())

            // Untuk sub-category yang optional
            val subCategoryPart = subCategoryId?.toString()?.toRequestBody("text/plain".toMediaType())

            // Untuk gambar
            val imagePart = if (imageFile != null) {
                val requestFile = imageFile.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("photo", imageFile.name, requestFile)
            } else {
                null
            }

            // Kirim request
            val response = menuApiService.createMenuWithImage(
                name = namePart,
                price = pricePart,
                categoryId = categoryPart,
                subCategoryId = subCategoryPart,
                photo = imagePart,
                token = token
            )

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }
        }
    }

    override suspend fun updateMenu(
        id: Int,
        name: String,
        price: String,
        categoryId: Int,
        subCategoryId: Int?,
        imageFile: File?): Result<DataMenu> {
        return safeApiCall {
            val token = getAuthToken()
            val namePart = name.toRequestBody("text/plain".toMediaType())
            val pricePart = price.toRequestBody("text/plain".toMediaType())
            val categoryPart = categoryId.toString().toRequestBody("text/plain".toMediaType())

            // Untuk sub-category yang optional
            val subCategoryPart = subCategoryId?.toString()?.toRequestBody("text/plain".toMediaType())

            // Untuk gambar
            val imagePart = if (imageFile != null) {
                val requestFile = imageFile.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("photo", imageFile.name, requestFile)
            } else {
                null
            }

            val response = menuApiService.updateMenu(
                id = id,
                name = namePart,
                price = pricePart,
                categoryId = categoryPart,
                subCategoryId = subCategoryPart,
                photo = imagePart,
                token = token
            )

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.data
            } else {
                throw HttpException(response)
            }


        }
    }

    override suspend fun deleteMenu(id: Int): Result<Unit> {
        return safeApiCall {
            val token = getAuthToken()
            val response = menuApiService.deleteMenu(id, token)
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
                        Log.e("MenuRepo", "Network Error", throwable)
                        Result.failure(NetworkException("Network error occurred", throwable))
                    }
                    is HttpException -> {
                        val errorBody = throwable.response()?.errorBody()?.string()
                        Log.e("MenuRepo", "HTTP Error ${throwable.code()}: $errorBody", throwable)
                        Result.failure(ApiException("API error: ${throwable.code()}", throwable))
                    }
                    is IllegalStateException -> {
                        Log.e("MenuRepo", "Authentication Error", throwable)
                        Result.failure(AuthenticationException("No authentication token", throwable))
                    }
                    else -> {
                        Log.e("MenuRepo", "Unknown Error", throwable)
                        Result.failure(throwable)
                    }
                }
            }
        }
    }
    override suspend fun searchMenu(query: String): Result<List<DataMenu>> {
        return safeApiCall {
            val token = getAuthToken()
            val response = menuApiService.searchMenu(query, token)
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