package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.Serializable

@Serializable
data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Direction): Position = Position(x + other.x, y + other.y)
    operator fun minus(other: Position): Direction = Direction(x - other.x, y - other.y)

    fun isValid(w: Int, h: Int) = x in (0 until w) && y in (0 until h)
}

@Serializable
data class Direction(val x: Int, val y: Int)
