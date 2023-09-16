import adapter.ktor.plugins.configureHttpClient
import adapter.ktor.plugins.configureHttpServer
import adapter.ktor.plugins.fetchProducts
import io.ktor.server.application.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.execute() {
    configureHttpServer()
    val client = configureHttpClient()

    launch {
        val products = fetchProducts(client)
        products.invoke()
    }
}

