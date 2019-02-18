package me.arkadybazhanov.spacedust.core

sealed class Cell {
    object Stone : Cell()
    object Air : Cell()
}

class Field(val field: Array<Array<Cell>>) : Iterable<Cell> {
    init {
        require(field.isNotEmpty())
    }

    val w = field.size
    val h = field[0].size

    override fun iterator(): Iterator<Cell> = iterator {
        for (col in field) for (cell in col) yield(cell)
    }

    fun withCoordinates(): Iterator<Triple<Int, Int, Cell>> = iterator {
        for ((x, col) in field.withIndex()) {
            for ((y, cell) in col.withIndex()) {
                yield(Triple(x, y, cell))
            }
        }
    }
}
