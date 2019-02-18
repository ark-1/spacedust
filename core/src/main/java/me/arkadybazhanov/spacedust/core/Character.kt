package me.arkadybazhanov.spacedust.core

interface Character : Comparable<Character> {
    val id: Int

    suspend fun getCharacterMove(): CharacterMove

    override fun compareTo(other: Character): Int {
        return id.compareTo(other.id)
    }
}

interface CharacterMove {
    val character: Character

    fun perform(): Int
}
