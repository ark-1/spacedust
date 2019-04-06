package me.arkadybazhanov.spacedust

import java.util.*
import me.arkadybazhanov.spacedust.core.*
import java.lang.Math.*

class Player(
    override var level: Level,
    position: Position,
    val view: GameView,
    override val id: Int = -1
) : Character {

    override var position = position
        set(value) {
            field = value
            updateDiscovered()
        }

    override val directions = Character.kingDirections

    override fun canMoveTo(position: Position): Boolean {
        return position.isValid(level.w, level.h)
                && level[position].type == CellType.AIR
    }

    override fun isVisible(position: Position): Boolean {
        return max(abs(position.x - this.position.x), abs(position.y - this.position.y)) <= VISIBILITY_RANGE
    }

    val discoveredCells = Cache<Level, Array<BooleanArray>> { level ->
        Array(level.h) { BooleanArray(level.w) }
    }

    private val queuedMoves: Queue<Position> = ArrayDeque()

    fun updateDiscovered() {
        for ((position, _) in level.withPosition().filter { (p, _) -> isVisible(p) }) {
            discoveredCells[level][position.x][position.y] = true
        }
    }

    override suspend fun getNextEvent(): Action {

        if (!queuedMoves.isEmpty()) {
            val position = queuedMoves.remove()
            var danger = false
            for (to in near(position)) {
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
                return Move(this, position, Game.time, 20)
            }
        }

        var position: Position
        var path: List<Position>?
        do {
            position = view.playerMoves.receive()
            path = getPathToTarget(this) { it == position }
        } while (path == null || !canMoveTo(position) || (!level[position].isEmpty() && !isNear(position)))

        return if (!level[position].isEmpty()) {
            Attack(this, level[position].character!!, Game.time, 10)
        } else {
            if (path.size == 1) {
                view.camera.move(path[0] - this.position)

                return Move(this, path[0], Game.time, 20)
            }
            queuedMoves += path
            getNextEvent()
        }
    }

    companion object {
        const val VISIBILITY_RANGE = 3
    }

    override fun toString() = "Player(id=$id)"
}
