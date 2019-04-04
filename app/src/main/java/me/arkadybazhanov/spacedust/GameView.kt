package me.arkadybazhanov.spacedust

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import me.arkadybazhanov.spacedust.core.Position

class GameView(context: Context, attributes: AttributeSet) :
    SurfaceView(context, attributes),
    SurfaceHolder.Callback,
    CoroutineScope {

    private lateinit var drawer: LevelDrawer

    private lateinit var job: Job
    override val coroutineContext
        get() = Dispatchers.Default + job

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        drawer = LevelDrawer(resources, width)
        job = Job()
        launch {
            ViewUpdater.run(holder, this@GameView)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        job.cancel()
    }

    val playerMoves = Channel<Position>(Channel.UNLIMITED)

    var snapshot: LevelSnapshot? = null
    var scaleFactor
        get() = drawer.scaleFactor
        set(value) { drawer.scaleFactor = value }

    interface Adder {
        operator fun plusAssign(d: Int)
        operator fun minusAssign(d: Int) = plusAssign(-d)
    }

    val shiftX get() = drawer.shiftX
    val shiftY get() = drawer.shiftY

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        snapshot?.let { drawer.drawLevel(it, canvas) }
    }

    fun tap(x: Float, y: Float) {
        playerMoves.offer(snapshot?.let { drawer.getCell(it, x, y) } ?: return)
    }
}
