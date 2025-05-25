package dev.codcow.kasirku.core.data.model.menu

data class MenuResponses(
    val code: Int,
    val `data`: List<DataMenu>,
    val message: String,
    val status: Boolean
)