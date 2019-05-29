package me.arkadybazhanov.spacedust.server

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import java.sql.Connection

object Saves : LongIdTable() {
    val name: Column<String> = varchar(this::name.name, length = 100)
    val save: Column<ByteArray> = binary(this::save.name, length = 10_000_000)
}

class Save(id: EntityID<Long>) : LongEntity(id) {
    var name by Saves.name
    var save by Saves.save

    companion object : LongEntityClass<Save>(Saves)
}

fun main() {
    Database.connect("jdbc:sqlite:saves.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED

    transaction {
        SchemaUtils.createMissingTablesAndColumns(Saves)
    }

    val server = embeddedServer(Netty, 8080) {
        routing {
            get("/") {
                transaction {
                    Save.new {
                        name = call.parameters["name"]!!
                        save = call.parameters["save"]!!.toByteArray(Charsets.UTF_8)
                    }
                }

                transaction { Save.all().forEach { println("${it.name}: ${it.save}") } }

                call.respondText("Hello, world!", ContentType.Text.Html)
            }
        }
    }
    server.start(wait = true)
}