package dev.codcow.kasirku.core.data.model.transaksi

import dev.codcow.kasirku.core.data.model.transaksi.Pagination

data class DataTransaksi(
    val pagination: Pagination,
    val transactions: List<Transaction>
)