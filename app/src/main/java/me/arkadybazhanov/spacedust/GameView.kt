package me.arkadybazhanov.spacedust

import android.content.*
import android.graphics.*
import android.util.*
import android.view.*
import kotlinx.coroutines.channels.*
import me.arkadybazhanov.spacedust.core.*

class GameView(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes), SurfaceHolder.Callback {

    private val thread = ViewThread(holder, this)
    private val drawer = LevelDrawer(resources)

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        thread.running = true
        thread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        while (true) catchPrint {
            thread.running = false
            thread.join()
            return
        }
    }

    val playerMoves = Channel<Position>(Channel.UNLIMITED)

    var snapshot: LevelSnapshot? = null
    var scaleFactor by drawer::scaleFactor
    var shiftX by drawer::shiftX
    var shiftY by drawer::shiftY

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        snapshot?.let { drawer.drawLevel(it, canvas) }
    }

    fun tap(x: Float, y: Float) {
        playerMoves.offer(snapshot?.let { drawer.getCell(it, width, x, y) } ?: return)
    }
}
