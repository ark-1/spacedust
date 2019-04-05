package me.arkadybazhanov.spacedust.core

/**
 *  @param positionValidator CANNOT CAPTURE!!
 *  @param spawn CANNOT CAPTURE!!
 */
class MonsterSpawner(
    val level: Level,
    val duration: Int,
    val delay: Int = 0,
    val positionValidator: (Level, Position) -> Boolean,
    override val id: Int = Game.getNextId(),
    val spawn: (Level, Position) -> Character
) : EventGenerator {

    override tailrec suspend fun getNextEvent(): Spawn {
        for ((pos, cell) in level.withPosition().shuffled(Game.random)) {
            if (cell.isEmpty() && positionValidator(level, pos)) {
                return Spawn(Game.time, duration, delay) { spawn(level, pos) }
            }
        }

        return getNextEvent()
    }
}