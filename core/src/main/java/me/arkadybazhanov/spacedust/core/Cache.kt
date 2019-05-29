package me.arkadybazhanov.spacedust.core

class Cache<K, V> private constructor(
    private val map: MutableMap<K, V>,
    private val getValue: (K, Cache<K, V>) -> V
) : MutableMap<K, V> by map {

    constructor(getValue: (K) -> V) : this({ key, _ -> getValue(key) })
    constructor(getValue: (K, Cache<K, V>) -> V) : this(mutableMapOf(), getValue)

    override fun get(key: K): V = map[key] ?: getValue(key, this).also { this[key] = it }
}
