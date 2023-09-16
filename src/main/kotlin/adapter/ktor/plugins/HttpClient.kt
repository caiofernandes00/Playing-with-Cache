package adapter.ktor.plugins

import domain.Customer
import domain.Product
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

fun Application.configureHttpClient(): HttpClient {
    return HttpClient(CIO) {
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
            filter { request ->
                request.url.host.contains("ktor.io")
            }
        }
    }
}

suspend fun Application.fetchProducts(client: HttpClient): suspend () -> List<Product> {
    var cacheControlNoCache = false

    return suspend {
        val response: List<Product>
        val dataFromCache: List<Product>? = Cache.get("products")?.let {
            Json.decodeFromString(ListSerializer(Product.serializer()), it)
        }

        if (!cacheControlNoCache && !dataFromCache.isNullOrEmpty()) {
            println("Products: $dataFromCache")
            response = dataFromCache
        } else {
            try {
                val apiResponse = client.get("http://localhost:3000/product") {
                    headers {
                        append("If-None-Match", dataFromCache.toString())
                    }
                }

                val cacheControl = apiResponse.headers["Cache-Control"]
                cacheControlNoCache = cacheControl?.contains("no-cache") == true

                response = when (apiResponse.status.value) {
                    304 -> dataFromCache!!
                    200 -> apiResponse.body<List<Product>>().let {
                        Cache.set("products", it.toString())
                        println("Products: $it")
                        it
                    }

                    else -> throw Exception("Error: ${apiResponse.status}")
                }
            } catch (e: Exception) {
                println("Error: $e")
                throw e
            }
        }

        response
    }
}

suspend fun Application.fetchData(client: HttpClient): suspend () -> List<Customer> {
    var cacheControlNoCache = false
    var cacheControlMaxAge: Int? = null
    var lastTimeFetched: Instant? = null

    return suspend innerFn@{
        val dataFromCache: List<Customer>? = Cache.get("products")?.let {
            Json.decodeFromString(ListSerializer(Customer.serializer()), it)
        }

        if (!cacheControlNoCache && !dataFromCache.isNullOrEmpty()) {
            println("Customers: $dataFromCache")
            return@innerFn dataFromCache
        }

        if (checkIfCacheIsStale(lastTimeFetched, cacheControlMaxAge)) {
            println("Customers: $dataFromCache")
            return@innerFn dataFromCache!!
        }

        try {
            val apiResponse = client.get("http://localhost:3000/product") {
                headers {
                    append("If-None-Match", dataFromCache.toString())
                }
            }

            val cacheControl = apiResponse.headers["Cache-Control"]
            cacheControlNoCache = cacheControl?.contains("no-cache") == true
            val maxAgeMatch = cacheControl?.let { Regex("max-age=(\\d+)").find(it) }
            cacheControlMaxAge = maxAgeMatch?.groups?.get(1)?.value?.toIntOrNull()

            return@innerFn when (apiResponse.status.value) {
                304 -> dataFromCache!!
                200 -> apiResponse.body<List<Customer>>().let {
                    println("Customers: $it")
                    Cache.set("products", it.toString())
                    lastTimeFetched = Clock.System.now()
                    it
                }

                else -> throw Exception("Error: ${apiResponse.status}")
            }
        } catch (e: Exception) {
            println("Error: $e")
            throw e
        }
    }
}
