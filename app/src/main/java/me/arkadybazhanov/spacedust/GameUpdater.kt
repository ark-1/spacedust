package me.arkadybazhanov.spacedust

import android.util.Log
import me.arkadybazhanov.spacedust.core.*

class GameUpdater(private val view: GameView) {

    init {
        Log.d(this::class.simpleName, "${Game::class.simpleName}.${Game::seed.name} is ${Game.seed}")
    }

    private val player = LevelGeneration.generateLevelAndCreate { level, position ->
        Player(level, position, view)
    }.second

    suspend fun run() {
        do {
            view.snapshot = player.level.snapshot()
        } while (Game.update())
    }
}
