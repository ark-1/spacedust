package me.arkadybazhanov.spacedust.core

import java.util.*

fun getNextMove(character: Character, destination: Position): Position {
    val bfs = ArrayDeque<Position>()
    bfs.add(character.position)
    val distances = HashMap<Position, Int>()
    distances[character.position] = 0
    val previousPositions = HashMap<Position, Position>()
    previousPositions[character.position] = character.position
    while (!bfs.isEmpty()) {
        val position = bfs.pollFirst()
        for (direction in getLegalDirections(character, position)) {
            if (!distances.containsKey(position + direction)) {
                distances[position + direction] = distances[position]!! + 1
                previousPositions[position + direction] = position
                bfs.addLast(position + direction)
            }
        }
    }
    if (!distances.containsKey(destination)) {
        return character.position
    }
    var position = destination
    while (previousPositions[position]!! != character.position) {
        position = previousPositions[position]!!
    }
    return position
}

fun getLegalDirections(character: Character, position: Position): List<Direction> {
    return character.directions.filter { it -> character.canMoveTo(position + it) }
}