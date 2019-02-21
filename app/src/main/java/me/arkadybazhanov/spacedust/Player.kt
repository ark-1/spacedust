package me.arkadybazhanov.spacedust

import me.arkadybazhanov.spacedust.core.*

class Player(override var level: Level, override var position: Position, private val view: GameView) : Character {
    override val directions = Character.kingDirections

    override fun canMoveTo(position: Position): Boolean {
        return position.isValid(level.w, level.h)
            && level[position].type == CellType.AIR
            && level[position].character == null
    }

    override val id: Int = Game.getNextId()

    override suspend fun getCharacterMove(): PerformableEvent {
        var position: Position
        do {
            position = view.playerMoves.receive()
        } while (!canMoveTo(position) && level[position].character == null || !isNear(position))

        return if (level[position].character != null) {
            Attack(this, level[position].character!!, Game.time, 10)
        } else {
            Move(this, position, Game.time, 20)
        }
    }
}
