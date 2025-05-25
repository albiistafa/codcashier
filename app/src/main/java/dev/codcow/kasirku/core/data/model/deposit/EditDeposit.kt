package dev.codcow.kasirku.core.data.model.deposit

data class EditDeposit(
    val balance: String,
    val created_at: String,
    val customer_id: Int,
    val name: String,
    val id: Int,
    val phone_number: String,
    val updated_at: String
)