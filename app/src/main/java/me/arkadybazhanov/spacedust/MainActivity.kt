package me.arkadybazhanov.spacedust

import android.app.*
import android.os.*
import android.support.v4.view.GestureDetectorCompat
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager.LayoutParams.*

class MainActivity : Activity() {

    lateinit var view: GameView
    lateinit var detector: GestureDetectorCompat

    override fun onTouchEvent(event: MotionEvent): Boolean = detector.onTouchEvent(event) || super.onTouchEvent(event)

    override fun onCreate(savedInstanceState: Bundle?) {
        detector = GestureDetectorCompat(this, Listener())
        super.onCreate(savedInstanceState)


        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)
        view = findViewById(R.id.game_view)
    }

    private inner class Listener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean = true

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            view.tap(e.x, e.y)
            return true
        }
    }
}
