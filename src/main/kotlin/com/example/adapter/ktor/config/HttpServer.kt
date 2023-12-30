package com.example.adapter.ktor.config

import com.example.adapter.exposed.CustomersRepository
import com.example.adapter.exposed.ProductsRepository
import com.example.adapter.ktor.plugins.HttpServerConfig
import com.example.adapter.ktor.plugins.configureHttpServer
import com.example.caching
import com.example.etags
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.ktor.server.application.*

fun Application.setupHttpServer(
    cacheClient: KredsClient,
    productsRepository: ProductsRepository,
    customersRepository: CustomersRepository
) {

    val httpServerConfig = HttpServerConfig(
        productsRepository = productsRepository,
        customersRepository = customersRepository,
        cacheClient = cacheClient,
        useCache = caching == "true",
        useETags = etags == "true"
    )

    configureHttpServer(httpServerConfig)
}