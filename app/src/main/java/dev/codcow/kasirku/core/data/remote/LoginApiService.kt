package dev.codcow.kasirku.core.data.remote

import dev.codcow.kasirku.core.data.model.login.LoginResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginApiService {
    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun postLogin(
        @Field("phone_number") phoneNumber: String,
        @Field("password") password: String
    ): Response<LoginResponse>
}