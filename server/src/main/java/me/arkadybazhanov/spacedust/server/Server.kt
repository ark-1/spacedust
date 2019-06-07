package me.arkadybazhanov.spacedust.server

import io.ktor.application.*
import io.ktor.request.receiveParameters
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import java.sql.Connection

object Saves : LongIdTable() {
    val name: Column<String> = varchar(this::name.name, length = 100).uniqueIndex()
    val save: Column<ByteArray> = binary(this::save.name, length = 10_000_000)
}

class Save(id: EntityID<Long>) : LongEntity(id) {
    var name by Saves.name
    var save by Saves.save

    companion object Factory : LongEntityClass<Save>(Saves)
}

private inline fun Save.Factory.updateOrCreate(name: String, crossinline block: Save.() -> Unit) {
    find { Saves.name eq name }.singleOrNull()?.block() ?: new {
        this.name = name
        block()
    }
}

fun main() {
    Database.connect("jdbc:sqlite:saves.sqlite", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED

    transaction {
        SchemaUtils.createMissingTablesAndColumns(Saves)
    }

    val server = embeddedServer(Netty, 8080) {
        routing {
            get("/get") {
//                diagnose()
                transaction { Save.all().forEach { println("${it.name}: ${it.save.toString(Charsets.UTF_8)}") } }

                val name = call.parameters["name"]!!
                call.respondText {
                    transaction {
                        Save.find { Saves.name eq name }.singleOrNull()?.save
                    }?.toString(Charsets.UTF_8) ?: ""
                }
            }

            post("/save") {
                val content = call.receiveParameters()["save"]!!
                transaction {
                    Save.updateOrCreate(call.parameters["name"]!!) {
                        save = content.toByteArray(Charsets.UTF_8)
                    }
                }

                call.respond("ok")
            }
        }
    }
    server.start(wait = true)
}

private fun PipelineContext<Unit, ApplicationCall>.diagnose() {
    println(call.parameters)
    call.request.headers.entries().forEach {
        println("${it.key}: ${it.value}, ")
    }
    println()
    println("hi everybody")
}
