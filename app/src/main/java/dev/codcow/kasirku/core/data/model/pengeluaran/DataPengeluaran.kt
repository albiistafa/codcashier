package dev.codcow.kasirku.core.data.model.pengeluaran

import dev.codcow.kasirku.core.data.model.transaksi.Pagination
import dev.codcow.kasirku.core.data.model.transaksi.Transaction

data class DataPengeluaran(
    val pagination: Pagination,
    val transactions: List<TransactionPengeluaran>
)