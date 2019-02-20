package me.arkadybazhanov.spacedust

import me.arkadybazhanov.spacedust.core.*

class GameUpdater(private val view: GameView) {

    private val player = generateLevelAndCreate { level, position ->
        Player(level, position, view)
    }.second

    suspend fun run() {
        while (Game.update()) {
            view.snapshot = player.level.snapshot()
        }
    }
}
