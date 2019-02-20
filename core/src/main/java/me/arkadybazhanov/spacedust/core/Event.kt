package me.arkadybazhanov.spacedust.core

interface Event {
    val time: Int
}

interface PerformableEvent : Event {
    fun perform(): Int
}

class Move(
    val character: Character,
    val to: Position,
    override val time: Int,
    val duration: Int
) : PerformableEvent {

    override fun perform(): Int {
        character.level[character.position].character = null
        character.position = to
        character.level[character.position].character = character
        return duration
    }
}

class Attack(
    val attacker: Character,
    val defender: Character,
    override val time: Int,
    val duration: Int
) : PerformableEvent {

    override fun perform(): Int {
        defender.die()
        return duration
    }
}