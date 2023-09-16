package domain

fun generateETag(data: Any): String {
    val bytes = java.security.MessageDigest.getInstance("MD5").digest(data.toString().toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}