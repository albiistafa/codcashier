package dev.codcow.kasirku.core.data.repository

import android.util.Log
import dev.codcow.kasirku.core.data.mappers.toDomain
import dev.codcow.kasirku.core.domain.model.AuthResult
import dev.codcow.kasirku.core.domain.repository.LoginRepository
import dev.codcow.kasirku.core.data.model.login.LoginRequest
import dev.codcow.kasirku.core.data.remote.LoginApiService
import dev.codcow.kasirku.core.domain.repository.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepoImpl @Inject constructor(
    private val loginApiService: LoginApiService,
    private val tokenManager: TokenRepository
) : LoginRepository {

    override suspend fun login(request: LoginRequest): Result<AuthResult> {
        return runCatching {
            withContext(Dispatchers.IO) {
                val response = loginApiService.postLogin(
                    phoneNumber = request.phoneNumber,
                    password = request.password
                )

                when {
                    response.isSuccessful && response.body() != null -> {
                        val authResult = response.body()!!.toDomain()
                        tokenManager.saveToken(authResult.token)
                        authResult
                    }
                    response.code() == 401 -> {
                        throw AuthenticationException("Invalid Password")
                    }
                    response.code() == 404 -> {
                        throw AuthorizationException("User Not Found")
                    }
                    else -> {
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("LoginRepo", "Error ${response.code()}: $errorBody")
                        throw HttpException(response)
                    }
                }
            }
        }.onFailure { exception ->
            when (exception) {
                is IOException -> Log.e("LoginRepo", "Network error", exception)
                is HttpException -> Log.e("LoginRepo", "HTTP error", exception)
                is AuthenticationException -> Log.e("LoginRepo", "Authentication failed", exception)
                is AuthorizationException -> Log.e("LoginRepo", "Authorization failed", exception)
                else -> Log.e("LoginRepo", "Unexpected error", exception)
            }
        }
    }
}

class AuthenticationException(message: String) : Exception(message)
class AuthorizationException(message: String) : Exception(message)