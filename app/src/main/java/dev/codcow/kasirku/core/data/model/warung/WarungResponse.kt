package dev.codcow.kasirku.core.data.model.warung

data class WarungResponse(
    val code: Int,
    val `data`: Data,
    val message: String,
    val status: Boolean
)