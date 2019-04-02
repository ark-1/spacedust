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

    private lateinit var gestureDetector: GestureDetectorCompat
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private lateinit var view: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gestureDetector = GestureDetectorCompat(this, gestureListener)
        scaleGestureDetector = ScaleGestureDetector(this, gestureListener)

        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)
        view = findViewById(R.id.game_view)!!

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
            view.scaleFactor *= detector.scaleFactor
            view.shiftX -= ((detector.scaleFactor - 1) / view.scaleFactor * detector.focusX).toInt()
            view.shiftY -= ((detector.scaleFactor - 1) / view.scaleFactor * detector.focusY).toInt()
            println(view.scaleFactor)
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            view.shiftX -= distanceX / view.scaleFactor
            view.shiftY -= distanceY / view.scaleFactor
            return true
        }
    }
}
