package com.example.adapter.kreds.config

import com.example.adapter.ktor.extensions.toMap
import io.github.crackthecodeabhi.kreds.connection.Endpoint
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.github.crackthecodeabhi.kreds.connection.newClient
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*

fun configureCache(config: ApplicationConfig): KredsClient {
    val cacheProps = getCacheProperties(config)
    return runBlocking{
        withContext(Dispatchers.IO) {
            newClient(Endpoint.from(cacheProps.getProperty("host")))
        }
    }
}

private fun getCacheProperties(config: ApplicationConfig) =
    Properties().apply {
        putAll(config.toMap("kreds.cache"))
    }
