package dev.codcow.kasirku.core.data.model.SearchRekap

data class SearchRekapListResponse(
    val code: Int,
    val message: String,
    val data: List<dev.codcow.kasirku.core.data.model.SearchRekap.Transaction>
)
