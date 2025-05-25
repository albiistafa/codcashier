package dev.codcow.kasirku.core.data.model.deposit

data class DataDeposit(
    val balance: String,
    val created_at: String,
    val customer_id: Int,
    val customer_name: String,
    val id: Int,
    val phone_number: String,
    val updated_at: String
)