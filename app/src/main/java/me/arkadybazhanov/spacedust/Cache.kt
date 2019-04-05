package me.arkadybazhanov.spacedust

class Cache<K, V> private constructor(
    private val map: MutableMap<K, V>,
    private val getValue: (K) -> V
) : MutableMap<K, V> by map {

    constructor(getValue: (K) -> V) : this(mutableMapOf(), getValue)

    override fun get(key: K): V = map[key] ?: getValue(key).also { this[key] = it }
}
