package me.arkadybazhanov.spacedust.core

abstract class MonsterSpawner(
    val level: Level,
    val duration: Int,
    val delay: Int = 0,
    override val saveId: Int = level.game.getNextId()
) : EventGenerator {

    abstract fun positionValidator(position: Position): Boolean
    abstract fun spawn(position: Position): Character

    override val refs = listOf(level)

    final override tailrec suspend fun getNextEvent(): Spawn {
        for ((pos, cell) in level.withPosition().shuffled(level.game.random)) {
            if (cell.isEmpty() && positionValidator(pos)) {
                return Spawn(level.game.time, duration, delay) { spawn(pos) }
            }
        }

        return getNextEvent()
    }
}