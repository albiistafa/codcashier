package dev.codcow.kasirku.core.data.model.kategori

data class KategoriResponse(
    val code: Int,
    val `data`: DataKategori,
    val message: String,
    val status: Boolean
)