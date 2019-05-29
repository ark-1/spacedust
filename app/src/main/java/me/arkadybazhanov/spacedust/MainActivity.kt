package me.arkadybazhanov.spacedust

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View.*
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : Activity(), CoroutineScope {

    private lateinit var job: Job
    private lateinit var gameUpdater: GameUpdater
    override val coroutineContext
        get() = Dispatchers.Default + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = gameView.inventory
            setBackgroundColor(Color.WHITE)
        }

        button.setOnClickListener {
            recyclerView.visibility = if (recyclerView.visibility == GONE) VISIBLE else GONE
        }

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
}
