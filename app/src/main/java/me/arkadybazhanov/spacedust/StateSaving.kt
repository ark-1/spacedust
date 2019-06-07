package me.arkadybazhanov.spacedust

import android.content.Context
import android.os.Bundle
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.request.*
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.response.*
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion
import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.io.*
import java.util.Properties

private const val kClassesFileName = "kClasses.data"
private const val valuesFileName = "values.data"
private const val nameFileName = "name.data"

private fun Context.writeStringArray(array: Array<String>, fileName: String) {
    JsonConfiguration
    val bytes = array.stringify().toByteArray()
    FileOutputStream(File(filesDir, fileName)).use { it.write(bytes) }
}

private fun Array<String>.stringify() = json.stringify(String.serializer().list, asList())

fun Context.saveToFiles(state: SerializedState, name: String?) {
    writeStringArray(state.kClasses, kClassesFileName)
    writeStringArray(state.values, valuesFileName)
}

private fun Context.readStringArray(fileName: String): Array<String>? {
    val file = RandomAccessFile(File(filesDir, fileName).apply {
        if (!exists()) return null
    }, "r")
    val bytes = ByteArray(file.length().toInt())
    file.readFully(bytes)
    return bytes.toString(Charsets.UTF_8).parseStringArray()
}

private fun String.parseStringArray(): Array<String> =
    json.parse(String.serializer().list, this).toTypedArray()

fun Context.loadFromFiles(): SerializedState? {
    return SerializedState(
        kClasses = readStringArray(kClassesFileName) ?: return null,
        values = readStringArray(valuesFileName) ?: return null,
        name = readStringArray(nameFileName)?.single()
    )
}

fun saveToBundle(state: SerializedState) = Bundle().apply {
    putStringArray(SerializedState::kClasses.name, state.kClasses)
    putStringArray(SerializedState::values.name, state.values)
    putString(SerializedState::name.name, state.name)
}

fun loadFromBundle(bundle: Bundle): SerializedState? {
    val kClasses = bundle.getStringArray(SerializedState::kClasses.name) ?: return null
    val values = bundle.getStringArray(SerializedState::values.name) ?: return null
    val name = bundle.getString(SerializedState::name.name)
    return SerializedState(kClasses = kClasses, values = values, name = name)
}

@Serializable
private data class SerializableSerializedState(val kClasses: String, val values: String, val name: String) {
    constructor(state: SerializedState) : this(
        kClasses = state.kClasses.stringify(),
        values = state.values.stringify(),
        name = state.name.orEmpty()
    )

    fun toSerializableState() = SerializedState(
        kClasses = kClasses.parseStringArray(),
        values = values.parseStringArray(),
        name = name.ifBlank { null }
    )
}

suspend fun saveToServer(state: SerializedState, name: String, client: HttpClient) {
    val content = json.stringify(SerializableSerializedState.serializer(), SerializableSerializedState(state))

    client.call(BuildConfig.serverUrl + "/save") {
        method = HttpMethod.Post
        parameter("name", name)
        println(url.build())
        body = FormDataContent(Parameters.build {
            append("save", content)
        })
    }
}

suspend fun loadFromServer(name: String, client: HttpClient): SerializedState? {
    val content = client.get<String>(BuildConfig.serverUrl + "/get") {
        parameter("name", name)
    }.ifBlank { return null }

    return json.parse(SerializableSerializedState.serializer(), content).toSerializableState()
}
