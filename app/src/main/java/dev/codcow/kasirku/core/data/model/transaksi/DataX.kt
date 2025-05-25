package dev.codcow.kasirku.core.data.model.transaksi

data class DataX(
    val created_at: String,
    val id: Int,
    val items: List<Item>,
    val nama_transaksi: String,
    val payment_method: String,
    val status: String,
    val is_delivered: String,
    val total_amount: String,
    val type: String,
    val updated_at: String,
    val user_id: Int
)