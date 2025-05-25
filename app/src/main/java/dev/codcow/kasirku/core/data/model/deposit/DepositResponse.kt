package dev.codcow.kasirku.core.data.model.deposit

data class DepositResponse(
    val code: Int,
    val `data`: EditDeposit,
    val message: String,
    val status: Boolean
)