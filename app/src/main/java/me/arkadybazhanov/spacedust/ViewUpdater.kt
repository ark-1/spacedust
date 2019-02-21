package me.arkadybazhanov.spacedust

import android.graphics.*
import android.view.*
import kotlinx.coroutines.delay

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

object ViewUpdater {
    suspend fun run(surfaceHolder: SurfaceHolder, gameView: GameView) {
        while (true) {
            val startTime = System.nanoTime()

            surfaceHolder.withCanvas(gameView::draw)

            val timeMillis = (System.nanoTime() - startTime) / 1_000_000
            val waitTime = targetTimeMillis - timeMillis

            delay(waitTime)
        }
    }

    private const val targetFps = 20
    private const val targetTimeMillis = 1000L / targetFps
}
