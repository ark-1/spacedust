package me.arkadybazhanov.spacedust.core

fun generateLevel(character: Character): Level {
    val (level, position) = generateLevel()
    character.position = position
    character.level = level
    return level
}

fun generateLevel(): Pair<Level, Position> {
    val level = Level(Array(40) { x ->
        Array(40) { y ->
            if (x % (y + 1) == 0) Cell.Stone() else Cell.Air()
        }
    })
    return level to Position(1, 1)
}
