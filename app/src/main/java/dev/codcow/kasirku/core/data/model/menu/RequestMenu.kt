package dev.codcow.kasirku.core.data.model.menu

import java.io.File

data class RequestMenu(
    val name: String,
    val price: String,
    val photo: File?,
    val category_id: Int,
    val sub_category_id: Int?
)