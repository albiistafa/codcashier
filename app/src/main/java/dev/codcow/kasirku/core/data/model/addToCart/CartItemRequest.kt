package dev.codcow.kasirku.core.data.model.addToCart

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItemRequest(
    @SerialName("menu_id")
    val menu_id: Int,

    @SerialName("quantity")
    val quantity: Int
)