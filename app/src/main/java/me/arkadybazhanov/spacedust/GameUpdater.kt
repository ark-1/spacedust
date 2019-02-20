package me.arkadybazhanov.spacedust

import me.arkadybazhanov.spacedust.core.Game
import me.arkadybazhanov.spacedust.core.generateLevelAndCreate

class GameUpdater(private val view: GameView) {

    private val player = generateLevelAndCreate { level, position ->
        Player(level, position, view)
    }.second

    init {
        Game.characters += 0 to player
    }

    suspend fun run() {
        while (Game.update()) {
            view.snapshot = player.level.snapshot()
        }
    }
}
