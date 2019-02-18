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
    private val player = run {
        val (level, position) = generateLevel()
        Player(level, position)
    }

    init {
        Game.characters += 0 to player
    }

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

    suspend fun update() = Game.update()

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawer.drawLevel(player.level, canvas)
    }
}
