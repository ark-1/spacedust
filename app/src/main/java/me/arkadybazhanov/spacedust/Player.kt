package me.arkadybazhanov.spacedust

import kotlinx.serialization.Serializable
import java.util.*
import me.arkadybazhanov.spacedust.core.*
import android.content.ContextWrapper
import android.app.Activity

class Player(
    level: Level,
    position: Position,
    private val view: GameView,
    override var hp: Int,
    override val maxHp: Int,
    override var strength: Int,
    val discoveredCells: Cache<Level, Array<BooleanArray>> = Cache { lvl ->
        Array(lvl.h) { BooleanArray(lvl.w) }
    },
    val visibleCells: Cache<Level, Array<BooleanArray>> = Cache { lvl ->
        Array(lvl.h) { BooleanArray(lvl.w) }
    }
) : Character {
    override var level = level
        set(value) {
            field = value
            if (view.snapshot != null) {
                view.camera.reset(position.x, position.y)
            }
        }
    override val refs get() = inventory + level as Savable + discoveredCells.keys
    override val inventory = view.inventory.items.apply { clear() }

    override val saveId: Int get() = PLAYER_SAVE_ID

    private var _position = position

    enum class Turn {
        LEFT, RIGHT
    }

    var turn: Turn = Turn.RIGHT

    override var position: Position
        get() = _position
        set(value) {
            val dir = value.minus(_position)
            if (dir.x < 0) {
                turn = Turn.LEFT
            } else if (dir.x > 0) {
                turn = Turn.RIGHT
            }
            _position = value
            updateVisibility()
        }

    override fun move(level: Level, position: Position) {
        _position = position
        this.level = level
        this.position = position
    }

    override val directions = Character.kingDirections

    override fun canMoveTo(position: Position): Boolean {
        return position.isValid(level.w, level.h)
                && level[position].type in canStandIn
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
                if (level[position].type in listOf(CellType.DOWNSTAIRS, CellType.UPSTAIRS)) {
                    queuedMoves.clear()
                }
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

    override fun die() {
        var context = view.context
        while (context is ContextWrapper) {
            if (context is Activity) {
                (context as MainActivity).restart()
            }
            context = context.baseContext
        }
    }

    companion object {
        const val VISIBILITY_RANGE = 9
        const val PLAYER_SAVE_ID = -1
        const val STARTING_HP = 100
        const val STARTING_STRENGTH = 20
        const val ATTACK_SPEED = 10
        const val MOVE_SPEED = 20
        val canStandIn = listOf(CellType.AIR, CellType.DOWNSTAIRS, CellType.UPSTAIRS)
    }

    override fun toString() = "Player(id=$saveId)"

    @Serializable
    class SavedPlayer(
        val level: Int,
        val position: Position,
        val hp: Int,
        val maxHp: Int,
        val strength: Int,
        val inventory: List<Int>,
        val discoveredCells: Array<Pair<Int, Array<Array<Boolean>>>>
    ) : SavedStrong<Player> {
        override val saveId: Int get() = PLAYER_SAVE_ID
        override val refs = inventory + level
        override fun initial(pool: Pool): Player = Player(pool.load(level), position,
            (pool as CachePool).view, hp, maxHp, strength).apply {
            discoveredCells.putAll(this@SavedPlayer.discoveredCells.associate {
                pool.load<Level>(it.first) to it.second.map(Array<Boolean>::toBooleanArray).toTypedArray()
            })
            inventory += this@SavedPlayer.inventory.map { id -> pool.load<Item>(id) }
        }
    }

    override fun save() = SavedPlayer(level.saveId, position, hp, maxHp, strength,
        inventory.map(Item::saveId),
        discoveredCells.map { (level, cells) ->
            level.saveId to cells.map(BooleanArray::toTypedArray).toTypedArray()
        }.toTypedArray())

}
