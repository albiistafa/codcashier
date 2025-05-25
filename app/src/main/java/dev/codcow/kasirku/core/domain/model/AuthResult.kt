package dev.codcow.kasirku.core.domain.model

data class AuthResult(
    val userId: Int,
    val userName: String,
    val role: String,
    val phoneNumber: String,
    val token: String
)