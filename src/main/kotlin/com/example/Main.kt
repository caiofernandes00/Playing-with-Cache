package com.example

import com.example.adapter.exposed.CustomersRepository
import com.example.adapter.exposed.ProductsRepository
import com.example.adapter.exposed.config.configureDatabase
import com.example.adapter.ktor.plugins.HttpServerConfig
import com.example.adapter.ktor.plugins.configureHttpClient
import com.example.adapter.ktor.plugins.configureHttpServer
import com.example.adapter.ktor.plugins.fetchProducts
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch

val caching: String = System.getenv("SERVER_USE_CACHING") ?: "true"
val etags: String = System.getenv("SERVER_USE_ETAGS") ?: "true"
val databaseConfig = ApplicationConfig("database.conf")

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.execute() {
    configureDatabase(databaseConfig)
    setupHttpServer(this)
    setupHttpClient(this)
}

private fun setupHttpServer(application: Application) {
    val productsRepository = ProductsRepository()
    val customersRepository = CustomersRepository()

    val httpServerConfig = HttpServerConfig(
        productsRepository = productsRepository,
        customersRepository = customersRepository,
        useCache = caching == "true",
        useETags = etags == "true"
    )

    application.configureHttpServer(httpServerConfig)
}

private fun setupHttpClient(application: Application) {
    val client = application.configureHttpClient()

    application.launch {
        val products = application.fetchProducts(client).invoke()
        println("Products: $products")
    }
}