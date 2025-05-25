package dev.codcow.kasirku.core.data.model.pengeluaran

data class PengeluaranResponse(
    val code: Int,
    val `data`: Data,
    val message: String,
    val status: Boolean
)