package me.arkadybazhanov.spacedust

import me.arkadybazhanov.spacedust.core.*
import kotlin.reflect.KClass

class LevelSnapshot private constructor(
    private val cells: Array<Array<CellSnapshot>>) {
    data class CellSnapshot(
        val type: CellType,
        val characterType: KClass<out Character>?,
        val discovered: Boolean,
        val visible: Boolean,
        val itemType: KClass<out Item>?
    )

    val w = cells.size
    val h = cells[0].size

    constructor(level: Level, player: Player) : this(
        Array(level.w) { x ->
            Array(level.h) { y ->
                val cell = level[x, y]
                CellSnapshot(
                    cell.type,
                    cell.character?.let { it::class },
                    player.discoveredCells.getValue(level)[x][y],
                    player.visibleCells.getValue(level)[x][y],
                    cell.items.lastOrNull()?.let { it::class }
                )
            }
        })

    fun withCoordinates() = iterator {
        for ((x, col) in cells.withIndex()) {
            for ((y, cell) in col.withIndex()) {
                yield(Triple(x, y, cell))
            }
        }
    }
}

fun Level.snapshot(player: Player) = LevelSnapshot(this, player)
