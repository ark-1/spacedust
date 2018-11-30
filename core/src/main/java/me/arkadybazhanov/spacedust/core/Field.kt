package me.arkadybazhanov.spacedust.core

sealed class Cell {
    object Stone : Cell()
    object Air : Cell()
}

class Field(val field: Array<Array<Cell>>) {

    init {
        require(field.isNotEmpty())
    }

    val w = field.size
    val h = field[0].size
}
