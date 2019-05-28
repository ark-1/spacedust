package me.arkadybazhanov.spacedust

import android.util.Log
import kotlinx.serialization.Serializable
import java.util.*
import me.arkadybazhanov.spacedust.core.*
import java.lang.Math.*

class Player(
    override var level: Level,
    position: Position,
    private val view: GameView,
    override var hp: Int,
    override var strength: Int,
    val discoveredCells: Cache<Level, Array<BooleanArray>> = Cache { lvl ->
        Array(lvl.h) { BooleanArray(lvl.w) }
    },
    val visibleCells: Cache<Level, Array<BooleanArray>> = Cache { lvl ->
        Array(lvl.h) { BooleanArray(lvl.w) }
    }
) : Character {
    override val refs get() = inventory + (level as Savable)
    override val inventory = mutableListOf<Item>()

    override val saveId: Int get() = PLAYER_SAVE_ID

    override var position = position
        set(value) {
            field = value
            updateVisibility()
        }

    override val directions = Character.kingDirections

    override fun canMoveTo(position: Position): Boolean {
        return position.isValid(level.w, level.h)
                && level[position].type == CellType.AIR
    }

    override fun isVisible(position: Position): Boolean {
        return visibleCells[level][position.x][position.y]
    }

    private val queuedMoves: Queue<Position> = ArrayDeque()

    fun updateVisibility() {
        val calculator = VisibilityCalculator(this, VISIBILITY_RANGE)
        visibleCells[level] = calculator.calculateVisibility()

        for ((position, _) in level.withPosition().filter { (p, _) -> isVisible(p) }) {
            discoveredCells[level][position.x][position.y] = true
        }
    }

    override suspend fun getNextEvent(): Action {
        if (!queuedMoves.isEmpty()) {
            val position = queuedMoves.remove()
            var danger = false
            for (to in nearList(position) + position) {
                if (!to.isValid(level.w, level.h)) {
                    continue
                }
                if (!level[to].isEmpty() && to != this.position) {
                    danger = true
                }
            }
            if (danger) {
                queuedMoves.clear()
            } else {
                view.camera.move(position - this.position)
                return Move(this, position, game.time, MOVE_SPEED)
            }
        }

        var position: Position
        var path: List<Position>?
        do {
            position = view.playerMoves.receive()
            path = getPathToTarget(this) { it == position }
        } while (path == null || !canMoveTo(position) || (!level[position].isEmpty() && !isNear(position)))

        return if (!level[position].isEmpty()) {
            Attack(this, level[position].character!!, game.time, ATTACK_SPEED)
        } else {
            if (path.size == 1) {
                view.camera.move(path[0] - this.position)
                return Move(this, path[0], game.time, MOVE_SPEED)
            }
            queuedMoves += path
            getNextEvent()
        }
    }

    companion object {
        const val VISIBILITY_RANGE = 9
        const val PLAYER_SAVE_ID = -1
        const val STARTING_HP = 100
        const val STARTING_STRENGTH = 20
        const val ATTACK_SPEED = 10
        const val MOVE_SPEED = 20
    }

    override fun toString() = "Player(id=$saveId)"

    @Serializable
    class SavedPlayer(
        val level: Int,
        val position: Position,
        val hp: Int,
        val strength: Int,
        val inventory: List<Int>,
        val discoveredCells: Array<Pair<Int, Array<Array<Boolean>>>>
    ) : SavedStrong<Player> {
        override val saveId: Int get() = PLAYER_SAVE_ID
        override val refs = inventory + level
        override fun initial(pool: Pool): Player = Player(pool.load(level), position,
            (pool as CachePool).view, hp, strength).apply {
            discoveredCells.putAll(this@SavedPlayer.discoveredCells.associate {
                pool.load<Level>(it.first) to it.second.map(Array<Boolean>::toBooleanArray).toTypedArray()
            })
            inventory += this@SavedPlayer.inventory.map { id -> pool.load<Item>(id) }
        }
    }

    override fun save() = SavedPlayer(level.saveId, position, hp, strength,
        inventory.map(Item::saveId),
        discoveredCells.map { (level, cells) ->
            level.saveId to cells.map(BooleanArray::toTypedArray).toTypedArray()
        }.toTypedArray())
}
