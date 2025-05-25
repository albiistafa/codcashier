package dev.codcow.kasirku.core.data.model.login

import dev.codcow.kasirku.core.data.model.user.DataUser

data class LoginResponse(
    val code: Int,
    val `data`: DataUser,
    val message: String,
    val status: Boolean
)