package com.example.adapter.exposed.config

import com.example.adapter.ktor.extensions.toMap
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import java.util.*

fun configureDatabase(config: ApplicationConfig, dbPool: Boolean = true): Database {
    val dbProps = getDbProperties(config)
    val hikari = hikariConfig(dbProps)

    return if (dbPool) {
        Database.connect(hikari)
    } else {
        databaseConfigNoPool(dbProps)
    }
}

private fun hikariConfig(dbProps: Properties): HikariDataSource {
    return HikariDataSource().apply {
        jdbcUrl = dbProps.getProperty("url")
        username = dbProps.getProperty("user")
        driverClassName = dbProps.getProperty("driver")
        password = dbProps.getProperty("password")
        maximumPoolSize = dbProps.getProperty("maximumPoolSize").toInt()
        isAutoCommit = dbProps.getProperty("isAutoCommit").toBoolean()
        transactionIsolation = dbProps.getProperty("transactionIsolation")
    }
}

private fun databaseConfigNoPool(dbProps: Properties): Database {
    return Database.connect(
        url = dbProps.getProperty("url"),
        driver = dbProps.getProperty("driver"),
        user = dbProps.getProperty("user"),
        password = dbProps.getProperty("password")
    )
}

private fun getDbProperties(config: ApplicationConfig) =
    Properties().apply {
        putAll(config.toMap("exposed.postgres"))
    }
