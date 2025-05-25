package dev.codcow.kasirku.core.data.model.transaksi

data class Transaction(
    val created_at: String,
    val id: Int,
    val nama_transaksi: String = "",
    val payment_method: String,
    val status: String,
    val is_delivered: String,
    val total_amount: String,
    val transaction_details: List<TransactionDetail>,
    val updated_at: String,
    val user_id: Int,
    val customer_id: Int? = null
)