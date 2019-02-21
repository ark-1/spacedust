package me.arkadybazhanov.spacedust.core

class BasicMonster(override var level: Level, override var position: Position,
                   var speed: Int, var maxHp: Int, var strength: Int) : Character {

    override fun canMoveTo(position: Position): Boolean {
        return position.x in (0 until level.w)
                && position.y in (0 until level.h)
                && level[position].type == CellType.AIR
                && level[position].character == null
    }

    override val id: Int = Game.getNextId()

    override suspend fun getCharacterMove(): PerformableEvent {
        for ((position, cell) in level.withPosition()) {
            if (cell.character != null && position != this.position) {
                return if (isNear(cell.character!!.position)) {
                    Attack(this, cell.character!!, Game.time, speed)
                } else {
                    Move(this, getNextMoveToTarget(this, cell.character!!), Game.time, speed)
                }
            }
        }
        return Move(this, position, Game.time, speed)
    }

    override val directions = Character.kingDirections
}
