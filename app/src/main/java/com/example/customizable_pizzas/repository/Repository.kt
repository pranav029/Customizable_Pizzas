package com.example.customizable_pizzas.repository

import com.example.customizable_pizzas.api.RetrofitInstance
import com.example.customizable_pizzas.modal.ResponseData
import retrofit2.Response

class Repository {
    suspend fun fetchItems(): Response<ResponseData> {
        return RetrofitInstance.get.getData()
    }
}