package me.arkadybazhanov.spacedust

import android.util.Log
import kotlinx.coroutines.yield
import me.arkadybazhanov.spacedust.core.*

class GameUpdater(private val view: GameView, player: Player? = null) {

    init {
        Log.d(this::class.simpleName, "${Game::class.simpleName}.${Game::seed.name} is ${Game.seed}")
    }

    private val player = player?.apply(Player::updateDiscovered).also {
        println("q ${it == null}")
    } ?: LevelGeneration.generateSmallRoomAndCreate { level, position ->
        Player(level, position, view).apply(Player::updateDiscovered)
    }.second

    suspend fun run() {
        do {
            view.snapshot = player.level.snapshot(player)
            yield()
        } while (Game.update())
    }
}
