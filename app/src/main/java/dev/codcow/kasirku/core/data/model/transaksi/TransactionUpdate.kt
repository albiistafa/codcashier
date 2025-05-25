package dev.codcow.kasirku.core.data.model.transaksi

data class TransactionUpdateRequest(
    val status: String,
    val payment_method: String,
    val items: List<MenuItemUpdate>,
    val customer_id: Int? = null,
    val is_delivered: String
)