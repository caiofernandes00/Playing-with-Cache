package com.example.domain

data class PaginationFilters(
    val perPage: Int = 10,
    val page: Long = 0,
)