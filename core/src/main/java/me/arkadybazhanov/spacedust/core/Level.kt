package me.arkadybazhanov.spacedust.core

sealed class Cell {
    val events = mutableListOf<Event>()
    val characters = mutableListOf<Character>()
    class Stone : Cell()
    class Air : Cell()
}

data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Direction): Position {
        return Position(x + other.x, y + other.y)
    }
}

data class Direction(val x: Int, val y: Int)

class Level(val cells: Array<Array<Cell>>) : Iterable<Cell> {
    init {
        require(cells.isNotEmpty())
    }

    val w = cells.size
    val h = cells[0].size

    override fun iterator(): Iterator<Cell> = iterator {
        for (col in cells) for (cell in col) yield(cell)
    }

    fun withCoordinates(): Iterator<Triple<Int, Int, Cell>> = iterator {
        for ((x, col) in cells.withIndex()) {
            for ((y, cell) in col.withIndex()) {
                yield(Triple(x, y, cell))
            }
        }
    }

    operator fun get(position: Position): Cell = cells[position.x][position.y]
}
