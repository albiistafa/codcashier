package dev.codcow.kasirku.core.data.model.transaksi

data class DataSearchTransaksi(
    val pagination: Pagination,
    val transactions: List<TransactionX>
)