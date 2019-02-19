package me.arkadybazhanov.spacedust

import kotlinx.coroutines.delay
import me.arkadybazhanov.spacedust.core.*
import kotlin.random.Random

class Player(override var level: Level, override var position: Position) : Character {
    override val directions = listOf(
        Direction(0, -1),
        Direction(-1, 0),
        Direction(0, 1),
        Direction(1, 0)
    )

    override fun canMoveTo(position: Position): Boolean {
        return position.x in (0 until level.w)
                && position.y in (0 until level.h)
                && level[position].type == CellType.AIR
                && level[position].character == null
    }

    override val id: Int = Game.getNextId()

    private val random = Random(0)

    override suspend fun getCharacterMove(): PerformableEvent {

        var pos: Position
        do pos = position + directions[random.nextInt(4)] while (!this.canMoveTo(pos))

        delay(500)

        return Move(this, pos, Game.time, 2)
    }
}