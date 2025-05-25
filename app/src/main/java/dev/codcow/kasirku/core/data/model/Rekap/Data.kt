package dev.codcow.kasirku.core.data.model.Rekap

data class Data(
    val pagination: Pagination,
    val transactions: List<Transaction>
)