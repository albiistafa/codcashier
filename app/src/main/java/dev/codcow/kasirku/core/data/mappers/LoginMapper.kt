package dev.codcow.kasirku.core.data.mappers

import dev.codcow.kasirku.core.data.model.login.LoginResponse
import dev.codcow.kasirku.core.domain.model.AuthResult

fun LoginResponse.toDomain(): AuthResult {
    return AuthResult(
        userId = this.data.user.id,
        userName = this.data.user.name,
        role = this.data.user.role,
        phoneNumber = this.data.user.phone_number,
        token = this.data.token
    )
}
