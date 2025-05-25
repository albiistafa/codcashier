package dev.codcow.kasirku.core.data.model.pegawai

data class PegawaiResponses(
    val code: Int,
    val `data`: List<Data>,
    val message: String,
    val status: Boolean
)