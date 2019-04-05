package me.arkadybazhanov.spacedust.core

import java.util.PriorityQueue
import kotlin.random.Random

object Game {
    val characters = PriorityQueue<Pair<Int, EventGenerator>>(
        11,
        compareBy({ it.first }, { it.second })
    )

    val seed = System.nanoTime()
    val random = Random(seed)
    fun withProbability(probability: Double) = random.nextDouble() < probability

    private var nextId = 0
    var time: Int = 0
        private set

    fun getNextId() = nextId++

    var current: EventGenerator? = null

    suspend fun update(): Boolean {
        val next = current.also { println("current is $it") } ?: run {
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

    fun reset() = characters.clear()
}
