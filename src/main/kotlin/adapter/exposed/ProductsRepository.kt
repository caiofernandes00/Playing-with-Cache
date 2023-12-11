package adapter.exposed

import domain.ProductDomain
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class ProductsRepository() : BaseRepository() {
    object ProductsTable : Table() {
        val id = varchar("id", 36).autoIncrement()
        val name = varchar("name", 255)
        val price = double("age")

        override val primaryKey = PrimaryKey(id, name = "PK_Products_ID")
    }

    override val table: Table
        get() = ProductsTable

    suspend fun create(productDomain: ProductDomain): String = query {
        ProductsTable.insert {
            it[id] = UUID.randomUUID().toString()
            it[name] = productDomain.name
        }[ProductsTable.id]
    }

    suspend fun list(perPage: Int = 10, offset: Long = 1): List<ProductDomain> = query {
        ProductsTable.selectAll().limit(perPage, offset).map {
            ProductDomain(
                id = it[ProductsTable.id].toInt(),
                name = it[ProductsTable.name],
                price = it[ProductsTable.price],
            )
        }
    }

    suspend fun getById(id: String): ProductDomain? = query {
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