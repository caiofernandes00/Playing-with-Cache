package com.example.adapter.exposed

import com.example.domain.CustomerDomain
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class CustomersRepository() : BaseRepository() {
    object CustomersTable : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", 255)
        val age = integer("age")

        override val primaryKey = PrimaryKey(id, name = "PK_Customers_ID")
    }

    override val table: Table
        get() = CustomersTable

    suspend fun create(customerDomain: CustomerDomain): Int = query {
        CustomersTable.insert {
            it[id] = customerDomain.id
            it[name] = name
        }[CustomersTable.id]
    }

    suspend fun list(perPage: Int = 10, offset: Long = 1): List<CustomerDomain> = query {
        CustomersTable.selectAll().limit(perPage, offset).map {
            CustomerDomain(
                id = it[CustomersTable.id].toInt(),
                name = it[CustomersTable.name],
                age = it[CustomersTable.age],
            )
        }
    }

    suspend fun getById(id: Int): CustomerDomain? = query {
        CustomersTable.select {
            CustomersTable.id eq id
        }.map {
            CustomerDomain(
                id = it[CustomersTable.id].toInt(),
                name = it[CustomersTable.name],
                age = it[CustomersTable.age],
            )
        }.singleOrNull()
    }
}