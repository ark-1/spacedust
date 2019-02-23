package me.arkadybazhanov.spacedust.core

import me.arkadybazhanov.spacedust.core.CellType.AIR

class MonsterSpawner(
    private val level: Level,
    private val duration: Int,
    private val delay: Int,
    private val spawn: (Position) -> Character
) : EventGenerator {
    override val id: Int = Game.getNextId()

    override tailrec suspend fun getNextEvent(): Spawn {
        for ((pos, cell) in level.withPosition().shuffled(Game.random)) {
            if (cell.type == AIR && cell.isEmpty()) {
                return Spawn(Game.time, duration, delay) { spawn(pos) }
            }
        }

        return getNextEvent()
    }
}