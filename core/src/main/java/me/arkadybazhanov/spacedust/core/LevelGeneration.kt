package me.arkadybazhanov.spacedust.core

fun generateLevelAndPut(character: Character): Level = generateLevelAndCreate { _, _ -> character }.first

inline fun <T : Character> generateLevelAndCreate(characterSupplier: (Level, Position) -> T): Pair<Level, T> {
    val (level, position) = generateLevel()
    val character = characterSupplier(level, position)
    character.position = position
    character.level = level
    return level to character
}

fun generateLevel(): Pair<Level, Position> {
    val level = Level(Array(40) { x ->
        Array(40) { y ->
            if (x % (y + 1) == 0) Cell.Stone() else Cell.Air()
        }
    })
    val monster = BasicMonster(level, Position(8, 10))
    Game.characters += 0 to monster
    return level to Position(1, 1)
}
