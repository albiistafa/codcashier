package dev.codcow.kasirku.core.data.model.transaksi

data class CreateTransactionResponse(
    val code: Int,
    val `data`: DataX,
    val message: String,
    val status: Boolean
)