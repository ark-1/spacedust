package me.arkadybazhanov.spacedust

import android.content.res.*
import android.graphics.*
import me.arkadybazhanov.spacedust.LevelSnapshot.CellSnapshot
import me.arkadybazhanov.spacedust.R.drawable.*
import me.arkadybazhanov.spacedust.core.*
import me.arkadybazhanov.spacedust.core.CellType.*

class LevelDrawer(private val resources: Resources) {
    var scaleFactor = 1.0f

    var shiftX = 0f
    var shiftY = 0f

    private fun square(left: Float, top: Float, width: Float) = RectF(
        left,
        top,
        left + width,
        top + width
    )

    fun drawLevel(level: LevelSnapshot, canvas: Canvas) {
        canvas.save()
        canvas.scale(scaleFactor, scaleFactor)
        canvas.translate(shiftX, shiftY)
        val cellWidth = CELL_WIDTH

        for ((x, y, cell) in level.withCoordinates()) {
            val (bm, bm2) = cell.toBitmaps()
            val src = Rect(0, 0, bm.width, bm.height)
            val dst = square(
                x * cellWidth,
                y * cellWidth,
                cellWidth
            )
            canvas.drawBitmap(bm, src, dst, null)
            bm2?.let { canvas.drawBitmap(it, src, dst, null) }
        }
        canvas.restore()
    }

    private val bitmaps = object {
        private val cache = mutableMapOf<Int, Bitmap>()

        operator fun get(id: Int) = cache[id] ?: (
            BitmapFactory.decodeResource(resources, id) ?: error("Could not decode cell bitmap")
        ).also { cache[id] = it }
    }

    private fun CellSnapshot.toBitmaps(): Pair<Bitmap, Bitmap?> {
        val cellId = when (type) {
            AIR -> air
            STONE -> stone
        }
        val id = when (characterType) {
            null -> null
            Player::class -> player
            else -> monster
        }
        return bitmaps[cellId] to id?.let { bitmaps[it] }
    }

    fun getCell(level: LevelSnapshot, x: Float, y: Float): Position? {
        val cellWidth = CELL_WIDTH
        val position = Position(
            x = ((x / scaleFactor - shiftX) / cellWidth).toInt(),
            y = ((y / scaleFactor - shiftY) / cellWidth).toInt()
        )
        return position.takeIf { it.isValid(level.w, level.h) }
    }

    companion object {
        private const val CELL_WIDTH = 64f
    }
}
