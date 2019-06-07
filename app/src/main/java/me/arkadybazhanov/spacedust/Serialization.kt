package me.arkadybazhanov.spacedust

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import me.arkadybazhanov.spacedust.Player.SavedPlayer
import me.arkadybazhanov.spacedust.core.*
import me.arkadybazhanov.spacedust.core.Attack.AttackEvent.SavedAttackEvent
import me.arkadybazhanov.spacedust.core.Monster.SavedBasicMonster
import me.arkadybazhanov.spacedust.core.Game.SavedGame
import me.arkadybazhanov.spacedust.core.HealKit.*
import me.arkadybazhanov.spacedust.core.Level.SavedLevel
import me.arkadybazhanov.spacedust.core.LevelGeneration.DefaultMonsterSpawner.SavedDefaultMonsterSpawner
import me.arkadybazhanov.spacedust.core.Move.MoveEvent.SavedMoveEvent
import me.arkadybazhanov.spacedust.core.Spawn.SpawnEvent.SavedSpawnEvent
import me.arkadybazhanov.spacedust.core.Weapon.*
import kotlin.reflect.KClass

class SerializedState(val kClasses: Array<String>, val values: Array<String>, val name: String?)

fun serialize(player: Player, name: String?): SerializedState {
    val saved = save(player).values
    return SerializedState(
        saved.map { it::class.qualifiedName!! }.toTypedArray(),
        saved.map {
            stringifyDynamic(it::class.serializer(), it)
        }.toTypedArray(),
        name
    )
}

val json = Json(JsonConfiguration.Stable)

private fun <T> stringifyDynamic(ser: KSerializer<T>, obj: Any): String {
    @Suppress("UNCHECKED_CAST")
    return json.stringify(ser, obj as T)
}

fun save(savable: Savable): Map<Savable, Saved<*>> = mutableMapOf<Savable, Saved<*>>().also {
    saveRecursively(savable, it)
}

private fun saveRecursively(savable: Savable, cache: MutableMap<Savable, Saved<*>>) {
    if (savable in cache) return
    cache[savable] = savable.save()
    savable.refs.forEach {
        saveRecursively(it, cache)
    }
}

fun makeInitial(id: Int, pool: Pool, saved: Map<Int, Saved<*>>): Savable {
    return when (val s = saved.getValue(id)) {
        is SavedWeak -> s.initial()
        is SavedStrong -> s.initial(pool)
        else -> error("Every `Saved` implementation must implement `SavedWeak` or `SavedStrong`")
    }
}

private inline fun <reified T : Saved<*>> MutableMap<String, KClass<out Saved<*>>>.add() {
    this[T::class.qualifiedName!!] = T::class
}

private val savedClasses = mutableMapOf<String, KClass<out Saved<*>>>().apply {
    add<SavedPlayer>()
    add<SavedBasicMonster>()
    add<SavedAttackEvent>()
    add<SavedMoveEvent>()
    add<SavedSpawnEvent>()
    add<SavedGame>()
    add<SavedLevel>()
    add<SavedDefaultMonsterSpawner>()
    add<SavedWeapon>()
    add<SavedHealKit>()
}

private fun forName(className: String): KClass<*> = try {
    Class.forName(className).kotlin
} catch (e: ClassNotFoundException) {
    savedClasses[className] ?: error("class $className not found")
}

fun SerializedState.restorePlayer(view: GameView): Player {
    val saved = List(values.size) {
        json.parse(forName(kClasses[it]).serializer(), values[it]) as Saved<*>
    }.associateBy { it.saveId }

    val pool = CachePool(view) { id, pool ->
        makeInitial(id, pool, saved)
    }

    saved.values.forEach {
        pool[it.saveId]
    }

    saved.values.forEach {
        @Suppress("UNCHECKED_CAST")
        (it as Saved<Savable>).restore(pool[it.saveId], pool)
    }

    return pool.load(Player.PLAYER_SAVE_ID)
}

class CachePool(val view: GameView, getValue: (Int, CachePool) -> Savable) : Pool {
    private val cache: Cache<Int, Savable> = Cache { it ->
        getValue(it, this)
    }

    override fun get(id: Int) = cache[id]
}
