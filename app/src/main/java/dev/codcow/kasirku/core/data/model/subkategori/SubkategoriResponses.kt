package dev.codcow.kasirku.core.data.model.subkategori

data class SubkategoriResponses(
    val code: Int,
    val `data`: List<DataSubkategori>,
    val message: String,
    val status: Boolean
)