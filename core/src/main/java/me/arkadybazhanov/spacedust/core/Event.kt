package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.Serializable

interface Event : Savable {
    val time: Int
    val level: Level?
    val position: Position
    val duration: Int
}

val Event.cell get() = level?.get(position)

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
        override val duration: Int,
        override val saveId: Int = Game.getNextId()
    ) : Event {
        override val level = character.level
        override val position = character.position

        @Serializable
        data class SavedMoveEvent(
            val id: Int,
            val character: Int,
            val time: Int,
            val duration: Int
        ) : SavedStrong<MoveEvent> {
            override val refs = listOf(character)

            override fun initial(pool: Map<Int, Savable>) = MoveEvent(
                character = pool.load(character),
                time = time,
                duration = duration,
                saveId = id
            )
        }

        override fun save() = SavedMoveEvent(
            id = saveId,
            character = character.saveId,
            time = time,
            duration = duration
        )
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
        override val duration: Int,
        override val saveId: Int = Game.getNextId()
    ) : Event {

        override val level = attacker.level
        override val position = attacker.position

        @Serializable
        data class SavedAttackEvent(
            val id: Int,
            val attacker: Int,
            val defender: Int,
            val time: Int,
            val duration: Int
        ) : SavedStrong<AttackEvent> {
            override val refs = listOf(attacker, defender)

            override fun initial(pool: Map<Int, Savable>) = AttackEvent(
                attacker = pool.load(attacker),
                defender = pool.load(defender),
                time = time,
                duration = duration,
                saveId = id
            )
        }

        override fun save() = SavedAttackEvent(
            id = saveId,
            attacker = attacker.saveId,
            defender = defender.saveId,
            time = time,
            duration = duration
        )
    }

    override fun perform(): AttackEvent {
        defender.die()
        return AttackEvent(attacker, defender, time, duration)
    }
}

class Spawn(
    private val time: Int,
    private val duration: Int,
    private val delay: Int,
    private val spawn: () -> Character
) : Action {

    class SpawnEvent(
        override val time: Int,
        override val duration: Int,
        val character: Character,
        override val saveId: Int = Game.getNextId()
    ) : Event {

        override val level = character.level
        override val position = character.position

        @Serializable
        data class SavedSpawnEvent(
            val id: Int,
            val time: Int,
            val duration: Int,
            val character: Int
        ) : SavedStrong<SpawnEvent> {
            override val refs = listOf(character)

            override fun initial(pool: Map<Int, Savable>) = SpawnEvent(
                time = time,
                duration = duration,
                character = pool.load(character),
                saveId = id
            )
        }

        override fun save(): SavedSpawnEvent = SavedSpawnEvent(
            id = saveId,
            time = time,
            duration = duration,
            character = character.saveId
        )
    }

    override fun perform(): SpawnEvent {
        val character = spawn()
        character.create(delay)
        return SpawnEvent(time, duration, character)
    }
}
