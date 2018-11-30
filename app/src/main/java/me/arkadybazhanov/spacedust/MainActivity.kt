package me.arkadybazhanov.spacedust

import android.app.*
import android.os.*
import android.view.WindowManager.LayoutParams.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)
    }
}
