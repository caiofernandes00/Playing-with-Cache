package com.example

import com.example.adapter.exposed.CustomersRepository
import com.example.adapter.exposed.ProductsRepository
import com.example.adapter.exposed.config.configureDatabase
import com.example.adapter.ktor.plugins.configureHttpClient
import com.example.adapter.ktor.plugins.configureHttpServer
import com.example.adapter.ktor.plugins.fetchProducts
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch

val databaseConfig = ApplicationConfig("database.conf")

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.execute() {
    configureDatabase(databaseConfig)

    val productsRepository = ProductsRepository()
    val customersRepository = CustomersRepository()

    configureHttpServer(
        productsRepository = productsRepository,
        customersRepository = customersRepository
    )
    val client = configureHttpClient()

    launch {
        val products = fetchProducts(client).invoke()
        println("Products: $products")
    }
}

