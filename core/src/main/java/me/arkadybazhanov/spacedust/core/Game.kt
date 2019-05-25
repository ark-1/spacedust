package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.Serializable
import java.util.PriorityQueue
import kotlin.random.Random

class Game(override val saveId: Int = getNextId()) : Savable {
    override val refs get() = characters.map { it.second }.run {
        current?.let { plus(it) } ?: this
    }

    var time: Int = 0
        private set

    val characters = PriorityQueue<Pair<Int, EventGenerator>>(
        11,
        compareBy({ it.first }, { it.second })
    )

    var current: EventGenerator? = null

    suspend fun update(): Boolean {
        val next = current ?: run {
            val next = characters.poll() ?: return false
            time = next.first
            current = next.second
            next.second
        }

        val event = next.getNextEvent().perform()
        event.cell?.events?.add(event)

        val newTime = time + event.duration
        characters += newTime to next
        current = null
        return true
    }

    override fun save(): SavedGame = SavedGame(saveId, time, characters.map { it.first to it.second.saveId }, current?.saveId)

    @Serializable
    data class SavedGame(
        override val saveId: Int,
        val time: Int,
        val characters: List<Pair<Int, Int>>,
        val current: Int?
    ) : SavedWeak<Game> {
        override val refs = characters.map(Pair<Int, Int>::second)

        override fun initial() = Game(saveId).also { it.time = time }

        override fun restore(initial: Game, pool: Pool) {
            initial.characters.addAll(characters.map { it.first to (pool[it.second] as EventGenerator) })
            if (current != null) {
                initial.current = pool[current] as EventGenerator
            }
        }

    }

    companion object {
        val seed = System.nanoTime()
        val random = Random(seed)
        fun withProbability(probability: Double) = random.nextDouble() < probability

        private var nextId = 0
        fun getNextId() = nextId++
    }
}
