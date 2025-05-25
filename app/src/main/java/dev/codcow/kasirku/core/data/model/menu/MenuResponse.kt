package dev.codcow.kasirku.core.data.model.menu

data class MenuResponse(
    val code: Int,
    val `data`: DataMenu,
    val message: String,
    val status: Boolean
)