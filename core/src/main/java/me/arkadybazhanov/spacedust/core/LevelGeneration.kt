package me.arkadybazhanov.spacedust.core

import me.arkadybazhanov.spacedust.core.CellType.*

object LevelGeneration {
    fun generateLevelAndPut(character: Character): Level = generateLevelAndCreate { _, _ -> character }.first

    inline fun <T : Character> generateLevelAndCreate(characterSupplier: (Level, Position) -> T): Pair<Level, T> {
        val (level, position) = generateMaze(30, 30)

        val character = characterSupplier(level, position)
        character.position = position
        character.level = level
        character.create()

        return level to character
    }

    private fun Level.createDefaultMonster(position: Position) {
        defaultMonster(position).create()
    }

    fun generateMaze(w: Int, h: Int): Pair<Level, Position> {
        require(w > 2 && h > 2) { "width and height should be > 2 (w = $w, h = $h)" }

        val edges = mutableSetOf<Position>()
        val connectivity = UnionFind<Position>()

        val cells = Array(w) { x ->
            Array(h) { y ->
                when {
                    x == 0 || y == 0 || x == w - 1 || y == h - 1 -> Cell(STONE)
                    (x + y) % 2 == 1 -> null.also { edges += Position(x, y) }
                    x % 2 == 0 -> Cell(STONE)
                    else -> Cell(AIR)
                }
            }
        }

        while (edges.isNotEmpty()) {
            val edge = edges.random(Game.random).also { edges.remove(it) }
            val (v1, v2) = if (edge.x % 2 == 0) {
                Position(edge.x - 1, edge.y) to Position(edge.x + 1, edge.y)
            } else {
                Position(edge.x, edge.y - 1) to Position(edge.x, edge.y + 1)
            }

            cells[edge.x][edge.y] = if (connectivity.areConnected(v1, v2)) {
                if (Game.withProbability(0.1)) {
                    Cell(AIR)
                } else {
                    Cell(STONE)
                }
            } else {
                connectivity.connect(v1, v2)
                Cell(AIR)
            }
        }

        if (javaClass.desiredAssertionStatus()) {
            assert(cells.all { it.all { cell -> cell != null } })
        }

        val startPos = Position(1, 1)

        @Suppress("UNCHECKED_CAST")
        return Level(cells as Array<Array<Cell>>).also { level ->
            for ((pos, cell) in level.withPosition()) {
                if (cell.type in BasicMonster.canStandIn && pos != startPos && Game.withProbability(0.05)) {
                    level.createDefaultMonster(pos)
                }
            }

            Game.characters += Game.time to MonsterSpawner(
                level = level,
                duration = 40,
                delay = 100,
                positionValidator = { level[it].type in BasicMonster.canStandIn }
            ) { level.defaultMonster(it) }
        } to Position(1, 1)
    }

    private fun Level.defaultMonster(position: Position) =
        BasicMonster(this, position, 15, 100, 10)
}
