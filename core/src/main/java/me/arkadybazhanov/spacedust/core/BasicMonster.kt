package me.arkadybazhanov.spacedust.core

class BasicMonster(override var level: Level, override var position: Position,
                   var speed: Int, var maxHp: Int, var strength: Int) : Character {

    override fun canMoveTo(position: Position): Boolean {
        return position.x in (0 until level.w)
                && position.y in (0 until level.h)
                && level[position].type == CellType.AIR
                && level[position].characters.isEmpty()
    }

    fun canAttack(character: Character): Boolean {
        for (direction in directions) {
            if (character.position == position + direction) return true
        }
        return false
    }

    override val id: Int = Game.getNextId()

    override suspend fun getCharacterMove(): PerformableEvent {
        for ((position, cell) in level.withPosition()) {
            if (!cell.characters.isEmpty() && position != this.position) {
                if (canAttack(cell.characters[0])) {
                    return Attack(this, cell.characters[0], Game.time, 1)
                }
                return Move(this, getNextMoveToTarget(this, cell.characters[0]), Game.time, speed)
            }
        }
        return Move(this, position, Game.time, speed)
    }

    override val directions = listOf(
        Direction(0, -1),
        Direction(-1, 0),
        Direction(0, 1),
        Direction(1, 0)
    )


}
