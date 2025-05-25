package dev.codcow.kasirku.core.data.model.transaksi

data class Data(
    val created_at: String,
    val id: Int,
    val nama_transaksi: String,
    val payment_method: String,
    val status: String,
    val is_delivered: String,
    val total_amount: String,
    val transaction_details: List<TransactionDetailX>,
    val user_id: Int,
    val customer_id: Int? = null
)