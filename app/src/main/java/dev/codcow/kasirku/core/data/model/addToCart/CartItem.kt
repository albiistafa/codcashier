package dev.codcow.kasirku.core.data.model.addToCart

import kotlinx.serialization.Serializable

@Serializable
data class CartItem(
    val menuId: Int,
    val quantity: Int,
    val name: String = "", // Optional untuk menampilkan nama menu
    val price: String = "",
    val photo: String?,
)