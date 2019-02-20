package me.arkadybazhanov.spacedust

import me.arkadybazhanov.spacedust.core.*

class Player(override var level: Level, override var position: Position, private val view: GameView) : Character {
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

    override suspend fun getCharacterMove(): PerformableEvent {
        var position: Position
        do {
            position = view.playerMoves.receive()
        } while (!canMoveTo(position) && level[position].character == null)
        if (level[position].character != null) {
            return Attack(this, level[position].character!!, Game.time, 1)
        }
        return Move(this, position, Game.time, 20)
    }
}
