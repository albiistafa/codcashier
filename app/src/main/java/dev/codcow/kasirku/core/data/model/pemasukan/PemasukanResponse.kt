package dev.codcow.kasirku.core.data.model.pemasukan

data class PemasukanResponse(
    val code: Int,
    val `data`: Data,
    val message: String,
    val status: Boolean
)