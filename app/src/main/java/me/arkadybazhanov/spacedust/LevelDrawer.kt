package me.arkadybazhanov.spacedust

import android.content.res.*
import android.graphics.*
import me.arkadybazhanov.spacedust.LevelSnapshot.CellSnapshot
import me.arkadybazhanov.spacedust.R.drawable.*
import me.arkadybazhanov.spacedust.core.*
import me.arkadybazhanov.spacedust.core.CellType.*

class LevelDrawer(private val resources: Resources) {
    var scaleFactor = 1.0

    var shiftX = 0f
    var shiftY = 0f

    private fun square(left: Float, top: Float, width: Float) = RectF(
        left,
        top,
        left + width,
        top + width
    )

    fun drawLevel(level: LevelSnapshot, canvas: Canvas) {
        val cellWidth = cellWidth(canvas.width, level)

        for ((x, y, cell) in level.withCoordinates()) {
            val bm = cell.toBitmap()
            val src = Rect(0, 0, bm.width, bm.height)
            val dst = square(
                shiftX + x * cellWidth,
                shiftY + y * cellWidth,
                cellWidth
            )
            canvas.drawBitmap(bm, src, dst, null)
        }
    }

    private val bitmaps = object {
        private val cache = mutableMapOf<Int, Bitmap>()

        operator fun get(id: Int) = cache[id] ?: (
            BitmapFactory.decodeResource(resources, id) ?: error("Could not decode cell bitmap")
        ).also { cache[id] = it }
    }

    private fun CellSnapshot.toBitmap(): Bitmap {
        val id = when (characterType) {

            null -> when (type) {
                AIR -> blue
                STONE -> white
            }

            Player::class -> player
            else -> monster
        }
        return bitmaps[id]
    }

    fun getCell(level: LevelSnapshot, canvasWidth: Int, x: Float, y: Float): Position {
        val cellWidth = cellWidth(canvasWidth, level)
        return Position(((x - shiftX) / cellWidth).toInt(), ((y - shiftY) / cellWidth).toInt())
    }

    private fun cellWidth(canvasWidth: Int, level: LevelSnapshot) =
        (canvasWidth / level.w * scaleFactor).toFloat()
}
