package com.example.adapter.ktor.plugins

import com.example.adapter.exposed.CustomersRepository
import com.example.adapter.exposed.ProductsRepository
import com.example.domain.CustomerCreationDomain
import com.example.domain.PaginationFilters
import com.example.domain.ProductCreationDomain
import com.example.domain.generateETag
import io.github.crackthecodeabhi.kreds.connection.KredsClient
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class HttpServerConfig(
    val productsRepository: ProductsRepository,
    val customersRepository: CustomersRepository,
    val cacheClient: KredsClient,
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
            val filters = getFilters()
            val key = "products-${filters.page}-${filters.perPage}"
            val products = useFromCacheIfExists(httpServerConfig, key) {
                httpServerConfig.productsRepository.list(getFilters())
            }

            if (httpServerConfig.useETags) {
                useEtags(products)
            } else {
                respondAndSetCache(httpServerConfig, key, products)
            }
        }

        get("/products/{id}") {
            call.response.headers.append(HttpHeaders.CacheControl, "must-revalidate,max-age=60,public")
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val product = useFromCacheIfExists(httpServerConfig, "product-$id") {
                    httpServerConfig.productsRepository.getById(id)
                }

                prepareGetByIdResponse(httpServerConfig, product)
            }
        }

        post("/products") {
            val product = call.receive<ProductCreationDomain>()
            httpServerConfig.productsRepository.create(product)
            call.respond(HttpStatusCode.Created)
        }

        get("/customers") {
            call.response.headers.append(HttpHeaders.CacheControl, "must-revalidate,max-age=60,public")
            val filters = getFilters()
            val key = "customers-${filters.page}-${filters.perPage}"
            val customers = useFromCacheIfExists(httpServerConfig, key) {
                httpServerConfig.customersRepository.list(getFilters())
            }

            if (httpServerConfig.useETags) {
                useEtags(customers)
            } else {
                respondAndSetCache(httpServerConfig, key, customers)
            }
        }

        get("/customers/{id}") {
            call.response.headers.append(HttpHeaders.CacheControl, "must-revalidate,max-age=60,public")
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val customer = useFromCacheIfExists(httpServerConfig, "customer-$id") {
                    httpServerConfig.productsRepository.getById(id)
                }

                prepareGetByIdResponse(httpServerConfig, customer)
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
    httpServerConfig: HttpServerConfig,
    payload: Any?
) {
    if (payload == null) {
        call.respond(HttpStatusCode.NotFound)
    } else {
        respondAndSetCache(httpServerConfig, "products", payload)
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

private suspend fun <T> PipelineContext<Unit, ApplicationCall>.useFromCacheIfExists(
    httpServerConfig: HttpServerConfig,
    key: String,
    fn: suspend () -> T
): T =
    if (httpServerConfig.useCache) httpServerConfig.cacheClient.get("products") as T ?: fn() else fn()


private suspend inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.respondAndSetCache(
    httpServerConfig: HttpServerConfig,
    key: String,
    value: T,
) {
    httpServerConfig.cacheClient.set(key, json.encodeToString(value))
    call.respond(value)
}

private fun PipelineContext<Unit, ApplicationCall>.getFilters(): PaginationFilters {
    val perPage = call.request.queryParameters["perPage"]?.toIntOrNull() ?: 10
    val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0
    return PaginationFilters(perPage, page.toLong())
}

private val json = Json {
    ignoreUnknownKeys = true
}
