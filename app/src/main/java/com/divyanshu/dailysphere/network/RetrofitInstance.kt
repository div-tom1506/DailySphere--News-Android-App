package com.divyanshu.dailysphere.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://newsdata.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NewsApiService::class.java)
    }
}