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
        override val saveId: Int = character.game.getNextId()
    ) : Event {
        override val refs = listOf(character)

        override val level get() = character.level
        override val position get() = character.position

        @Serializable
        data class SavedMoveEvent(
            override val saveId: Int,
            val character: Int,
            val time: Int,
            val duration: Int
        ) : SavedStrong<MoveEvent> {
            override val refs = listOf(character)

            override fun initial(pool: Pool) = MoveEvent(
                character = pool.load(character),
                time = time,
                duration = duration,
                saveId = saveId
            )
        }

        override fun save() = SavedMoveEvent(
            saveId = saveId,
            character = character.saveId,
            time = time,
            duration = duration
        )
    }

    override fun perform(): MoveEvent {
        character.cell.character = null
        character.position = to
        character.cell.character = character
        if (character.cell.items.isNotEmpty()) {
            for (item in character.cell.items) {
                item.process(character)
            }
            character.cell.items.clear()
        }
        when (character.cell.type) {
            CellType.DOWNSTAIRS, CellType.UPSTAIRS -> {
                val res = character.game.stairsMap[character.level to character.position]
                character.cell.character = null
                character.move(res.first, res.second)
                character.cell.character = character
            }
            else -> {}
        }

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
        override val saveId: Int = attacker.game.getNextId()
    ) : Event {
        override val refs = listOf(attacker, defender)

        override val level get() = attacker.level
        override val position get() = attacker.position

        @Serializable
        data class SavedAttackEvent(
            override val saveId: Int,
            val attacker: Int,
            val defender: Int,
            val time: Int,
            val duration: Int
        ) : SavedStrong<AttackEvent> {
            override val refs = listOf(attacker, defender)

            override fun initial(pool: Pool) = AttackEvent(
                attacker = pool.load(attacker),
                defender = pool.load(defender),
                time = time,
                duration = duration,
                saveId = saveId
            )
        }

        override fun save() = SavedAttackEvent(
            saveId = saveId,
            attacker = attacker.saveId,
            defender = defender.saveId,
            time = time,
            duration = duration
        )
    }

    override fun perform(): AttackEvent {
        defender.hp -= attacker.strength
        attacker.inventory.map { if (it is Weapon) it.damage else 0 }.max()?.let { defender.hp -= it }
        if (defender.hp <= 0) {
            defender.die()
        }
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
        override val saveId: Int = character.game.getNextId()
    ) : Event {
        override val refs = listOf(character)

        override val level get() = character.level
        override val position get() = character.position

        @Serializable
        data class SavedSpawnEvent(
            override val saveId: Int,
            val time: Int,
            val duration: Int,
            val character: Int
        ) : SavedStrong<SpawnEvent> {
            override val refs = listOf(character)

            override fun initial(pool: Pool) = SpawnEvent(
                time = time,
                duration = duration,
                character = pool.load(character),
                saveId = saveId
            )
        }

        override fun save(): SavedSpawnEvent = SavedSpawnEvent(
            saveId = saveId,
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
