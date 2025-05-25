package dev.codcow.kasirku.core.data.model.transaksi

data class Pagination(
    val limit: Int,
    val page: Int,
    val total: String,
    val totalPages: Int
)