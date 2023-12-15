package com.example.adapter.ktor.plugins

import com.example.adapter.exposed.CustomersRepository
import com.example.adapter.exposed.ProductsRepository
import com.example.domain.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

data class HttpServerConfig(
    val productsRepository: ProductsRepository,
    val customersRepository: CustomersRepository,
    val useCache: Boolean = true,
    val useETags: Boolean = true
)


fun Application.configureHttpServer(
    httpServerConfig: HttpServerConfig
) {
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/products") {
            call.response.headers.append(HttpHeaders.CacheControl, "must-revalidate,max-age=60,public")
            val products = if (httpServerConfig.useCache){
                httpServerConfig.productsRepository.list(getFilters())
            } else {
                httpServerConfig.productsRepository.list(getFilters())
            }

            if (httpServerConfig.useETags) {
                useEtags(products)
            } else {
                call.respond(products)
            }
        }

        get("/products/{id}") {
            call.response.headers.append(HttpHeaders.CacheControl, "must-revalidate,max-age=60,public")
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val product = if (httpServerConfig.useCache) {
                    httpServerConfig.productsRepository.getById(id)
                } else {
                    httpServerConfig.productsRepository.getById(id)
                }
                prepareGetByIdResponse(product)
            }
        }

        post("/products") {
            val product = call.receive<ProductCreationDomain>()
            httpServerConfig.productsRepository.create(product)
            call.respond(HttpStatusCode.Created)
        }

        get("/customers") {
            call.response.headers.append(HttpHeaders.CacheControl, "must-revalidate,max-age=60,public")
            val customers = if (httpServerConfig.useCache) {
                httpServerConfig.customersRepository.list(getFilters())
            } else {
                httpServerConfig.customersRepository.list(getFilters())
            }

            if (httpServerConfig.useETags) {
                useEtags(customers)
            } else {
                call.respond(customers)
            }
        }

        get("/customers/{id}") {
            call.response.headers.append(HttpHeaders.CacheControl, "must-revalidate,max-age=60,public")
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val customer = if (httpServerConfig.useCache) {
                    httpServerConfig.customersRepository.getById(id)
                } else {
                    httpServerConfig.customersRepository.getById(id)
                }
                prepareGetByIdResponse(customer)
            }
        }

        post("/customers") {
            val customer = call.receive<CustomerCreationDomain>()
            httpServerConfig.customersRepository.create(customer)
            call.respond(HttpStatusCode.Created)
        }
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.prepareGetByIdResponse(
    payload: Any?
) {
    if (payload == null) {
        call.respond(HttpStatusCode.NotFound)
    } else {
        call.respond(payload)
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.useEtags(
    payload: Any
) {
    val eTag = generateETag(payload)
    val requestETag = call.request.header(HttpHeaders.IfNoneMatch)
    if (requestETag == eTag) {
        call.respond(HttpStatusCode.NotModified)
    } else {
        call.response.headers.append(HttpHeaders.ETag, eTag)
        call.respond(payload)
    }
}

private fun PipelineContext<Unit, ApplicationCall>.getFilters(): PaginationFilters {
    val perPage = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10
    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
    return PaginationFilters(perPage, page.toLong())
}