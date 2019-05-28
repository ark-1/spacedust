package me.arkadybazhanov.spacedust.core

interface Character : EventGenerator {
    var level: Level
    var position: Position
    val directions: List<Direction>
    val inventory: List<Item>
    var hp: Int
    var strength: Int

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

val Character.game get() = level.game

fun Character.create(delay: Int = 0) {
    cell.character = this
    game.characters += game.time + delay to this
}

fun Character.die() {
    cell.character = null
    game.characters -= game.characters.first { (_, character) -> character.saveId == saveId }
}

fun Character.isNear(where: Position): Boolean = directions.any { where == position + it }
fun Character.near(position: Position): Iterator<Position> = directions.asSequence().map { position + it }.iterator()
fun Character.nearList(position: Position) = directions.map { position + it }
val Character.cell: Cell get() = level[position]
