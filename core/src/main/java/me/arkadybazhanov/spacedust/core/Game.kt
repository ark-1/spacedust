package me.arkadybazhanov.spacedust.core

import java.util.*

object Game {
    val characters = PriorityQueue<Pair<Int, Character>>(
        11,
        compareBy({ it.first }, { it.second })
    )
    private var nextId = 0
    var time: Int = 0
        private set

    fun getNextId() = nextId++

    suspend fun update() {
        val (time, nextCharacter) = characters.poll() ?: error("Nothing to do :(")
        this.time = time
        val newTime = time + nextCharacter.getCharacterMove().perform()
        characters += newTime to nextCharacter
    }
}
