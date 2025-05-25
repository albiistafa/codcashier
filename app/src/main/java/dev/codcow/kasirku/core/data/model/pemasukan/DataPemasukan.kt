package dev.codcow.kasirku.core.data.model.pemasukan

import dev.codcow.kasirku.core.data.model.pengeluaran.TransactionPengeluaran
import dev.codcow.kasirku.core.data.model.transaksi.Pagination
import dev.codcow.kasirku.core.data.model.transaksi.Transaction

data class DataPemasukan(
    val pagination: Pagination,
    val transactions: List<TransactionPemasukan>
)