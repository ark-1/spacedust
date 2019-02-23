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
    private val duration: Int
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
    private val duration: Int
) : PerformableEvent {

    override fun perform(): Int {
        defender.die()
        return duration
    }
}

class Spawn(
    override val time: Int,
    private val duration: Int,
    private val delay: Int,
    private val spawn: () -> Character
) : PerformableEvent {
    override fun perform(): Int {
        spawn().create(delay)
        return duration
    }
}
