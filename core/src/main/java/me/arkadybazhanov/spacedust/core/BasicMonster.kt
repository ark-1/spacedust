package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.Serializable

class BasicMonster(
    override var level: Level,
    override var position: Position,
    var speed: Int,
    override var hp: Int,
    override var strength: Int,
    override val saveId: Int = Game.getNextId()
) : Character {
    override val refs get() = inventory + (level as Savable)
    override val inventory = mutableListOf<Item>()

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
                return Attack(this, cell.character!!, game.time, speed)
            }
        }

        val path = getPathToTarget(this) {
            it != position && !level[it].isEmpty()
        } ?: return Move(this, position, game.time, speed)

        return Move(this, path[0], game.time, speed)
    }

    override val directions = Character.kingDirections

    companion object {
        val canStandIn = listOf(CellType.AIR)
    }

    override fun toString() = "BasicMonster(id=$saveId)"

    override fun save() = SavedBasicMonster(
        saveId,
        level.saveId,
        position,
        speed,
        hp,
        strength,
        inventory.map(Item::saveId)
    )

    override fun die() {
        super.die()
        cell.items += Weapon(5)
        cell.items += inventory
    }

    @Serializable
    data class SavedBasicMonster(
        override val saveId: Int,
        val level: Int,
        val position: Position,
        val speed: Int,
        val maxHp: Int,
        val strength: Int,
        val inventory: List<Int>
    ) : SavedStrong<BasicMonster> {
        override val refs = listOf(level)

        override fun initial(pool: Pool): BasicMonster =
            BasicMonster(pool.load(level), position, speed, maxHp, strength, saveId).apply {
                inventory += this@SavedBasicMonster.inventory.map {id -> pool.load<Item>(id)}
            }
    }
}
