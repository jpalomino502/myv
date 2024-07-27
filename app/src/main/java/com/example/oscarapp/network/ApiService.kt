package com.example.oscarapp.network

import com.example.oscarapp.models.ServiceRequest
import com.example.oscarapp.models.TicketResponse
import com.example.oscarapp.models.UserResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("auth/login")
    @FormUrlEncoded
    fun login(@Field("email") email: String, @Field("password") password: String): Call<UserResponse>

    @GET("tickets/get-all-by-user-id/{userId}")
    fun getTickets(@Path("userId") userId: String): Call<TicketResponse>

    @POST("diligencias")
    suspend fun sendServiceRequest(@Body request: ServiceRequest): Response<Unit>
}
