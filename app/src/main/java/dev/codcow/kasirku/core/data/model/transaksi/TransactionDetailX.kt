package dev.codcow.kasirku.core.data.model.transaksi

data class TransactionDetailX(
    val menu_id: Int,
    val menu_name: String,
    val menu_price: String,
    val quantity: Int,
    val subtotal: String
)