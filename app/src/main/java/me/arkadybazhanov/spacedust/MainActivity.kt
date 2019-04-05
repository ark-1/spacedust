package me.arkadybazhanov.spacedust

import android.app.Activity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.view.*
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.serialization.cbor.Cbor
import me.arkadybazhanov.spacedust.core.Game

class MainActivity : Activity(), CoroutineScope {

    private lateinit var job: Job
    override val coroutineContext
        get() = Dispatchers.Default + job

    private val gestureDetector by lazy { GestureDetectorCompat(this, gestureListener) }
    private val scaleGestureDetector by lazy { ScaleGestureDetector(this, gestureListener) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)

        job = Job()
        Game.reset()

        val game = savedInstanceState.also {
            println(it == null)
        }?.getByteArray(SerializableGame::class.simpleName).also {
            println(it?.size)
        }

        if (game != null) {
            Cbor.load(SerializableGame.serializer(), game).restore(this)
            println("s")
        }

        val player = Game.current as? Player ?: Game.characters.singleOrNull { it.second is Player }?.second as Player?
        launch {
            GameUpdater(gameView, player).run()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        runBlocking { job.cancelAndJoin() }
        outState.putByteArray(
            SerializableGame::class.simpleName,
            Cbor.dump(SerializableGame.serializer(), SerializableGame()).also { println(it.size) }
        )
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
