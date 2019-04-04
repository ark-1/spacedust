package me.arkadybazhanov.spacedust

import android.animation.*
import android.animation.Animator.AnimatorListener
import android.graphics.Path
import kotlinx.coroutines.*
import java.util.*
import me.arkadybazhanov.spacedust.core.*
import java.lang.Math.*
import java.util.concurrent.ConcurrentHashMap

class Player(override var level: Level, position: Position, private val view: GameView) : Character {

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

    override val id: Int get() = -1

    private fun putLevel(level: Level, it: Array<BooleanArray>) {
        discoveredCells[level] = it
    }

    val discoveredCells: MutableMap<Level, Array<BooleanArray>> = mutableMapOf<Level, Array<BooleanArray>>().withDefault { level ->
        Array(level.h) { BooleanArray(level.w) }.also { putLevel(level, it) }
    }

    private val queuedMoves: Queue<Position> = ArrayDeque()

    private val globalCoordinateUpdater = object {
        private val xDeltas = ConcurrentHashMap<Int, Float>()
        private val yDeltas = ConcurrentHashMap<Int, Float>()

        fun addX(id: Int, deltaX: Float) {
            xDeltas.putIfAbsent(id, 0f)

            view.shiftX -= xDeltas[id]!!
            xDeltas[id] = deltaX
            view.shiftX += xDeltas[id]!!
        }

        fun addY(id: Int, deltaY: Float) {
            yDeltas.putIfAbsent(id, 0f)

            view.shiftY -= yDeltas[id]!!
            yDeltas[id] = deltaY
            view.shiftY += yDeltas[id]!!
        }
    }

    private var updId = 0

    private suspend fun moveCamera(dir: Direction) {


        val coordinateUpdater = object {
            val id = updId++

            @Suppress("unused")
            var x = 0f
                set(value) {
                    field = value
                    globalCoordinateUpdater.addX(id, value)
                }

            @Suppress("unused")
            var y = 0f
                set(value) {
                    field = value
                    globalCoordinateUpdater.addY(id, value)
                }
        }
        withContext(Dispatchers.Main) {
            val animator = ObjectAnimator.ofFloat(
                coordinateUpdater,
                coordinateUpdater::x.name,
                coordinateUpdater::y.name,
                Path().apply {
                    moveTo(0f, 0f)
                    lineTo(-dir.x.cell, -dir.y.cell)
                }
            )

            animator.addListener(object : AnimatorListener {
                // target (coordinateUpdater) in ObjectAnimator is stored as weak reference, so when getNextEvent
                // ends, the animation is cancelled. To overcome this, we store normal reference to
                // coordinateUpdater in a listener, so it lives until animation end.
                @Suppress("unused")
                private var strongReference: Any? = coordinateUpdater

                override fun onAnimationEnd(animation: Animator) {
                    strongReference = null
                }

                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationStart(animation: Animator) {}
            })

            animator.start()
        }
    }

    private fun updateDiscovered() {
        for ((position, _) in level.withPosition().filter { (p, _) -> isVisible(p) }) {
            discoveredCells.getValue(level)[position.x][position.y] = true
        }
    }


    override suspend fun getNextEvent(): PerformableEvent {

        if (!queuedMoves.isEmpty()) {
            val position = queuedMoves.remove()
            var danger = false
            for (to in near(position)) {
                if (!level[to].isEmpty() && to != this.position) {
                    danger = true
                }
            }
            if (danger) {
                queuedMoves.clear()
            } else {
                moveCamera(position - this.position)
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
                moveCamera(path[0] - this.position)

                return Move(this, path[0], Game.time, 20)
            }
            queuedMoves += path
            getNextEvent()
        }
    }

    companion object {
        const val VISIBILITY_RANGE = 3
    }
}
