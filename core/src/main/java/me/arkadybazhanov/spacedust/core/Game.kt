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

    suspend fun update(): Boolean {
        val (time, nextCharacter) = characters.poll() ?: return false
        this.time = time
        val newTime = time + nextCharacter.getNextEvent().perform()
        characters += newTime to nextCharacter
        return true
    }

    fun reset() = characters.clear()
}
