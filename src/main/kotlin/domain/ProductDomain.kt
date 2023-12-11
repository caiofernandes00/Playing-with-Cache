package domain

import kotlinx.serialization.Serializable

@Serializable
data class ProductDomain(
    val id: Int,
    val name: String,
    val price: Double
) {
    fun generateETag(): String = generateETag(this)
}