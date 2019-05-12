package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.Serializable
import me.arkadybazhanov.spacedust.core.CellType.*

object LevelGeneration {
    inline fun <T : Character> generateLevelAndCreate(game: Game, characterSupplier: (Level, Position) -> T): Pair<Level, T> {
        val (level, position) = generateMaze(game, 31, 31)
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

    fun generateMaze(game: Game, w: Int, h: Int): Pair<Level, Position> {
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
        return Level(game, cells as Array<Array<Cell>>).also { level ->
            for ((pos, cell) in level.withPosition()) {
                if (cell.type in BasicMonster.canStandIn && pos != startPos && Game.withProbability(0.0/*5*/)) {
                    level.createDefaultMonster(pos)
                }
            }

            game.characters += game.time to DefaultMonsterSpawner(level)
        } to Position(1, 1)
    }

    class DefaultMonsterSpawner(level: Level) : MonsterSpawner(level, duration = 40, delay = 100) {

        override fun positionValidator(position: Position): Boolean {
            return level[position].type in BasicMonster.canStandIn
        }

        override fun spawn(position: Position): Character {
            return level.defaultMonster(position)
        }

        @Serializable
        data class SavedDefaultMonsterSpawner(val level: Int) : SavedStrong<DefaultMonsterSpawner> {
            override val refs = listOf(level)
            override fun initial(pool: Map<Int, Savable>) = DefaultMonsterSpawner(pool.load(level))
        }

        override fun save() = SavedDefaultMonsterSpawner(level.saveId)
    }

    private fun Level.defaultMonster(position: Position) =
        BasicMonster(this, position, 20, 100, 10)

    fun generateSmallRoom(game: Game): Pair<Level, Position> {
        val level = Level(game, array2D(3, 3) { _, _ ->
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
