package dev.codcow.kasirku.core.domain.repository

import dev.codcow.kasirku.core.domain.model.AuthResult
import dev.codcow.kasirku.core.data.model.login.LoginRequest

interface LoginRepository {

    suspend fun login(request: LoginRequest): Result<AuthResult>

}