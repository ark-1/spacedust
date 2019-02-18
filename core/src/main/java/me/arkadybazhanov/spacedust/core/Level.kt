package me.arkadybazhanov.spacedust.core

sealed class Cell {
    val events = mutableListOf<Event>()
    val characters = mutableListOf<Character>()
    object Stone : Cell()
    object Air : Cell()
}

data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Direction): Position {
        return Position(x + other.x, y + other.y)
    }
}

data class Direction(val x: Int, val y: Int)

class Level(val level: Array<Array<Cell>>) : Iterable<Cell> {
    init {
        require(level.isNotEmpty())
    }

    val w = level.size
    val h = level[0].size

    override fun iterator(): Iterator<Cell> = iterator {
        for (col in level) for (cell in col) yield(cell)
    }

    fun withCoordinates(): Iterator<Triple<Int, Int, Cell>> = iterator {
        for ((x, col) in level.withIndex()) {
            for ((y, cell) in col.withIndex()) {
                yield(Triple(x, y, cell))
            }
        }
    }

    operator fun get(position: Position): Cell {
        return level[position.x][position.y]
    }
}
