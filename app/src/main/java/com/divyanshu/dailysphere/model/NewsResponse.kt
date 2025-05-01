package com.divyanshu.dailysphere.model

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val results: List<Article>,
    val nextPage: String?
)

data class Article(
    val title: String,
    val description: String?,
    val link: String?,
    val image_url: String?,
    val pubDate: String?,
    val content: String?
)