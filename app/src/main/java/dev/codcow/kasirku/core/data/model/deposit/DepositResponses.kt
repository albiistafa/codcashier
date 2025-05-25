package dev.codcow.kasirku.core.data.model.deposit

data class DepositResponses(
    val code: Int,
    val `data`: List<DataDeposit>,
    val message: String,
    val status: Boolean
)