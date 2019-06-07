package me.arkadybazhanov.spacedust

import me.arkadybazhanov.spacedust.core.*
import me.arkadybazhanov.spacedust.core.Monster.MonsterType
import kotlin.reflect.KClass

class LevelSnapshot private constructor(
    private val cells: Array<Array<CellSnapshot>>,
    val playerSnapshot: PlayerSnapshot
) {

    data class CellSnapshot(
        val type: CellType,
        val characterType: KClass<out Character>?,
        val monsterType: MonsterType?,
        val discovered: Boolean,
        val visible: Boolean,
        val itemType: KClass<out Item>?,
        val weaponType: WeaponType?
    )

    val w = cells.size
    val h = cells[0].size

    data class PlayerSnapshot(
        val hp: Int,
        val maxHp: Int,
        val x: Int,
        val y: Int,
        val turn: Player.Turn
    )

    constructor(level: Level, player: Player) : this(
        Array(level.w) { x ->
            Array(level.h) { y ->
                val cell = level[x, y]
                CellSnapshot(
                    cell.type,
                    cell.character?.let { it::class },
                    (cell.character as? Monster)?.type,
                    player.discoveredCells.getValue(level)[x][y],
                    player.visibleCells.getValue(level)[x][y],
                    cell.items.lastOrNull()?.let { it::class },
                    (cell.items.lastOrNull() as? Weapon)?.type
                )
            }
        }, PlayerSnapshot(
            player.hp,
            Player.STARTING_HP,
            player.position.x,
            player.position.y,
            player.turn
        ))

    fun withCoordinates() = iterator {
        for ((x, col) in cells.withIndex()) {
            for ((y, cell) in col.withIndex()) {
                yield(Triple(x, y, cell))
            }
        }
    }
}

fun Level.snapshot(player: Player) = LevelSnapshot(this, player)
