package dev.codcow.kasirku.core.data.remote

import androidx.compose.compiler.plugins.kotlin.inference.Token
import dev.codcow.kasirku.core.data.model.deposit.CreateDataDeposit
import dev.codcow.kasirku.core.data.model.deposit.DataDeposit
import dev.codcow.kasirku.core.data.model.deposit.DepositResponse
import dev.codcow.kasirku.core.data.model.deposit.DepositResponses
import dev.codcow.kasirku.core.data.model.deposit.TopUpData
import dev.codcow.kasirku.core.data.model.deposit.UpdateDataDeposit
import dev.codcow.kasirku.core.data.model.kategori.DataKategori
import dev.codcow.kasirku.core.data.model.kategori.KategoriResponses
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DepositApiService {

    @GET("api/deposits")
    suspend fun getAllDeposit(
        @Header("Authorization") token: String
    ) : Response<DepositResponses>

    @POST("api/deposits/create")
    suspend fun createDeposit(
        @Body createDeposit: CreateDataDeposit,
        @Header("Authorization") token: String
    ) : Response<DepositResponse>

    @GET("api/deposits/customer/{customerId}")
    suspend fun getCustomerById(
        @Path("customerId") customerId: Int,
        @Header("Authorization") token: String
    ) : Response<DepositResponse>

    @PUT("api/deposits/update/{id}")
    suspend fun updateCustomerById(
        @Path("id") id: Int,
        @Body updateDeposit : UpdateDataDeposit,
        @Header("Authorization") token: String
    ) : Response<DepositResponse>

    @PUT("api/deposits/top-up/{id}")
    suspend fun topUpByCustomerId(
        @Path("id") id: Int,
        @Body topUpData : TopUpData,
        @Header("Authorization") token: String
    ) : Response<DepositResponses>

    @DELETE("api/deposits/customer/delete/{id}")
    suspend fun deleteDeposit(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ) : Response<DepositResponses>

    @GET("api/deposits/search/{search}")
    suspend fun searchDeposit(
        @Path("search") search: String,
        @Header("Authorization") token: String
    ): Response<DepositResponses>


}