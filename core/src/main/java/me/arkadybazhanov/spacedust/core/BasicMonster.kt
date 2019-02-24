package me.arkadybazhanov.spacedust.core

class BasicMonster(override var level: Level, override var position: Position,
                   var speed: Int, var maxHp: Int, var strength: Int) : Character {

    override fun canMoveTo(position: Position): Boolean {
        return position.x in (0 until level.w)
                && position.y in (0 until level.h)
                && level[position].type in canStandIn
                && level[position].isEmpty()
    }

    override val id: Int = Game.getNextId()

    override suspend fun getNextEvent(): PerformableEvent {
        for (position in nearList(position).shuffled(Game.random)) {
            val cell = level[position]
            if (!cell.isEmpty()) {
                return Attack(this, cell.character!!, Game.time, speed)
            }
        }

        return Move(this, getNextMoveToTarget(this) {
            it != position && !level[it].isEmpty()
        }, Game.time, speed)
    }

    override val directions = Character.kingDirections

    companion object {
        val canStandIn = listOf(CellType.AIR)
    }
}
