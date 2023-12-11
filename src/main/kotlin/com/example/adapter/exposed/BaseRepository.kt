package com.example.adapter.exposed

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

abstract class BaseRepository() {
    abstract val table: Table

    init {
        transaction {
            SchemaUtils.create(table)
        }
    }
}