package me.arkadybazhanov.spacedust.core

class Cell(val type: CellType) {
    val events = mutableListOf<Event>()
    var character: Character? = null
}

enum class CellType {
    STONE, AIR
}

data class Position(val x: Int, val y: Int) {
    operator fun plus(other: Direction): Position = Position(x + other.x, y + other.y)
    operator fun minus(other: Position): Direction = Direction(x - other.x, y - other.y)

    fun isValid(w: Int, h: Int) = x in (0 until w) && y in (0 until h)
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

    fun withPosition(): Iterator<Pair<Position, Cell>> = iterator {
        for ((x, col) in cells.withIndex()) {
            for ((y, cell) in col.withIndex()) {
                yield(Position(x, y) to cell)
            }
        }
    }

    operator fun get(position: Position): Cell = cells[position.x][position.y]
    operator fun get(x: Int, y: Int): Cell = cells[x][y]
}
