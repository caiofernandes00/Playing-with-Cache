import adapter.exposed.CustomersRepository
import adapter.exposed.ProductsRepository
import adapter.exposed.config.configureDatabase
import adapter.ktor.plugins.configureHttpClient
import adapter.ktor.plugins.configureHttpServer
import adapter.ktor.plugins.fetchProducts
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch

val databaseConfig = ApplicationConfig("database.conf")
val productsRepository = ProductsRepository()
val customersRepository = CustomersRepository()

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.execute() {
    configureDatabase(databaseConfig)
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

