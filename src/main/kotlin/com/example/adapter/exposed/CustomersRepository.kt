package com.example.adapter.exposed

import com.example.domain.CustomerCreationDomain
import com.example.domain.CustomerDomain
import com.example.domain.PaginationFilters
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

    suspend fun create(customerCreationDomain: CustomerCreationDomain): Int = query {
        CustomersTable.insert {
            it[name] = customerCreationDomain.name
            it[age] = customerCreationDomain.age
        }[CustomersTable.id]
    }

    suspend fun list(paginationFilters: PaginationFilters): List<CustomerDomain> = query {
        CustomersTable.selectAll().limit(paginationFilters.perPage, paginationFilters.page).map {
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