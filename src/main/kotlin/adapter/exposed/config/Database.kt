package adapter.exposed.config

import adapter.ktor.extensions.toMap
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import java.util.*

fun configureDatabase(config: ApplicationConfig): Database {
    val dbProps = getDbProperties(config)
    val hikari = hikariConfig(dbProps)

    return Database.connect(hikari)
}

private fun hikariConfig(dbProps: Properties): HikariDataSource {
    return HikariDataSource().apply {
        jdbcUrl = dbProps.getProperty("url")
        username = dbProps.getProperty("user")
        driverClassName = dbProps.getProperty("driver")
        password = dbProps.getProperty("password")
        maximumPoolSize = dbProps.getProperty("maximumPoolSize").toInt()
        isAutoCommit = dbProps.getProperty("isAutoCommit").toBoolean() ?: false
        transactionIsolation = dbProps.getProperty("transactionIsolation")
    }
}

private fun getDbProperties(config: ApplicationConfig) =
    Properties().apply {
        putAll(config.toMap("exposed.postgres"))
    }
