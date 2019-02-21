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

    companion object {
        val wazirDirections = listOf(
            Direction(0, -1),
            Direction(-1, 0),
            Direction(0, 1),
            Direction(1, 0)
        )

        val kingDirections = listOf(
            Direction(0, -1),
            Direction(-1, 0),
            Direction(0, 1),
            Direction(1, 0),
            Direction(-1, -1),
            Direction(-1, 1),
            Direction(1, 1),
            Direction(1, -1)
        )
    }
}

fun Character.create() {
    level[position].character = this
    Game.characters += Game.time to this
}

fun Character.die() {
    level[position].character = null
    Game.characters -= Game.characters.first { (_, character) -> character == this }
}

fun Character.isNear(where: Position): Boolean = directions.any { where == position + it }
