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
    private val drawer = LevelDrawer(resources)

    private val player = generateLevelAndCreate(::Player).second

    init {
        Game.characters += 0 to player
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        thread.running = true
        thread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        while (true) {
            catchPrint {
                thread.running = false
                thread.join()
                return
            }
        }
    }

    suspend fun update() = Game.update()

    var resume: ((Position) -> Unit)? = null

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawer.drawLevel(player.level, canvas)
    }

    fun tap(x: Float, y: Float) {
        println("$x $y !!")
        resume?.invoke(drawer.getCell(player.level, width, x, y))
        resume = null
    }
}
