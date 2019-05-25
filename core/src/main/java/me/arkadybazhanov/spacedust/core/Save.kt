package me.arkadybazhanov.spacedust.core

interface Savable {
    val saveId: Int
    fun save(): Saved<*>
    val refs: Iterable<Savable>
}

interface Saved<in I : Savable> {
    val saveId: Int
    val refs: Iterable<Int>

    fun restore(initial: I, pool: Pool)
}

interface SavedWeak<I : Savable> : Saved<I> {
    fun initial(): I
}

interface SavedStrong<I : Savable> : Saved<I> {
    fun initial(pool: Pool): I

    override fun restore(initial: I, pool: Pool) {}
}

interface Pool {
    operator fun get(id: Int): Savable
}

inline fun <reified T : Savable> Pool.load(id: Int): T = this[id] as T

inline fun <reified T : Savable> List<Int>.loadEach(pool: Pool): Iterable<T> =
    asSequence().map { pool[it] as T }.asIterable()
