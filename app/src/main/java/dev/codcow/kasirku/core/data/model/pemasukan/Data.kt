package dev.codcow.kasirku.core.data.model.pemasukan

data class Data(
    val last_updated_time: String,
    val pagination: Pagination,
    val total_pemasukan: String,
    val transaction_pemasukan: List<TransactionPemasukan>
)