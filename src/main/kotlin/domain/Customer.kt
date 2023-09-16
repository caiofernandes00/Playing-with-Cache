package domain

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: Int,
    val name: String,
    val age: Int
) {
    fun generateETag(): String = generateETag(this)
}