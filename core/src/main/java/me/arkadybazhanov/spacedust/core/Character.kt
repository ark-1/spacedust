package me.arkadybazhanov.spacedust.core

interface Character : Comparable<Character> {
    val id: Int
    var level: Level
    var position: Position
    val directions: List<Direction>

    fun canMoveTo(position: Position): Boolean

    suspend fun getCharacterMove(): PerformableEvent

    override fun compareTo(other: Character): Int {
        return id.compareTo(other.id)
    }
}