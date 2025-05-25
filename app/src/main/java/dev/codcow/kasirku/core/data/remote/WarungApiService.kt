package dev.codcow.kasirku.core.data.remote

import dev.codcow.kasirku.core.data.model.warung.RequestWarung
import dev.codcow.kasirku.core.data.model.warung.WarungResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface WarungApiService {

    @GET("api/warung/")
    suspend fun getWarung(
        @Header("Authorization") token: String
    ): retrofit2.Response<WarungResponse>

    @POST("api/warung/")
    suspend fun postWarung(
        @Body name: RequestWarung,
        @Header("Authorization") token: String
    ): retrofit2.Response<WarungResponse>

}