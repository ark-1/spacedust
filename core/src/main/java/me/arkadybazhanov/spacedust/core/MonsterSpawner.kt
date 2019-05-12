package me.arkadybazhanov.spacedust.core

abstract class MonsterSpawner(
    val level: Level,
    val duration: Int,
    val delay: Int = 0,
    override val saveId: Int = Game.getNextId()
) : EventGenerator {

    abstract fun positionValidator(position: Position): Boolean
    abstract fun spawn(position: Position): Character

    final override tailrec suspend fun getNextEvent(): Spawn {
        for ((pos, cell) in level.withPosition().shuffled(Game.random)) {
            if (cell.isEmpty() && positionValidator(pos)) {
                return Spawn(level.game.time, duration, delay) { spawn(pos) }
            }
        }

        return getNextEvent()
    }
}