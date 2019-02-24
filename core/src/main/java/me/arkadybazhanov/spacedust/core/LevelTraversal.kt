package me.arkadybazhanov.spacedust.core

import java.util.*

fun getNextMoveToTarget(character: Character, isTarget: (Position) -> Boolean): Position {
    val bfs: Queue<Position> = ArrayDeque()
    bfs.add(character.position)

    val distances = HashMap<Position, Int>()
    distances[character.position] = 0

    val previousPositions = HashMap<Position, Position>()
    previousPositions[character.position] = character.position

    while (!bfs.isEmpty()) {
        val position = bfs.remove()!!

        for (to in character.near(position)) {

            if (character.canMoveTo(to) && to !in distances.keys) {
                distances[to] = distances[position]!! + 1
                previousPositions[to] = position
                bfs += to
            }

            if (isTarget(to)) {
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
