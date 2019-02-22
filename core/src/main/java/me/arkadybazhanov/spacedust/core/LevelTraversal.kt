package me.arkadybazhanov.spacedust.core

import java.util.*

fun getNextMoveToTarget(character: Character, target: Character): Position {
    val bfs: Queue<Position> = ArrayDeque()
    bfs.add(character.position)

    val distances = HashMap<Position, Int>()
    distances[character.position] = 0

    val previousPositions = HashMap<Position, Position>()
    previousPositions[character.position] = character.position

    while (!bfs.isEmpty()) {
        val position = bfs.remove()!!

        for (direction in character.directions) {
            val to = position + direction

            if (character.canMoveTo(to) && to !in distances.keys) {
                distances[to] = distances[position]!! + 1
                previousPositions[to] = position
                bfs.add(to)
            }

            if (to == target.position) {
                var destination = position
                while (previousPositions[destination]!! != character.position) {
                    destination = previousPositions[destination]!!
                }
                return destination
            }
        }
    }
    return character.position
}
