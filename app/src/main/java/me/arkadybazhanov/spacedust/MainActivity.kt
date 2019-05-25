package me.arkadybazhanov.spacedust

import android.app.Activity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.view.*
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : Activity(), CoroutineScope {

    private lateinit var job: Job
    private lateinit var gameUpdater: GameUpdater
    override val coroutineContext
        get() = Dispatchers.Default + job

    private val gestureDetector by lazy { GestureDetectorCompat(this, gestureListener) }
    private val scaleGestureDetector by lazy { ScaleGestureDetector(this, gestureListener) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)

        savedInstanceState?.let { state = it }
    }

    override fun onResume() {
        super.onResume()

        val savedInstanceState = state

        val player = savedInstanceState?.let {
            val kClasses = savedInstanceState.getStringArray(SerializedState::kClasses.name) ?: return@let null
            val values = savedInstanceState.getStringArray(SerializedState::values.name) ?: return@let null
            SerializedState(kClasses = kClasses, values = values)
        }?.let { it.restorePlayer(gameView) }

        job = Job()
        launch {
            gameUpdater = GameUpdater(gameView, player)
            gameUpdater.run()
        }
    }

    override fun onPause() {
        super.onPause()

        runBlocking { job.cancelAndJoin() }

        val serializedState = serialize(gameUpdater.player)

        state = Bundle().apply {
            putStringArray(SerializedState::kClasses.name, serializedState.kClasses)
            putStringArray(SerializedState::values.name, serializedState.values)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(state)
    }

    private var state: Bundle? = null

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
            gameView.tap(e.x, e.y)
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            gameView.camera.scaleFactor *= detector.scaleFactor
            gameView.camera.shiftX -= (detector.scaleFactor - 1) / gameView.camera.scaleFactor * detector.focusX
            gameView.camera.shiftY -= (detector.scaleFactor - 1) / gameView.camera.scaleFactor * detector.focusY
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            gameView.camera.shiftX -= distanceX / gameView.camera.scaleFactor
            gameView.camera.shiftY -= distanceY / gameView.camera.scaleFactor
            return true
        }
    }
}
