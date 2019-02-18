package me.arkadybazhanov.spacedust

import kotlinx.coroutines.delay
import me.arkadybazhanov.spacedust.core.*
import java.lang.IllegalStateException
import kotlin.random.Random

class Player(override var level: Level, override var position: Position) : Character {
    override val id: Int = Game.getNextId()

    private val random = Random(0)

    override suspend fun getCharacterMove(): PerformableEvent {

        var pos: Position
        do pos = position + when (random.nextInt(4)) {
            0 -> Direction(0, -1)
            1 -> Direction(-1, 0)
            2 -> Direction(0, 1)
            3 -> Direction(1, 0)
            else -> throw IllegalStateException()
        } while (pos.x !in (0 until level.w) || pos.y !in (0 until level.h))

        delay(500)

        return Move(this, pos, Game.time, 2)
    }
}