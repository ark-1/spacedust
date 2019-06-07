package me.arkadybazhanov.spacedust

import android.animation.*
import android.animation.Animator.AnimatorListener
import android.graphics.Path
import kotlinx.coroutines.*
import me.arkadybazhanov.spacedust.core.Direction
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Camera(private val width: Int, private val height: Int, playerX: Int, playerY: Int) {

    companion object {
        private const val WIDTH_IN_CELLS = 10
    }

    var scaleFactor = width / WIDTH_IN_CELLS.cell

    var shiftX = AtomicFloat((width / 2f) / scaleFactor - (playerX.cell + 1.cell / 2))
    var shiftY = AtomicFloat((height / 2f) / scaleFactor - (playerY.cell + 1.cell / 2))

    private var updId = 0

    private val xDeltas = ConcurrentHashMap<Int, Float>()
    private val yDeltas = ConcurrentHashMap<Int, Float>()
    private val animators = Collections.newSetFromMap(ConcurrentHashMap<Animator, Boolean>() as MutableMap<Animator, Boolean>)

    private fun addX(id: Int, deltaX: Float) {
        xDeltas.putIfAbsent(id, 0f)

        shiftX -= xDeltas[id]!!
        xDeltas[id] = deltaX
        shiftX += xDeltas[id]!!
    }

    private fun addY(id: Int, deltaY: Float) {
        yDeltas.putIfAbsent(id, 0f)

        shiftY -= yDeltas[id]!!
        yDeltas[id] = deltaY
        shiftY += yDeltas[id]!!
    }

    suspend fun move(dir: Direction) {
        val coordinateUpdater = object {
            val id = updId++

            @Suppress("unused")
            var x = 0f
                set(value) {
                    field = value
                    addX(id, value)
                }

            @Suppress("unused")
            var y = 0f
                set(value) {
                    field = value
                    addY(id, value)
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
                    animators.remove(animator)
                }

                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationStart(animation: Animator) {}
            })

            animator.start()
            animators.add(animator)
        }
    }

    fun reset(playerX: Int, playerY: Int) {
        animators.forEach {
            it.removeAllListeners()
            GlobalScope.launch(Dispatchers.Main) { it.cancel() }
        }
        shiftX = AtomicFloat((width / 2f) / scaleFactor - (playerX.cell + 1.cell / 2))
        shiftY = AtomicFloat((height / 2f) / scaleFactor - (playerY.cell + 1.cell / 2))
    }
}