package me.arkadybazhanov.spacedust

import android.content.*
import android.graphics.*
import android.util.*
import android.view.*
import me.arkadybazhanov.spacedust.core.*

class GameView(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes), SurfaceHolder.Callback {

    init {
        holder.addCallback(this)
    }

    private val thread = GameThread(holder, this)
    private val drawer = FieldDrawer(resources)
    private val field = Field(Array(40) { x ->
        Array(40) { y ->
            if (x % (y + 1) == 0) Cell.Stone else Cell.Air
        }
    })

    private val game = Game()

    override fun surfaceCreated(holder: SurfaceHolder?) {
        thread.setRunning(true)
        thread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        while (true) {
            catchPrint {
                thread.setRunning(false)
                thread.join()
                return
            }
        }
    }

    fun update() = game.update()

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawer.drawField(field, canvas)
    }
}
