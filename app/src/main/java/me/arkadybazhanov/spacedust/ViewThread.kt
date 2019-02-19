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
    val canvas: Canvas = lockCanvas() ?: error("Could not lock canvas")

    try {
        synchronized(this) {
            body(canvas)
        }
    } finally {
        unlockCanvasAndPost(canvas)
    }
}

class ViewThread(private val surfaceHolder: SurfaceHolder, private val gameView: GameView) : Thread() {
    var running: Boolean = false

    override fun run() {
        while (running) {
            val startTime = System.nanoTime()

            surfaceHolder.withCanvas(gameView::draw)

            val timeMillis = (System.nanoTime() - startTime) / 1_000_000
            val waitTime = targetTimeMillis - timeMillis

            sleep(waitTime.coerceAtLeast(0))
        }
    }

    companion object {
        private const val targetFps = 20
        private const val targetTimeMillis = 1000L / targetFps
    }
}
