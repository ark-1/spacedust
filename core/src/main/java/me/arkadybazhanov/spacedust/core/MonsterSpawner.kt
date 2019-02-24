package me.arkadybazhanov.spacedust.core

class MonsterSpawner(
    private val level: Level,
    private val duration: Int,
    private val delay: Int = 0,
    private val positionValidator: (Position) -> Boolean,
    private val spawn: (Position) -> Character
) : EventGenerator {
    override val id: Int = Game.getNextId()

    override tailrec suspend fun getNextEvent(): Spawn {
        for ((pos, cell) in level.withPosition().shuffled(Game.random)) {
            if (cell.isEmpty() && positionValidator(pos)) {
                return Spawn(Game.time, duration, delay) { spawn(pos) }
            }
        }

        return getNextEvent()
    }
}