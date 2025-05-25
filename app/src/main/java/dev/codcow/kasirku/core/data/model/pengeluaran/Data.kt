package dev.codcow.kasirku.core.data.model.pengeluaran

data class Data(
    val last_updated_time: String,
    val pagination: Pagination,
    val total_pengeluaran: String,
    val transaction_pengeluaran: List<TransactionPengeluaran>
)