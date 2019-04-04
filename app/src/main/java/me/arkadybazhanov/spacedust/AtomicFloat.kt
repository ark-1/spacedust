package me.arkadybazhanov.spacedust

import java.lang.Float.*
import java.util.concurrent.atomic.AtomicInteger

inline class AtomicFloat(private val bits: AtomicInteger) {
    constructor(initialValue: Float) : this(AtomicInteger(floatToIntBits(initialValue)))

    var value: Float
        get() = intBitsToFloat(bits.get())
        set(value) = bits.set(floatToIntBits(value))

    private fun compareAndSet(expect: Float, update: Float) = bits.compareAndSet(
        floatToIntBits(expect),
        floatToIntBits(update)
    )

    operator fun plusAssign(d: Float) {
        do {
            val v = value
        } while (!compareAndSet(v, v + d))
    }

    operator fun minusAssign(d: Float) = plusAssign(-d)
}