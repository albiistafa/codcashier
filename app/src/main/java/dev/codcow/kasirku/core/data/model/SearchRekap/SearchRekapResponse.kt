package dev.codcow.kasirku.core.data.model.SearchRekap

data class SearchRekapResponse(
    val code: Int,
    val `data`: DataSearch,
    val message: String,
    val status: Boolean
)