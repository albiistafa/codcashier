package dev.codcow.kasirku.core.data.model.transaksi


data class TransaksiResponses(
    val code: Int,
    val `data`: DataTransaksi,
    val message: String,
    val status: Boolean
)



