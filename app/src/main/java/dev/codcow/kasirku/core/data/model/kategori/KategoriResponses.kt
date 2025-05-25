package dev.codcow.kasirku.core.data.model.kategori

data class KategoriResponses(
    val code: Int,
    val `data`: List<DataKategori>,
    val message: String,
    val status: Boolean
)