package dev.codcow.kasirku.core.data.model.pemasukan

data class TransactionPemasukan(
    val created_at: String,
    val id: Int,
    val nama_transaksi: String,
    val payment_method: String,
    val status: String,
    val total_amount: String,
    val transaction_details: List<TransactionDetail>,
    val updated_at: String,
    val user_id: Int
)