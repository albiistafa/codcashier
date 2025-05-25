package dev.codcow.kasirku.core.data.model.pengeluaran

data class TransactionDetail(
    val menu_id: Int,
    val menu_name: String,
    val menu_price: Int,
    val quantity: Int,
    val subtotal: Int
)