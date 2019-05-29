package me.arkadybazhanov.spacedust

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import me.arkadybazhanov.spacedust.core.Position

class GameView(context: Context, attributes: AttributeSet) :
    SurfaceView(context, attributes),
    SurfaceHolder.Callback,
    CoroutineScope {

    val inventory = Inventory()
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

    private val gestureDetector by lazy { GestureDetectorCompat(context, gestureListener) }
    private val scaleGestureDetector by lazy { ScaleGestureDetector(context, gestureListener) }
    private val gestureListener = object :
        GestureDetector.OnGestureListener by GestureDetector.SimpleOnGestureListener(),
        ScaleGestureDetector.OnScaleGestureListener by ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            performClick()
            tap(e.x, e.y)
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            camera.scaleFactor *= detector.scaleFactor
            camera.shiftX -= (detector.scaleFactor - 1) / camera.scaleFactor * detector.focusX
            camera.shiftY -= (detector.scaleFactor - 1) / camera.scaleFactor * detector.focusY
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            camera.shiftX -= distanceX / camera.scaleFactor
            camera.shiftY -= distanceY / camera.scaleFactor
            return true
        }
    }

    @SuppressLint("ClickableViewAccessibility") // Actually we ARE calling `performClick()` inside gestureDetector
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return (scaleGestureDetector.onTouchEvent(event) or gestureDetector.onTouchEvent(event)) ||
            super.onTouchEvent(event)
    }
}
