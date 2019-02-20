package me.arkadybazhanov.spacedust.core

class BasicMonster(override var level: Level, override var position: Position,
                   var speed: Int, var maxHp: Int, var strength: Int) : Character {

    override fun canMoveTo(position: Position): Boolean {
        return position.x in (0 until level.w)
                && position.y in (0 until level.h)
                && level[position].type == CellType.AIR
                && level[position].character == null
    }

    private fun canAttack(character: Character): Boolean {
        for (direction in directions) {
            if (character.position == position + direction) return true
        }
        return false
    }

    override val id: Int = Game.getNextId()

    override suspend fun getCharacterMove(): PerformableEvent {
        for ((position, cell) in level.withPosition()) {
            if (cell.character != null && position != this.position) {
                return if (canAttack(cell.character!!)) {
                    Attack(this, cell.character!!, Game.time, 1)
                } else {
                    Move(this, getNextMoveToTarget(this, cell.character!!), Game.time, speed)
                }
            }
        }
        return Move(this, position, Game.time, speed)
    }

    override val directions = listOf(
        Direction(0, -1),
        Direction(-1, 0),
        Direction(0, 1),
        Direction(1, 0),
        Direction(-1, -1),
        Direction(-1, 1),
        Direction(1, 1),
        Direction(1, -1)
    )
}
