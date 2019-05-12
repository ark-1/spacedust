package me.arkadybazhanov.spacedust

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import me.arkadybazhanov.spacedust.core.*

class GameView(context: Context, attributes: AttributeSet) :
    SurfaceView(context, attributes),
    SurfaceHolder.Callback,
    CoroutineScope,
    Savable {

    override val saveId: Int get() = -2

    override fun save(): Nothing = error("Should be restored explicitly")

    private val drawer: LevelDrawer by lazy { LevelDrawer(resources) }

    private lateinit var job: Job
    override val coroutineContext
        get() = Dispatchers.Default + job

    init {
        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        job = Job()

        launch {
            ViewUpdater.run(holder, this@GameView)
        }
    }

    val camera by lazy { Camera(width) }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        job.cancel()
    }

    val playerMoves = Channel<Position>(Channel.UNLIMITED)
    var snapshot: LevelSnapshot? = null

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        snapshot?.let { drawer.drawLevel(it, canvas, camera) }
    }

    fun tap(x: Float, y: Float) {
        playerMoves.offer(snapshot?.let { drawer.getCell(it, camera, x, y) } ?: return)
    }
}
