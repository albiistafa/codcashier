package dev.codcow.kasirku.core.data.model.pegawai

data class PegawaiResponse(
    val code: Int,
    val `data`: Data,
    val message: String,
    val status: Boolean
)