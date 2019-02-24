package me.arkadybazhanov.spacedust

import android.animation.*
import android.animation.Animator.AnimatorListener
import android.graphics.Path
import kotlinx.coroutines.*
import me.arkadybazhanov.spacedust.core.*

class Player(override var level: Level, override var position: Position, private val view: GameView) : Character {
    override val directions = Character.kingDirections

    override fun canMoveTo(position: Position): Boolean {
        return position.isValid(level.w, level.h)
            && level[position].type == CellType.AIR
            && level[position].isEmpty()
    }

    override val id: Int get() = -1

    override suspend fun getNextEvent(): PerformableEvent {
        var position: Position
        do {
            position = view.playerMoves.receive()
        } while (!canMoveTo(position) && level[position].isEmpty() || !isNear(position))

        return if (!level[position].isEmpty()) {
            Attack(this, level[position].character!!, Game.time, 10)
        } else {
            val dir = position - this.position

            withContext(Dispatchers.Main) {
                val coordinateUpdater = object {
                    val prevX = view.shiftX
                    val prevY = view.shiftY

                    @Suppress("unused")
                    var x
                        get() = view.shiftX - prevX
                        set(value) { view.shiftX = prevX + value }

                    @Suppress("unused")
                    var y
                        get() = view.shiftY - prevY
                        set(value) { view.shiftY = prevY + value }
                }
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

            Move(this, position, Game.time, 20)
        }
    }
}
