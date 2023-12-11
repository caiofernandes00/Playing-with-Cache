package com.example.adapter.ktor.plugins

import com.example.adapter.exposed.CustomersRepository
import com.example.adapter.exposed.ProductsRepository
import com.example.domain.generateETag
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureHttpServer(
    productsRepository: ProductsRepository,
    customersRepository: CustomersRepository
) {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/products") {
            call.response.headers.append(HttpHeaders.CacheControl, "no-cache")
            val products = productsRepository.list()

            val eTag = generateETag(products)
            val requestETag = call.request.header(HttpHeaders.IfNoneMatch)
            if (requestETag == eTag) {
                call.respond(HttpStatusCode.NotModified)
            } else {
                call.response.headers.append(HttpHeaders.ETag, eTag)
                call.respond(products)
            }
        }

        get("/customers") {
            call.response.headers.append(HttpHeaders.CacheControl, "must-revalidate,max-age=60,public")
            val customers = customersRepository.list()

            val eTag = generateETag(customers)
            val requestETag = call.request.header(HttpHeaders.IfNoneMatch)
            if (requestETag == eTag) {
                call.respond(HttpStatusCode.NotModified)
            } else {
                call.response.headers.append(HttpHeaders.ETag, eTag)
                call.respond(customers)
            }
        }
    }
}
