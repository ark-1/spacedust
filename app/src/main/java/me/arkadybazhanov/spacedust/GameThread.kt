package me.arkadybazhanov.spacedust

import android.graphics.*
import android.view.*

inline fun catchPrint(block: () -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

inline fun SurfaceHolder.withCanvas(body: (Canvas) -> Unit) {
    var canvas: Canvas? = null

    catchPrint {
        val c = lockCanvas() ?: error("Could not lock canvas")
        canvas = c
        synchronized(this) {
            body(c)
        }
    }

    if (canvas != null) {
        catchPrint {
            unlockCanvasAndPost(canvas)
        }
    }
}

class GameThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {
    private var running: Boolean = false

    fun setRunning(isRunning: Boolean) {
        running = isRunning
    }

    override fun run() {
        while (running) {
            val startTime = System.nanoTime()

            surfaceHolder.withCanvas { canvas ->
                gameView.update()
                gameView.draw(canvas)
            }

            val timeMillis = (System.nanoTime() - startTime) / 1_000_000
            val waitTime = targetTimeMillis - timeMillis

            catchPrint {
                sleep(waitTime.coerceAtLeast(0))
            }
        }
    }

    companion object {
        private const val targetFps = 20
        private const val targetTimeMillis = 1000L / targetFps
    }
}
