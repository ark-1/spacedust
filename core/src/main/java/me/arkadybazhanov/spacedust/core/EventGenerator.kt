package me.arkadybazhanov.spacedust.core

interface EventGenerator : Comparable<EventGenerator> {
    val id: Int
    suspend fun getNextEvent(): Action

    override fun compareTo(other: EventGenerator): Int {
        return id.compareTo(other.id)
    }
}