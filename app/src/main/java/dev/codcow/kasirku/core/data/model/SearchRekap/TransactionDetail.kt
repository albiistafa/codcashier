package dev.codcow.kasirku.core.data.model.SearchRekap

data class TransactionDetail(
    val menu_id: Int = 0,
    val menu_name: String? = "Item Tidak Diketahui", // Ubah menjadi nullable dengan default
    val menu_price: Int = 0,
    val quantity: Int = 0,
    val subtotal: Int = 0
)