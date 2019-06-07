package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.Serializable
import me.arkadybazhanov.spacedust.core.CellType.*
import me.arkadybazhanov.spacedust.core.Monster.MonsterType.*

object LevelGeneration {
    const val DEFAULT_LEVEL_SIZE = 13

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

    private fun Level.createDefaultMonster(position: Position) {
        defaultMonster(position).create()
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
        return Level(game,
            (previousLevel?.difficulty ?: 0) + 1,
            cells as Array<Array<Cell>>).also { level ->
            for ((pos, cell) in level.withPosition()) {
                if (cell.type in Monster.canStandIn && pos != startPos && Game.withProbability(0.0/*5*/)) {
                    level.createDefaultMonster(pos)
                }
            }

            level.withPosition().filter { (_, cell) -> cell.isEmpty() && cell.type in Monster.canStandIn }
                .shuffled().first().second.type = DOWNSTAIRS

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
            return level.defaultMonster(position)
        }

        @Serializable
        data class SavedDefaultMonsterSpawner(override val saveId: Int, val level: Int) : SavedStrong<DefaultMonsterSpawner> {
            override val refs = listOf(level)
            override fun initial(pool: Pool) = DefaultMonsterSpawner(pool.load(level))
        }

        override fun save() = SavedDefaultMonsterSpawner(saveId, level.saveId)
    }

    private fun Level.defaultMonster(position: Position) =
        Monster(this, position, 20, 100, 100, 10, UPSET)

    fun generateSmallRoom(game: Game): Pair<Level, Position> {
        val level = Level(game, 1, array2D(3, 3) { _, _ ->
            Cell(AIR)
        })

        level.withPosition().shuffled(Game.random).first { (pos, _) ->
            pos.y != 0 || pos.x == 1
        }.second.type = STONE

        level.createDefaultMonster(Position(2, 0))

        return level to Position(0, 0)
    }

    inline fun <T : Character> generateSmallRoomAndCreate(
        game: Game,
        characterSupplier: (Level, Position) -> T
    ): Pair<Level, T> {
        val (level, position) = generateSmallRoom(game)
        return create(level, position, characterSupplier)
    }
}
