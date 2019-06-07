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

        //state = loadFromFiles()?.let { saveToBundle(it) }
        state = null

        window.setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main)

        inventoryView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = gameView.inventory
            setBackgroundColor(Color.WHITE)
        }

        inventoryButton.setOnClickListener {
            inventoryView.visibility = if (inventoryView.visibility == GONE) VISIBLE else GONE
        }

        //savedInstanceState?.let { state = it } ?: loadFromFiles()?.let { state = saveToBundle(it) }
    }

    override fun onResume() {
        super.onResume()

        val player = state?.let { loadFromBundle(it) }?.restorePlayer(gameView)

        job = Job()
        launch {
            gameUpdater = GameUpdater(gameView, player)
            gameUpdater.run()
        }
    }

    override fun onPause() {
        super.onPause()

        runBlocking { job.cancelAndJoin() }

        state = saveToBundle(serialize(gameUpdater.player))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(state)
        saveToFiles(serialize(gameUpdater.player))
    }

    private var state: Bundle? = null

    override fun onDestroy() {
        super.onDestroy()
        runBlocking { job.cancelAndJoin() }
    }

    fun restart() {
        GlobalScope.launch {
            job.cancelAndJoin()
            job = Job()
            this@MainActivity.launch {
                gameUpdater = GameUpdater(gameView, null)
                gameView.camera.reset(gameUpdater.player.position.x, gameUpdater.player.position.y)
                gameUpdater.run()
            }
        }
    }
}
