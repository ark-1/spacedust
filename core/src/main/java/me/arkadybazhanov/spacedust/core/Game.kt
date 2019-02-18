package me.arkadybazhanov.spacedust.core

import java.util.*

object Game {
    val characters = PriorityQueue<Pair<Int, Character>>()
    val nextId = 0

    suspend fun update() {
        val (time, nextCharacter) = characters.poll()
        val newTime = time + nextCharacter.getCharacterMove().perform()
        characters.add(newTime to nextCharacter)
    }
}
