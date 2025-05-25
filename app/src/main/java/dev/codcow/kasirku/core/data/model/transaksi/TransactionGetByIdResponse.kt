package dev.codcow.kasirku.core.data.model.transaksi

data class TransactionGetByIdResponse(
    val code: Int,
    val `data`: Data,
    val message: String,
    val status: Boolean
)