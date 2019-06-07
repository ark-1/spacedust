package me.arkadybazhanov.spacedust

import kotlinx.coroutines.yield
import me.arkadybazhanov.spacedust.core.*

class GameUpdater(private val view: GameView, player: Player? = null) {

    private val game = player?.game ?: Game()

    val player = player?.apply(Player::updateVisibility)
        ?: LevelGeneration.generateLevelAndCreate(game) { level, position ->
            Player(level, position, view, Player.STARTING_HP, Player.STARTING_HP, Player.STARTING_STRENGTH).apply(Player::updateVisibility)
        }.second

    suspend fun run() {
        do {
            view.snapshot = player.level.snapshot(player)
            yield()
        } while (game.update())
    }
}
