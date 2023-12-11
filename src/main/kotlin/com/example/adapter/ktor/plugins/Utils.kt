package com.example.adapter.ktor.plugins

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

fun checkIfCacheIsStale(lastTimeFetched: Instant?, cacheControlMaxAge: Int?): Boolean {
    if (lastTimeFetched == null || cacheControlMaxAge == null) return true

    val currentTime = Clock.System.now()
    val timeElapsedInSeconds =
        (currentTime.toEpochMilliseconds() - lastTimeFetched.toEpochMilliseconds()) / 1000
    return timeElapsedInSeconds <= cacheControlMaxAge
}

object Cache {
    private val map = mutableMapOf<String, String>()

    fun get(key: String): String? {
        return map[key]
    }

    fun set(key: String, value: String) {
        map[key] = value
    }
}