package me.arkadybazhanov.spacedust

import android.app.*
import android.hardware.Camera
import android.os.*
import android.view.WindowManager.LayoutParams.*

class MainActivity : Activity() {

    private lateinit var view: GameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)
        view = findViewById(R.id.game_view)
    }

    override fun onResume() {
        super.onResume()
        view.camera = Camera.open()
    }
    private var inPreview = false
    override fun onPause() {
        super.onPause()
        if (inPreview) view.camera.stopPreview()
        view.camera.release()
    }
}
