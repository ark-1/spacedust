//@file:UseSerializers(FunctionSerializer::class)

package me.arkadybazhanov.spacedust

import android.support.annotation.IdRes
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.*
import me.arkadybazhanov.spacedust.SerializableGame.SerializableEvent.*
import me.arkadybazhanov.spacedust.SerializableGame.SerializableEventGenerator.*
import me.arkadybazhanov.spacedust.SerializableGame.SerializableLevel
import me.arkadybazhanov.spacedust.core.*
import me.arkadybazhanov.spacedust.core.CellType.*
import java.io.*

@Serializable
class SerializableGame(
    private val currentEventGenerator: Int?,
    private val eventGenerators: MutableMap<Int, SerializableEventGenerator>,
    private val levels: MutableMap<Int, SerializableLevel?>,
    private val eventGeneratorsQueue: List<Pair<Int, Int>>
) {

    constructor() : this(
        currentEventGenerator = Game.current?.id,
        eventGenerators = mutableMapOf(),
        levels = mutableMapOf(),
        eventGeneratorsQueue = Game.characters.map { (time, generator) ->
            println(generator)
            time to generator.id
        }
    ) {
        Game.characters.forEach { (_, generator) ->
            addGenerator(generator)
        }
        Game.current?.let(::addGenerator)
    }

    sealed class SerializableEvent {
        @Serializable
        data class MoveEvent(val character: Int, val time: Int, val duration: Int) : SerializableEvent() {
            constructor(event: Move.MoveEvent, game: SerializableGame) : this(
                character = event.character.also(game::addGenerator).id,
                time = event.time,
                duration = event.duration
            )
        }

        @Serializable
        data class AttackEvent(
            val attacker: Int,
            val defender: Int,
            val time: Int,
            val duration: Int
        ) : SerializableEvent() {
            constructor(event: Attack.AttackEvent, game: SerializableGame) : this(
                attacker = event.attacker.also(game::addGenerator).id,
                defender = event.defender.also(game::addGenerator).id,
                time = event.time,
                duration = event.duration
            )
        }

        @Serializable
        data class SpawnEvent(val character: Int, val time: Int, val duration: Int) : SerializableEvent() {
            constructor(event: Spawn.SpawnEvent, game: SerializableGame) : this(
                character = event.character.also(game::addGenerator).id,
                time = event.time,
                duration = event.duration
            )
        }
    }

    @Serializable
    data class SerializableCell(val type: CellType, val character: Int?, val events: List<SerializableEvent>) {
        constructor(cell: Cell, game: SerializableGame) : this(
            type = cell.type,
            character = cell.character?.also(game::addGenerator)?.id,
            events = cell.events.map {
                when (it) {
                    is Move.MoveEvent -> SerializableEvent.MoveEvent(it, game)
                    is Attack.AttackEvent -> SerializableEvent.AttackEvent(it, game)
                    is Spawn.SpawnEvent -> SerializableEvent.SpawnEvent(it, game)
                    else -> throw NotImplementedError("No serialization implemented for ${it::class.simpleName}")
                }
            }
        )
    }

    @Serializable
    class SerializableLevel(val w: Int, val h: Int, val cells: Array<Array<SerializableCell>>) {
        constructor(level: Level, game: SerializableGame) : this(
            w = level.w,
            h = level.h,
            cells = array2D(level.w, level.h) { x, y ->
                SerializableCell(level[x, y], game)
            }
        )
    }

    sealed class SerializableEventGenerator {
        @Serializable
        data class SerializablePlayer(
            val position: Position,
            val id: Int,
            val level: Int,
            @IdRes val view: Int
        ) : SerializableEventGenerator() {
            constructor(player: Player, game: SerializableGame) : this(
                id = player.id,
                position = player.position,
                level = player.level.also(game::addLevel).id,
                view = player.view.id
            )
        }

        @Serializable
        data class SerializableBasicMonster(
            val id: Int,
            val level: Int,
            val position: Position,
            val maxHp: Int,
            val speed: Int,
            val strength: Int
        ) : SerializableEventGenerator() {
            constructor(monster: BasicMonster, game: SerializableGame) : this(
                id = monster.id,
                level = monster.level.also(game::addLevel).id,
                position = monster.position,
                maxHp = monster.maxHp,
                speed = monster.speed,
                strength = monster.strength
            )
        }

        @Serializable
        data class SerializableMonsterSpawner(
            val id: Int,
            val level: Int,
            val duration: Int,
            val delay: Int,
            @Serializable(with = FunctionSerializer::class) val positionValidator: (Level, Position) -> Boolean,
            @Serializable(with = FunctionSerializer::class) val spawn: (Level, Position) -> Character
        ) : SerializableEventGenerator() {
            constructor(spawner: MonsterSpawner, game: SerializableGame) : this(
                id = spawner.id,
                level = spawner.level.also(game::addLevel).id,
                duration = spawner.duration,
                delay = spawner.delay,
                positionValidator = spawner.positionValidator,
                spawn = spawner.spawn
            )
        }
    }

    private fun serializable(generator: EventGenerator): SerializableEventGenerator = when (generator) {
        is Player -> SerializablePlayer(generator, this)
        is BasicMonster -> SerializableBasicMonster(generator, this)
        is MonsterSpawner -> SerializableMonsterSpawner(generator, this)
        else -> throw NotImplementedError("No serialization implemented for ${generator::class.simpleName}")
    }

    private fun addGenerator(character: EventGenerator) {
        if (character.id !in eventGenerators) {
            eventGenerators[character.id] = serializable(character)
        }
    }

    private fun addLevel(level: Level) {
        if (level.id !in levels) {
            levels[level.id] = null
            levels[level.id] = SerializableLevel(level, this)
        }
    }

    fun restore(activity: MainActivity) {
        val levelsSerializable = mutableMapOf<Int, SerializableLevel>()

        val levels = Cache<Int, Level> { key ->
            val serializable = levels.getValue(key)!!.also { levelsSerializable[key] = it }
            Level(array2D(serializable.w, serializable.h) { _, _ -> Cell(STONE) }, key)
        }

        val eventGeneratorsRestored = Cache<Int, EventGenerator> {
            println("restoring eg")
            eventGenerators.getValue(it).restore(activity, levels)
        }

        for ((time, eventGenerator) in eventGeneratorsQueue) {
            Game.characters.clear()
            Game.characters += time to eventGeneratorsRestored.getValue(eventGenerator)
        }

        println("levels size ${levels.size}")
        for ((id, level) in levels) {
            val serialized = levelsSerializable.getValue(id)
            println("before")
            printLevel(serialized)
            for ((position, cell) in level.withPosition()) {
                val serializedCell = serialized.cells[position.x][position.y]
                cell.type = serializedCell.type
                cell.character = serializedCell.character?.let {
                    eventGeneratorsRestored.getValue(it) as Character
                }

                cell.events += serializedCell.events.map {
                    when (it) {
                        is AttackEvent -> Attack.AttackEvent(
                            attacker = eventGeneratorsRestored.getValue(it.attacker) as Character,
                            defender = eventGeneratorsRestored.getValue(it.defender) as Character,
                            time = it.time,
                            duration = it.duration
                        )
                        is MoveEvent -> Move.MoveEvent(
                            character = eventGeneratorsRestored.getValue(it.character) as Character,
                            time = it.time,
                            duration = it.duration
                        )
                        is SpawnEvent -> Spawn.SpawnEvent(
                            time = it.time,
                            duration = it.duration,
                            character = eventGeneratorsRestored.getValue(it.character) as Character
                        )
                    }
                }
            }
            println("after")
            printLevel(level)
        }

        Game.current = currentEventGenerator.also { println("cur is $it") }?.let(eventGeneratorsRestored::getValue)
    }

    private fun SerializableEventGenerator.restore(activity: MainActivity, levels: Map<Int, Level>): EventGenerator {
        return when (this) {
            is SerializablePlayer -> Player(
                level = levels.getValue(level),
                position = position,
                view = activity.gameView,
                id = id
            )
            is SerializableBasicMonster -> BasicMonster(
                level = levels.getValue(level),
                position = position,
                speed = speed,
                maxHp = maxHp,
                strength = strength,
                id = id
            )
            is SerializableMonsterSpawner -> MonsterSpawner(
                level = levels.getValue(level),
                duration = duration,
                delay = delay,
                positionValidator = positionValidator,
                spawn = spawn,
                id = id
            )
        }
    }
}

@Serializer(forClass = Function::class)
object FunctionSerializer : KSerializer<Function<*>> {
    override val descriptor: SerialDescriptor = StringDescriptor.withName(this::class.simpleName!!)

    private fun fromByteArray(array: ByteArray) = ByteArrayInputStream(array).let { input ->
        ObjectInputStream(input).use {
            it.readObject() as Function<*>
        }
    }

    override fun deserialize(decoder: Decoder): Function<*> {
        return fromByteArray(HexConverter.parseHexBinary(decoder.decodeString()))
    }

    private fun Function<*>.toByteArray() = ByteArrayOutputStream().also { output ->
        ObjectOutputStream(output).use { oos ->
            oos.writeObject(this as java.io.Serializable)
        }
    }.toByteArray()!!

    override fun serialize(encoder: Encoder, obj: Function<*>) {
        encoder.encodeString(HexConverter.printHexBinary(obj.toByteArray()))
    }
}

fun printLevel(level: Level) = buildString {
    for (x in 0 until level.h) {
        for (y in 0 until level.w) {
            when (level[x, y].type) {
                AIR -> '.'
                STONE -> '#'
            }.let(::append)
        }
        append('\n')
    }
}.let(::println)

fun printLevel(level: SerializableLevel) = buildString {
    for (x in 0 until level.h) {
        for (y in 0 until level.w) {
            when (level.cells[x][y].type) {
                AIR -> '.'
                STONE -> '#'
            }.let(::append)
        }
        append('\n')
    }
}.let(::println)
