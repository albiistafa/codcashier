package dev.codcow.kasirku.core.data.model.addToCart

import kotlinx.serialization.Serializable

@Serializable
data class Cart(
    val items: List<CartItem> = emptyList(),
    val customerId: Int? = null,
    val paymentMethod: String? = null,
    val totalPrice: Double = 0.0
)