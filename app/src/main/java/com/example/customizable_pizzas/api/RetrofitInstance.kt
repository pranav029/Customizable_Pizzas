package com.example.customizable_pizzas.api

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RetrofitInstance {
    companion object{
        private val retrofit by lazy {
            Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .baseUrl("https://newsapi.org/")
                .build()
        }
        val get by lazy {
            retrofit.create(MyApi::class.java)
        }
    }
}