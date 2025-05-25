package dev.codcow.kasirku.core.data.model.menu

data class DataMenu(
    val id: Int,
    val name: String,
    val price: String,
    val category_id: Int,
    val sub_category_id: Int?,
    val photo: String?,
    val created_at: String?,
    val updated_at: String?
)