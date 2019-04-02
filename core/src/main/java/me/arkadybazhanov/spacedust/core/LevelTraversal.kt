package me.arkadybazhanov.spacedust.core

import java.util.*

fun getPathToTarget(character: Character, isTarget: (Position) -> Boolean): List<Position>? {
    val bfs: Queue<Position> = ArrayDeque()
    bfs.add(character.position)

    val distances = HashMap<Position, Int>()
    distances[character.position] = 0

    val previousPositions = HashMap<Position, Position>()
    previousPositions[character.position] = character.position

    while (!bfs.isEmpty()) {
        val position = bfs.remove()!!
        for (to in character.near(position)) {
            if (character.canMoveTo(to) && to !in distances.keys && character.isVisible(to)) {
                distances[to] = distances[position]!! + 1
                previousPositions[to] = position
                bfs += to
            }
        }

        if (isTarget(position)) {
            val path = ArrayList<Position>()
            var destination = position
            path += destination
            while (previousPositions[destination]!! != character.position) {
                destination = previousPositions[destination]!!
                path += destination
            }
            path.reverse()
            return path
        }
    }
    return null
}
