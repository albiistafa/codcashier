package dev.codcow.kasirku.core.data.model.transaksi

data class Item(
    val menu: Menu,
    val menu_id: String,
    val quantity: String,
    val subtotal: Int
)