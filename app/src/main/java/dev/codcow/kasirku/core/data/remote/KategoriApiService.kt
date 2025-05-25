package dev.codcow.kasirku.core.data.remote

import dev.codcow.kasirku.core.data.model.kategori.CreateKategoriRequest
import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import dev.codcow.kasirku.core.data.model.kategori.KategoriResponse
import dev.codcow.kasirku.core.data.model.kategori.KategoriResponses
import dev.codcow.kasirku.core.data.model.menu.MenuResponses
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface KategoriApiService{

    @GET("api/categories/")
    suspend fun getAllKategori(
        @Header("Authorization") token: String
    ): Response<KategoriResponses>

    @GET("api/categories/{id}")
    suspend fun getKategoriById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<KategoriResponse>

    @POST("api/categories/create")
    suspend fun createKategori(
        @Body newKategori: CreateKategoriRequest,
        @Header("Authorization") token: String
    ): Response<KategoriResponses>

    @PUT("api/categories/update/{id}")
    suspend fun updateKategori(
        @Path("id") id: Int,
        @Body updatedKategori: DataKategori,
        @Header("Authorization") token: String
    ): Response<KategoriResponses>

    @DELETE("api/categories/delete/{id}")
    suspend fun deleteKategori(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<KategoriResponses>

    @GET("api/categories/search/{search}")
    suspend fun searchkategori(
        @Path("search") search: String,
        @Header("Authorization") token: String
    ): Response<KategoriResponses>
}