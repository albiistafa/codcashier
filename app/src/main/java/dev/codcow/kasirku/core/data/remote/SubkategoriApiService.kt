package dev.codcow.kasirku.core.data.remote

import dev.codcow.kasirku.core.data.model.kategori.CreateKategoriRequest
import dev.codcow.kasirku.core.data.model.kategori.KategoriResponses
import dev.codcow.kasirku.core.data.model.subkategori.CreateSubkategoriRequest
import dev.codcow.kasirku.core.data.model.subkategori.SubkategoriResponses
import dev.codcow.kasirku.core.data.model.subkategori.DataSubkategori
import dev.codcow.kasirku.core.data.model.subkategori.SubkategoriResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface SubkategoriApiService{

    @GET("api/sub-categories")
    suspend fun getAllSubKategori(
        @Header("Authorization") token: String
    ): Response<SubkategoriResponses>

    @GET("api/sub-categories/{id}")
    suspend fun getSubKategoriById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<SubkategoriResponse>

    @POST("api/sub-categories/create")
    suspend fun createSubKategori(
        @Body newKategori: CreateSubkategoriRequest,
        @Header("Authorization") token: String
    ): Response<SubkategoriResponse>

    @PUT("api/sub-categories/update/{id}")
    suspend fun updateSubKategori(
        @Path("id") id: Int,
        @Body updatedKategori: DataSubkategori,
        @Header("Authorization") token: String
    ): Response<SubkategoriResponse>

    @DELETE("api/sub-categories/delete/{id}")
    suspend fun deleteSubKategori(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<SubkategoriResponses>

    @GET("api/sub-categories/search/{search}")
    suspend fun searchSub(
        @Path("search") search: String,
        @Header("Authorization") token: String
    ): Response<SubkategoriResponses>
}