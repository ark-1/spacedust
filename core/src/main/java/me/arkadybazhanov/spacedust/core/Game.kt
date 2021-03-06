package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.internal.ReferenceArraySerializer
import java.io.*
import java.util.*

class Game(override val saveId: Int = 0, random: Random? = null) : Savable {

    private var nextId = 1
    fun getNextId() = nextId++

    override val refs
        get() = characters.map { it.second }.run {
            current?.let { plus(it) } ?: this
        } + stairsMap.keys.map { it.first } + stairsMap.values.map { it.first }

    var time: Int = 0
        private set

    val characters = PriorityQueue<Pair<Int, EventGenerator>>(
        11,
        compareBy({ it.first }, { it.second })
    )

    var current: EventGenerator? = null

    val stairsMap: Cache<Pair<Level, Position>, Pair<Level, Position>> = Cache { (level, position) -> LevelGeneration.generateMaze(this, 31, 31, level, position) }

    suspend fun update(): Boolean {
        val next = current ?: run {
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

    override fun save(): SavedGame = SavedGame(
        saveId = saveId,
        nextId = nextId,
        time = time,
        characters = characters.map { it.first to it.second.saveId },
        current = current?.saveId,
        stairsMap = stairsMap.map { (key, value) ->
            (key.first.saveId to key.second) to (value.first.saveId to value.second)
        },
        random = serializeRandom()
    )

    @Serializable
    class SavedGame(
        override val saveId: Int,
        val nextId: Int,
        val time: Int,
        val characters: List<Pair<Int, Int>>,
        val current: Int?,
        val stairsMap: List<Pair<Pair<Int, Position>, Pair<Int, Position>>>,
        val random: String
    ) : SavedWeak<Game> {
        override val refs = characters.map(Pair<Int, Int>::second) + stairsMap.map { it.first.first } + stairsMap.map { it.second.first }

        private fun deserializeRandom(): Random = ByteArrayInputStream(random.toByteArray(Charsets.ISO_8859_1)).run {
            ReferenceArraySerializer(Any::class, String.serializer()).list
            ObjectInputStream(this).use { it.readObject() as Random }
        }

        override fun initial() = Game(saveId, random = deserializeRandom()).also {
            it.nextId = nextId
            it.time = time
        }

        override fun restore(initial: Game, pool: Pool) {
            initial.characters.addAll(characters.map { it.first to (pool[it.second] as EventGenerator) })
            if (current != null) {
                initial.current = pool.load(current)
            }
            initial.stairsMap.putAll(stairsMap.map { (key, value) ->
                (pool.load<Level>(key.first) to key.second) to (pool.load<Level>(value.first) to value.second)
            })
        }

    }

    val random = random ?: Random()
    fun withProbability(probability: Double) = random.nextDouble() < probability

    private fun serializeRandom(): String = ByteArrayOutputStream().apply {
        ObjectOutputStream(this).use { it.writeObject(random) }
    }.toByteArray().toString(Charsets.ISO_8859_1)
}
