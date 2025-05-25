package dev.codcow.kasirku.core.data.remote

import dev.codcow.kasirku.core.data.model.menu.DataMenu
import dev.codcow.kasirku.core.data.model.menu.MenuResponse
import dev.codcow.kasirku.core.data.model.menu.MenuResponses
import dev.codcow.kasirku.core.data.model.menu.RequestMenu
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface MenuApiService {

    @GET("api/menus")
    suspend fun getAllMenus(
        @Header("Authorization") token: String
    ): Response<MenuResponses>

    @GET("api/menus/{id}")
    suspend fun getMenuById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<MenuResponse>

    @POST("api/menus/create")
    suspend fun createMenu(
        @Body newMenu: RequestMenu,
        @Header("Authorization") token: String
    ): Response<MenuResponse>

    @Multipart
    @POST("api/menus/create")
    suspend fun createMenuWithImage(
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("sub_category_id") subCategoryId: RequestBody?,
        @Part photo: MultipartBody.Part?,
        @Header("Authorization") token: String
    ): Response<MenuResponse>

    @Multipart
    @PUT("api/menus/update/{id}")
    suspend fun updateMenu(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("price") price: RequestBody,
        @Part("category_id") categoryId: RequestBody,
        @Part("sub_category_id") subCategoryId: RequestBody?,
        @Part photo: MultipartBody.Part?,
        @Header("Authorization") token: String
    ): Response<MenuResponse>

    @DELETE("api/menus/delete/{id}")
    suspend fun deleteMenu(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<MenuResponses>

    @GET("api/menus/search/{search}")
    suspend fun searchMenu(
        @Path("search") search: String,
        @Header("Authorization") token: String
    ): Response<MenuResponses>

}