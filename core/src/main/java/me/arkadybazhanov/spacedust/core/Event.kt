package me.arkadybazhanov.spacedust.core

interface Event {
    val time: Int
    val cell: Cell?
    val duration: Int
}

interface Action {
    fun perform(): Event
}

class Move(
    private val character: Character,
    private val to: Position,
    private val time: Int,
    private val duration: Int
) : Action {

    class MoveEvent(
        val character: Character,
        override val time: Int,
        override val duration: Int
    ) : Event {
        override val cell = character.cell
    }

    override fun perform(): MoveEvent {
        character.cell.character = null
        character.position = to
        character.cell.character = character

        return MoveEvent(character, time, duration)
    }
}

class Attack(
    private val attacker: Character,
    private val defender: Character,
    private val time: Int,
    private val duration: Int
) : Action {

    class AttackEvent(
        val attacker: Character,
        val defender: Character,
        override val time: Int,
        override val duration: Int
    ) : Event {
        override val cell = attacker.cell
    }

    override fun perform(): AttackEvent {
        defender.die()
        return AttackEvent(attacker, defender, time, duration)
    }
}

class Spawn(
    val time: Int,
    private val duration: Int,
    private val delay: Int,
    private val spawn: () -> Character
) : Action {

    class SpawnEvent(
        override val time: Int,
        override val duration: Int,
        val character: Character
    ) : Event {
        override val cell = character.cell
    }

    override fun perform(): SpawnEvent {
        val character = spawn()
        character.create(delay)
        return SpawnEvent(time, duration, character)
    }
}
