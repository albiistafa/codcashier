package dev.codcow.kasirku.core.data.model.addToCart

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionRequest(
    @SerialName("items")
    val items: List<CartItemRequest>,

    @SerialName("payment_method")
    val payment_method: String?,

    @SerialName("customer_id")
    val customer_id: Int?,

    @SerialName("nama_transaksi")
    val nama_transaksi: String? = null,

    @SerialName("status")
    val status: String
)