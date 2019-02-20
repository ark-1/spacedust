package me.arkadybazhanov.spacedust.core

fun generateLevelAndPut(character: Character): Level = generateLevelAndCreate { _, _ -> character }.first

inline fun <T : Character> generateLevelAndCreate(characterSupplier: (Level, Position) -> T): Pair<Level, T> {
    val (level, position) = generateLevel()

    val character = characterSupplier(level, position)
    character.position = position
    character.level = level
    character.put()

    return level to character
}

fun generateLevel(): Pair<Level, Position> {
    val level = Level(Array(40) { x ->
        Array(40) { y ->
            Cell(if (x % (y + 1) == 0) CellType.STONE else CellType.AIR)
        }
    })

    BasicMonster(level, Position(1, 13), 15, 100, 10).put()
    BasicMonster(level, Position(8, 20), 15, 100, 10).put()

    return level to Position(1, 1)
}
