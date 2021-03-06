package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.Serializable
import me.arkadybazhanov.spacedust.core.CellType.*
import kotlin.random.asKotlinRandom
import me.arkadybazhanov.spacedust.core.Monster.MonsterType.*

object LevelGeneration {
    const val DEFAULT_LEVEL_SIZE = 20

    inline fun <T : Character> generateLevelAndCreate(game: Game, characterSupplier: (Level, Position) -> T): Pair<Level, T> {
        val (level, position) = generateMaze(game, DEFAULT_LEVEL_SIZE, DEFAULT_LEVEL_SIZE, null, null)
        return create(level, position, characterSupplier)
    }

    inline fun <T : Character> create(
        level: Level,
        position: Position,
        characterSupplier: (Level, Position) -> T
    ): Pair<Level, T> {
        val character = characterSupplier(level, position)
        character.position = position
        character.level = level
        character.create()
        return level to character
    }

    private fun Level.createMonster(position: Position) {
        randomMonster(position).create()
    }

    fun generateMaze(game: Game, w: Int, h: Int, previousLevel: Level?, previousPosition: Position?): Pair<Level, Position> {
        require(w > 2 && h > 2) { "width and height should be > 2 (w = $w, h = $h)" }

        val edges = mutableSetOf<Position>()
        val connectivity = UnionFind<Position>()

        val cells = array2D(w, h) { x, y ->
            when {
                x == 0 || y == 0 || x == w - 1 || y == h - 1 -> Cell(STONE)
                (x + y) % 2 == 1 -> null.also { edges += Position(x, y) }
                x % 2 == 0 -> Cell(STONE)
                else -> Cell(AIR)
            }
        }

        while (edges.isNotEmpty()) {
            val edge = edges.random(game.random.asKotlinRandom()).also { edges.remove(it) }
            val (v1, v2) = if (edge.x % 2 == 0) {
                Position(edge.x - 1, edge.y) to Position(edge.x + 1, edge.y)
            } else {
                Position(edge.x, edge.y - 1) to Position(edge.x, edge.y + 1)
            }

            cells[edge.x][edge.y] = if (connectivity.areConnected(v1, v2)) {
                if (game.withProbability(0.1)) {
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
        return Level(game,
            (previousLevel?.difficulty ?: 0) + 1,
            cells as Array<Array<Cell>>).also { level ->
            for ((pos, cell) in level.withPosition()) {
                if (cell.type in Monster.canStandIn && pos != startPos && game.withProbability(0.01)) {
                    level.createMonster(pos)
                }
            }

            level.withPosition().filter { (_, cell) -> cell.isEmpty() && cell.type in Monster.canStandIn }
                .shuffled(game.random).first().second.type = DOWNSTAIRS

            if (previousLevel != null) {
                level[startPos].type = UPSTAIRS
                game.stairsMap[level to startPos] = previousLevel to previousPosition!!
            }

            game.characters += game.time to DefaultMonsterSpawner(level)
        } to Position(1, 1)
    }

    class DefaultMonsterSpawner(level: Level) : MonsterSpawner(level, duration = 40, delay = 100) {
        override fun positionValidator(position: Position): Boolean {
            return level[position].type in Monster.canStandIn
        }

        override fun spawn(position: Position): Character {
            return level.randomMonster(position)
        }

        @Serializable
        data class SavedDefaultMonsterSpawner(override val saveId: Int, val level: Int) : SavedStrong<DefaultMonsterSpawner> {
            override val refs = listOf(level)
            override fun initial(pool: Pool) = DefaultMonsterSpawner(pool.load(level))
        }

        override fun save() = SavedDefaultMonsterSpawner(saveId, level.saveId)
    }

    private fun Level.randomMonster(position: Position) =
        when (game.random.nextInt(3)) {
            0 -> basicMonster(position)
            1 -> upsetMonster(position)
            else -> anxietyMonster(position)
        }

    private fun Level.basicMonster(position: Position) =
        Monster(this, position, 30, 50 + difficulty * 20, strength = 10, type = BASIC)

    private fun Level.upsetMonster(position: Position) =
        Monster(this, position, 20, 100 + difficulty * 25, strength = 10, type = UPSET)

    private fun Level.anxietyMonster(position: Position) =
        Monster(this, position, 20, 50 + difficulty * 20, strength = 15, type = ANXIETY)

}
