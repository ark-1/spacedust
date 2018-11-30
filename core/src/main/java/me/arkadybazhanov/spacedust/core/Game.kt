package me.arkadybazhanov.spacedust.core

import kotlin.random.*

class Game {

    private val rand = Random(0)

    infix fun Int.d(a: Int) = (1..this).sumBy { 1 + rand.nextInt(a) }

    fun main() {
        val damage = 2 d 12

    }

    fun update() {
        // TODO
    }
}
