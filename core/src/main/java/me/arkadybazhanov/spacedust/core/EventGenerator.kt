package me.arkadybazhanov.spacedust.core

interface EventGenerator : Comparable<EventGenerator>, Savable {
    override val saveId: Int
    suspend fun getNextEvent(): Action

    override fun compareTo(other: EventGenerator): Int {
        return saveId.compareTo(other.saveId)
    }
}
