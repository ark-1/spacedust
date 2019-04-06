package me.arkadybazhanov.spacedust.core

class BasicMonster(
    override var level: Level,
    override var position: Position,
    var speed: Int,
    var maxHp: Int,
    var strength: Int,
    override val id: Int = Game.getNextId()
) : Character {

    override fun canMoveTo(position: Position): Boolean {
        return position.x in (0 until level.w)
                && position.y in (0 until level.h)
                && level[position].type in canStandIn
    }

    override fun isVisible(position: Position): Boolean {
        return true
    }

    override suspend fun getNextEvent(): Action {
        for (position in nearList(position).filter { it.isValid(level.w, level.h) }.shuffled(Game.random)) {
            val cell = level[position]
            if (!cell.isEmpty()) {
                return Attack(this, cell.character!!, Game.time, speed)
            }
        }

        val path = getPathToTarget(this) {
            it != position && !level[it].isEmpty()
        } ?: return Move(this, position, Game.time, speed)

        return Move(this, path[0], Game.time, speed)
    }

    override val directions = Character.kingDirections

    companion object {
        val canStandIn = listOf(CellType.AIR)
    }

    override fun toString() = "BasicMonster(id=$id)"
}
