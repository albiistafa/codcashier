package dev.codcow.kasirku.core.data.model.SearchRekap

data class DataSearch(
    val pagination: Pagination,
    val transactions: List<Transaction>
)