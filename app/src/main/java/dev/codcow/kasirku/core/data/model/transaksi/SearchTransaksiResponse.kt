package dev.codcow.kasirku.core.data.model.transaksi

data class SearchTransaksiResponse(
    val code: Int,
    val `data`: DataSearchTransaksi,
    val message: String,
    val status: Boolean
)