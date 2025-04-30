package com.divyanshu.dailysphere.model

data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val results: List<Article>
)

data class Article(
    val title: String,
    val description: String?,
    val link: String?,
    val imageUrl: String?,
    val pubDate: String?,
    val content: String?
)