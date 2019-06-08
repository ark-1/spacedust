package me.arkadybazhanov.spacedust

import android.graphics.Canvas
import android.util.Log
import android.view.SurfaceHolder
import kotlinx.coroutines.*

object ViewUpdater {

    private inline fun SurfaceHolder.withCanvas(body: (Canvas) -> Unit) {
        val canvas = lockCanvas() ?: run {
            Log.d("${ViewUpdater::class.simpleName}", "Could not lock canvas")
            return
        }

        try {
            body(canvas)
        } finally {
            unlockCanvasAndPost(canvas)
        }
    }

    var healthSurfaceHolder: SurfaceHolder? = null
    var healthView: HealthView? = null

    suspend fun run(surfaceHolder: SurfaceHolder, gameView: GameView) {
        while (true) {
            val startTime = System.nanoTime()

            surfaceHolder.withCanvas(gameView::draw)
            healthSurfaceHolder?.withCanvas(healthView!!::draw)

            val timeMillis = (System.nanoTime() - startTime) / 1_000_000
            val waitTime = targetTimeMillis - timeMillis

            delay(waitTime)
            yield()
        }
    }

    private const val targetFps = 50
    private const val targetTimeMillis = 1000L / targetFps
}
