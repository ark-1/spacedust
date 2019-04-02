package me.arkadybazhanov.spacedust.core

interface Character : EventGenerator {
    var level: Level
    var position: Position
    val directions: List<Direction>

    fun canMoveTo(position: Position): Boolean
    fun isVisible(position: Position) : Boolean

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

fun Character.create(delay: Int = 0) {
    level[position].character = this
    Game.characters += Game.time + delay to this
}

fun Character.die() {
    level[position].character = null
    Game.characters -= Game.characters.first { (_, character) -> character == this }
}

fun Character.isNear(where: Position): Boolean = directions.any { where == position + it }
fun Character.near(position: Position): Iterator<Position> = directions.asSequence().map { position + it }.iterator()
fun Character.nearList(position: Position) = directions.map { position + it }
