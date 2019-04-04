package me.arkadybazhanov.spacedust

import android.app.Activity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.view.*
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import kotlinx.coroutines.*
import me.arkadybazhanov.spacedust.core.Game

class MainActivity : Activity(), CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext
        get() = Dispatchers.Default + job

    private val gestureDetector by lazy { GestureDetectorCompat(this, gestureListener) }
    private val scaleGestureDetector by lazy { ScaleGestureDetector(this, gestureListener) }

    private val view get() = findViewById<GameView>(R.id.game_view)!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)

        job = Job()
        Game.reset()
        launch {
            GameUpdater(view).run()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return (scaleGestureDetector.onTouchEvent(event) or
            gestureDetector.onTouchEvent(event)) ||
            super.onTouchEvent(event)
    }

    private val gestureListener = object :
        GestureDetector.OnGestureListener by GestureDetector.SimpleOnGestureListener(),
        ScaleGestureDetector.OnScaleGestureListener by ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            view.tap(e.x, e.y)
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            view.camera.scaleFactor *= detector.scaleFactor
            view.camera.shiftX -= (detector.scaleFactor - 1) / view.camera.scaleFactor * detector.focusX
            view.camera.shiftY -= (detector.scaleFactor - 1) / view.camera.scaleFactor * detector.focusY
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            view.camera.shiftX -= distanceX / view.camera.scaleFactor
            view.camera.shiftY -= distanceY / view.camera.scaleFactor
            return true
        }
    }
}
