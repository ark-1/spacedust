package me.arkadybazhanov.spacedust

import android.content.Context
import android.os.Bundle
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.*

private const val kClassesFileName = "kClasses.data"
private const val valuesFileName = "values.data"

private fun Context.writeStringArray(array: Array<String>, fileName: String) {
    val bytes = Json.stringify(String.serializer().list, array.asList()).toByteArray()
    FileOutputStream(File(filesDir, fileName)).use { it.write(bytes) }
}

fun Context.saveToFiles(state: SerializedState) {
    writeStringArray(state.kClasses, kClassesFileName)
    writeStringArray(state.values, valuesFileName)
}

private fun Context.readStringArray(fileName: String): Array<String>? {
    val file = RandomAccessFile(File(filesDir, fileName).apply {
        if (!exists()) return null
    }, "r")
    val bytes = ByteArray(file.length().toInt())
    file.readFully(bytes)
    return Json.parse(String.serializer().list, bytes.toString(Charsets.UTF_8)).toTypedArray()
}

fun Context.loadFromFiles(): SerializedState? {
    return SerializedState(
        kClasses = readStringArray(kClassesFileName) ?: return null,
        values = readStringArray(valuesFileName) ?: return null
    )
}

fun saveToBundle(state: SerializedState) = Bundle().apply {
    putStringArray(SerializedState::kClasses.name, state.kClasses)
    putStringArray(SerializedState::values.name, state.values)
}

fun loadFromBundle(bundle: Bundle): SerializedState? {
    val kClasses = bundle.getStringArray(SerializedState::kClasses.name) ?: return null
    val values = bundle.getStringArray(SerializedState::values.name) ?: return null
    return SerializedState(kClasses = kClasses, values = values)
}
