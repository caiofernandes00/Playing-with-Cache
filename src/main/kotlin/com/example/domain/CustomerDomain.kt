package com.example.domain

import kotlinx.serialization.Serializable

@Serializable
data class CustomerDomain(
    val id: Int,
    val name: String,
    val age: Int
) {
    fun generateETag(): String = generateETag(this)
}