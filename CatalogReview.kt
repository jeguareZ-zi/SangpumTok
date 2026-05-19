package com.neonloop.sangpumtok.model

data class CatalogReview(
    val id: String,
    val productName: String,
    val category: String,
    val optionName: String,
    val description: String,
    val memo: String,
    val result: String,
    val reason: String,
    val createdAt: String
)