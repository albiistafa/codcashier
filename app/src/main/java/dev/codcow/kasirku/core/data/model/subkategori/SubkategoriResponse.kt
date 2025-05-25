package dev.codcow.kasirku.core.data.model.subkategori

data class SubkategoriResponse(
    val code: Int,
    val `data`: DataSubkategori,
    val message: String,
    val status: Boolean
)