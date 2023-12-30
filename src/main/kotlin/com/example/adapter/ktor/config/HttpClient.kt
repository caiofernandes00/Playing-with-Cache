package com.example.adapter.ktor.config

import com.example.adapter.ktor.plugins.configureHttpClient
import com.example.adapter.ktor.plugins.fetchProducts
import io.ktor.server.application.*
import kotlinx.coroutines.launch

fun Application.setupHttpClient() {
    val client = configureHttpClient()

    launch {
        val products = fetchProducts(client).invoke()
        println("Products: $products")
    }
}