package me.arkadybazhanov.spacedust.core

class UnionFind<T> {
    private val sizes = mutableMapOf<T, Int>().withDefault { 1 }
    private val parents = mutableMapOf<T, T>()

    private var T.parent
        get() = parents[this]
        set(value) {
            val parent = parent
            if (parent != null) parent.size -= size
            if (value != null) {
                value.size += size
                parents[this] = value
            } else {
                parents -= this
            }
        }

    private var T.size
        get() = sizes.getValue(this)
        set(value) = sizes.set(this, value)

    private fun T.lift() {
        val par = parent ?: return
        parent = par.parent ?: return
    }

    private tailrec fun find(elem: T): T {
        return find(parents[elem].also { elem.lift() } ?: return elem)
    }

    fun areConnected(elem1: T, elem2: T): Boolean = find(elem1) == find(elem2)

    fun connect(elem1: T, elem2: T) {
        val top1 = find(elem1)
        val top2 = find(elem2)

        if (top1.size < top2.size) {
            top1.parent = top2
        } else {
            top2.parent = top1
        }
    }
}