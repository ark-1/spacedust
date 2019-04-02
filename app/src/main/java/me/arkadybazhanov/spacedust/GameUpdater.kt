package me.arkadybazhanov.spacedust

import android.util.Log
import kotlinx.coroutines.yield
import me.arkadybazhanov.spacedust.core.*

class GameUpdater(private val view: GameView) {

    init {
        Log.d(this::class.simpleName, "${Game::class.simpleName}.${Game::seed.name} is ${Game.seed}")
    }

    private val player = LevelGeneration.generateLevelAndCreate { level, position ->
        Player(level, position, view).also {
            for ((pos, _) in level.withPosition().filter { (p, _) -> it.isVisible(p) }) {
                it.discoveredCells.getValue(level)[pos.x][pos.y] = true
            }
        }
    }.second

    suspend fun run() {
        do {
            view.snapshot = player.level.snapshot(player)
            yield()
        } while (Game.update())
    }
}
