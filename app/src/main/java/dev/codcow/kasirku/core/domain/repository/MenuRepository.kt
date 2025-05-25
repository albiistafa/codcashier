package dev.codcow.kasirku.data.repository

import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.core.data.model.menu.MenuResponse
import dev.codcow.kasirku.core.data.model.menu.MenuResponses
import dev.codcow.kasirku.core.data.model.menu.RequestMenu
import retrofit2.http.Query
import java.io.File

interface MenuRepository {
    suspend fun getAllMenus(): Result<List<DataMenu>>
    suspend fun getMenuById(id: Int): Result<DataMenu>
    suspend fun createMenu(menu: RequestMenu): Result<DataMenu>
    suspend fun createMenuWithImage(
        name: String,
        price: String,
        categoryId: Int,
        subCategoryId: Int?,
        imageFile: File?
    ): Result<DataMenu>
    suspend fun updateMenu(
        id: Int,
        name: String,
        price: String,
        categoryId: Int,
        subCategoryId: Int?,
        imageFile: File?): Result<DataMenu>
    suspend fun deleteMenu(id: Int): Result<Unit>
    suspend fun searchMenu(query: String): Result<List<DataMenu>>
}
