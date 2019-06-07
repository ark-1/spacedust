package me.arkadybazhanov.spacedust

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View.*
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : Activity(), CoroutineScope {

    private lateinit var job: Job
    private lateinit var gameUpdater: GameUpdater
    override val coroutineContext
        get() = Dispatchers.Default + job

    private var username
        get() = editText.text.ifBlank { null }?.toString()
        set(value) = editText.setText(value.orEmpty())

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
            inventoryView.visibility = when (inventoryView.visibility) {
                GONE -> VISIBLE
                else -> GONE
            }
        }

        editText.setOnTouchListener { _, _ ->
            editText.isFocusableInTouchMode = true
            false
        }

        downloadButton.setOnClickListener {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    downloadButton.isEnabled = false
                    uploadButton.isEnabled = false
                }
                job.cancelAndJoin()
                job = Job()
                gameUpdater = GameUpdater(gameView, username?.let { loadFromServer(it, client) }?.restorePlayer(gameView))
                gameView.camera.reset(gameUpdater.player.position.x, gameUpdater.player.position.y)
                withContext(Dispatchers.Main) {
                    downloadButton.isEnabled = true
                    uploadButton.isEnabled = true
                }

                this@MainActivity.launch {
                    gameUpdater.run()
                }
            }
        }

        uploadButton.setOnClickListener {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    downloadButton.isEnabled = false
                    uploadButton.isEnabled = false
                }
                job.cancelAndJoin()
                job = Job()
                username?.let {
                    saveToServer(serialize(gameUpdater.player, it), it, client)
                }
                withContext(Dispatchers.Main) {
                    downloadButton.isEnabled = true
                    uploadButton.isEnabled = true
                }

                this@MainActivity.launch {
                    gameUpdater.run()
                }
            }
        }

        //savedInstanceState?.let { state = it } ?: loadFromFiles()?.let { state = saveToBundle(it) }
    }

    override fun onResume() {
        super.onResume()

        val player = state?.let { loadFromBundle(it) }?.run {
            username = name
            restorePlayer(gameView)
        }

        job = Job()
        launch {
            gameUpdater = GameUpdater(gameView, player)
            gameUpdater.run()
        }
    }

    override fun onPause() {
        super.onPause()

        runBlocking { job.cancelAndJoin() }

        state = saveToBundle(serialize(gameUpdater.player, username))
    }

    private val client = HttpClient(Android)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putAll(state)
        val serializedState = serialize(gameUpdater.player, username)
        saveToFiles(serializedState, username)
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
