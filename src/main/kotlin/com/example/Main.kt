package com.example

import com.example.adapter.exposed.CustomersRepository
import com.example.adapter.exposed.ProductsRepository
import com.example.adapter.exposed.config.configureDatabase
import com.example.adapter.kreds.config.configureCache
import com.example.adapter.ktor.config.setupHttpServer
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*

val caching: String = System.getenv("SERVER_USE_CACHING") ?: "true"
val etags: String = System.getenv("SERVER_USE_ETAGS") ?: "true"
val databaseConfig = ApplicationConfig("database.conf")
val cacheConfig = ApplicationConfig("cache.conf")

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.execute() {
    configureDatabase(databaseConfig)
    val cacheClient = configureCache(cacheConfig)

    setupHttpServer(
        cacheClient,
        ProductsRepository(),
        CustomersRepository()
    )
}



