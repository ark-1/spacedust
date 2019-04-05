package me.arkadybazhanov.spacedust.core

class Cell(var type: CellType) {
    val events = mutableListOf<Event>()
    var character: Character? = null

    fun isEmpty() = character == null
}

enum class CellType {
    STONE, AIR
}

class Level(private val cells: Array<Array<Cell>>, val id: Int = Game.getNextId()) : Iterable<Cell> {
    init {
        require(cells.isNotEmpty())
    }

    val w = cells.size
    val h = cells[0].size

    override fun iterator(): Iterator<Cell> = iterator {
        for (col in cells) for (cell in col) yield(cell)
    }

    fun withPosition(): Iterable<Pair<Position, Cell>> = iterator {
        for ((x, col) in cells.withIndex()) {
            for ((y, cell) in col.withIndex()) {
                yield(Position(x, y) to cell)
            }
        }
    }.asSequence().asIterable()

    operator fun get(position: Position): Cell = cells[position.x][position.y]
    operator fun get(x: Int, y: Int): Cell = cells[x][y]
}

inline fun <reified T> array2D(w: Int, h: Int, init: (Int, Int) -> T) = Array(w) { x ->
    Array(h) { y ->
        init(x, y)
    }
}
