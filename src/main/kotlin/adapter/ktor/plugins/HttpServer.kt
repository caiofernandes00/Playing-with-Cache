package adapter.ktor.plugins

import domain.Customer
import domain.Product
import domain.generateETag
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureHttpServer() {
    install(ContentNegotiation) {
        json()
    }
    routing {
        get("/products") {
            call.response.headers.append(HttpHeaders.CacheControl, "no-cache")
            val products = listOf(
                Product(1, "Product 1", 100.0),
                Product(2, "Product 2", 200.0),
                Product(3, "Product 3", 300.0),
            )

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
            val products = listOf(
                Customer(1, "Customer 1", 10),
                Customer(2, "Customer 2", 20),
                Customer(3, "Customer 3", 30),
            )

            val eTag = generateETag(products)
            val requestETag = call.request.header(HttpHeaders.IfNoneMatch)
            if (requestETag == eTag) {
                call.respond(HttpStatusCode.NotModified)
            } else {
                call.response.headers.append(HttpHeaders.ETag, eTag)
                call.respond(products)
            }
        }
    }
}
