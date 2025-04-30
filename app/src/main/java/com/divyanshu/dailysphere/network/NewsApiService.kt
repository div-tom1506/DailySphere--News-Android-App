package com.divyanshu.dailysphere.network

import com.divyanshu.dailysphere.model.NewsResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("api/1/news")
    fun getNews(
        @Query("apiKey") apiKey: String,
        //@Query("q") query: String?,
        @Query("language") language: String = "en",
        @Query("country") country: String = "in",
        @Query("category") category: String?,
        @Query("page") page: Int = 1
    ): Call<NewsResponse>
}