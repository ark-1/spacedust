package me.arkadybazhanov.spacedust

import android.app.*
import android.os.*
import android.support.v4.view.GestureDetectorCompat
import android.view.*
import android.view.WindowManager.LayoutParams.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

class MainActivity : Activity(), CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    lateinit var view: GameView
    private lateinit var detector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        detector = GestureDetectorCompat(this, Listener())

        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)
        view = findViewById(R.id.game_view)
        job = Job()

        launch {
            GameUpdater(view).run()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = detector.onTouchEvent(event) || super.onTouchEvent(event)

    private inner class Listener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            view.tap(e.x, e.y)
            return true
        }
    }
}
