package com.example.adapter.exposed

import com.example.domain.PaginationFilters
import com.example.domain.ProductCreationDomain
import com.example.domain.ProductDomain
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class ProductsRepository() : BaseRepository() {
    object ProductsTable : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", 255)
        val price = double("price")

        override val primaryKey = PrimaryKey(id, name = "PK_Products_ID")
    }

    override val table: Table
        get() = ProductsTable

    suspend fun create(productCreationDomain: ProductCreationDomain): Int = query {
        ProductsTable.insert {
            it[name] = productCreationDomain.name
            it[price] = productCreationDomain.price
        }[ProductsTable.id]
    }

    suspend fun list(paginationFilters: PaginationFilters): List<ProductDomain> = query {
        ProductsTable.selectAll().limit(paginationFilters.perPage, paginationFilters.page).map {
            ProductDomain(
                id = it[ProductsTable.id].toInt(),
                name = it[ProductsTable.name],
                price = it[ProductsTable.price],
            )
        }
    }

    suspend fun getById(id: Int): ProductDomain? = query {
        ProductsTable.select {
            ProductsTable.id eq id
        }.map {
            ProductDomain(
                id = it[ProductsTable.id].toInt(),
                name = it[ProductsTable.name],
                price = it[ProductsTable.price]
            )
        }.singleOrNull()
    }
}