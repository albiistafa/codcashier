package dev.codcow.kasirku.core.data.remote

import dev.codcow.kasirku.core.data.model.pegawai.CreatePegawaiRequest
import dev.codcow.kasirku.core.data.model.pegawai.Data
import dev.codcow.kasirku.core.data.model.pegawai.PegawaiResponse
import dev.codcow.kasirku.core.data.model.pegawai.PegawaiResponses
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PegawaiApiService {
    @GET("api/users/")
    suspend fun getAllPegawai(
        @Header("Authorization") token: String
    ): Response<PegawaiResponses>

    @GET("api/users/{id}")
    suspend fun getPegawaiById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<PegawaiResponse>

    @POST("api/users/create")
    suspend fun createPegawai(
        @Body newPegawai: CreatePegawaiRequest,
        @Header("Authorization") token: String
    ): Response<PegawaiResponses>

    @PUT("api/users/update/{id}")
    suspend fun updatePegawai(
        @Path("id") id: Int,
        @Body updatedPegawai: Data,
        @Header("Authorization") token: String
    ): Response<PegawaiResponses>

    @DELETE("api/users/delete/{id}")
    suspend fun deletePegawai(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<PegawaiResponses>

    @GET("api/users/search/{search}")
    suspend fun searchPegawai(
        @Path("search") search: String,
        @Header("Authorization") token: String
    ): Response<PegawaiResponses>
}