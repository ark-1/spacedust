package me.arkadybazhanov.spacedust.core

class BasicMonster(override var level: Level, override var position: Position) : Character {

    override fun canMoveTo(position: Position): Boolean {
        return position.x in (0 until level.w)
                && position.y in (0 until level.h)
                && level[position] is Cell.Air
                && level[position].characters.isEmpty()
    }

    override val id: Int = Game.getNextId()

    override suspend fun getCharacterMove(): PerformableEvent {
        for ((position, cell) in level.withPosition()) {
            if (!cell.characters.isEmpty() && position != this.position) {
                return Move(this, getNextMoveToTarget(this, cell.characters[0]), Game.time, 1)
            }
        }
        return Move(this, position, Game.time, 1)
    }

     override val directions = listOf(
        Direction(0, -1),
        Direction(-1, 0),
        Direction(0, 1),
        Direction(1, 0)
    )


}
