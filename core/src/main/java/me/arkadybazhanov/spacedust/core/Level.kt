package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.Serializable

class Cell(var type: CellType) {
    val events = mutableListOf<Event>()
    var character: Character? = null
    val items = mutableListOf<Item>()

    fun isEmpty() = character == null
}

enum class CellType {
    STONE, AIR, DOWNSTAIRS, UPSTAIRS
}

class Level(
    val game: Game,
    private val cells: Array2D<Cell>,
    override val saveId: Int = Game.getNextId()
) : Iterable<Cell>, Savable {
    override val refs get() = cells.flatMap { it.flatMap(Cell::events) + it.flatMap(Cell::items) } + game

    init {
        require(cells.isNotEmpty())
    }

    val w = cells.size
    val h = cells[0].size

    override fun iterator(): Iterator<Cell> = iterator {
        for (col in cells) for (cell in col) yield(cell)
    }

    fun withPosition(): Iterable<Pair<Position, Cell>> = iterator {
        for ((x, col) in cells.withIndex()) {
            for ((y, cell) in col.withIndex()) {
                yield(Position(x, y) to cell)
            }
        }
    }.asSequence().asIterable()

    operator fun get(position: Position): Cell = cells[position.x][position.y]

    operator fun get(x: Int, y: Int): Cell = cells[x][y]

    @Serializable
    data class SavedCell(
        val type: CellType,
        val events: List<Int>,
        val character: Int?,
        val items: List<Int>
    )

    @Serializable
    class SavedLevel(
        override val saveId: Int,
        val game: Int,
        val cells: Array2D<SavedCell>
    ) : SavedStrong<Level> {
        override val refs = cells.flatMap { it.flatMap(SavedCell::events) + it.flatMap(SavedCell::items) }

        override fun initial(pool: Pool) = Level(pool.load(game), cells.map {
            Cell(it.type)
        })

        override fun restore(initial: Level, pool: Pool) {
            for ((pos, cell) in initial.withPosition()) {
                val savedCell = cells[pos.x][pos.y]
                if (savedCell.character != null) {
                    cell.character = pool.load(savedCell.character)
                }
                cell.events += savedCell.events.loadEach(pool)
                cell.items += savedCell.items.map { id -> pool.load<Item>(id) }
            }
        }

    }

    override fun save() = SavedLevel(saveId, game.saveId, cells.map {
        SavedCell(
            it.type,
            it.events.map(Event::saveId),
            it.character?.saveId,
            it.items.map(Item::saveId)
        )
    })
}

typealias Array2D<T> = Array<Array<T>>

inline fun <reified T> array2D(w: Int, h: Int, init: (Int, Int) -> T) = Array(w) { x ->
    Array(h) { y ->
        init(x, y)
    }
}

inline fun <T, reified U> Array2D<T>.map(transform: (T) -> U): Array2D<U> = Array(size) { x ->
    Array(this[x].size) { y ->
        transform(this[x][y])
    }
}
