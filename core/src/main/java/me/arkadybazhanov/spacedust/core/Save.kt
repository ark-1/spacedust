package me.arkadybazhanov.spacedust.core

interface Savable {
    val saveId: Int
    fun save(): Saved<*>
}

interface Saved<in I : Savable> {
    val refs: Iterable<Int>

    fun restore(initial: I, pool: Map<Int, Savable>)
}

interface SavedWeak<I : Savable> : Saved<I> {
    fun initial(): I
}

interface SavedStrong<I : Savable> : Saved<I> {
    fun initial(pool: Map<Int, Savable>): I

    override fun restore(initial: I, pool: Map<Int, Savable>) {}
}

inline fun <reified T : Savable> Map<Int, Savable>.load(id: Int): T = this[id] as T

inline fun <reified T : Savable> List<Int>.loadEach(pool: Map<Int, Savable>): Iterable<T> =
    asSequence().map { pool[it] as T }.asIterable()
