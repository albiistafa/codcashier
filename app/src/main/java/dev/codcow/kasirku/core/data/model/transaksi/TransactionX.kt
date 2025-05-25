package dev.codcow.kasirku.core.data.model.transaksi

data class TransactionX(
    val created_at: String,
    val id: Int,
    val nama_transaksi: String,
    val payment_method: String,
    val status: String,
    val total_amount: String,
    val is_delivered: String,
    val transaction_details: List<TransactionDetailXX>,
    val updated_at: String,
    val user_id: Int
)