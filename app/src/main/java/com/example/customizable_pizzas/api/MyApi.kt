package com.example.customizable_pizzas.api

import com.example.customizable_pizzas.modal.ResponseData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MyApi {
    @GET("https://625bbd9d50128c570206e502.mockapi.io/api/v1/pizza/1")
    suspend fun getData(): Response<ResponseData>
}