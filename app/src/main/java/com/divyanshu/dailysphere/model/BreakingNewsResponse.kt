package com.divyanshu.dailysphere.model

data class BreakingNewsResponse(
    val results: List<BreakingNewsArticle>
)

data class BreakingNewsArticle(
    val title: String,
    val description: String?,
    val link: String?
)